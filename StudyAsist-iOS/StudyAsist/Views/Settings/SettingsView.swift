//
//  SettingsView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct SettingsView: View {
    @Environment(\.modelContext) private var modelContext
    @ObservedObject private var settings = SettingsStore.shared
    @AppStorage("geminiApiKey") private var geminiApiKey = ""
    @EnvironmentObject private var appState: AppState
    
    @State private var syncStatus: String?
    @State private var isSyncing = false
    @State private var showingBackupExport = false
    @State private var showingBackupImport = false
    
    var body: some View {
        NavigationStack {
            List {
                Section("Account") {
                    if appState.isSignedIn {
                        Button("Sign out", role: .destructive) {
                            AuthService.shared.signOut()
                            appState.isSignedIn = false
                        }
                    } else {
                        Button("Sign in with Apple") {
                            Task {
                                do {
                                    try await AuthService.shared.signInWithApple()
                                    appState.isSignedIn = true
                                } catch {
                                    syncStatus = "Sign in failed: \(error.localizedDescription)"
                                }
                            }
                        }
                    }
                }
                
                Section("Profile") {
                    TextField("Your name", text: $settings.userName)
                        .textContentType(.name)
                }
                
                Section("AI") {
                    SecureField("Gemini API Key", text: $geminiApiKey)
                    Text("Required for Explain and Solve. Get a key from Google AI Studio.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                
                Section("Notifications") {
                    Picker("Default reminder", selection: $settings.defaultLeadMinutes) {
                        Text("5 min").tag(5)
                        Text("10 min").tag(10)
                        Text("15 min").tag(15)
                        Text("30 min").tag(30)
                        Text("60 min").tag(60)
                    }
                    Toggle("Vibration", isOn: $settings.vibrationEnabled)
                }
                
                if appState.isSignedIn {
                    Section("Sync") {
                        if let status = syncStatus {
                            Text(status)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Button {
                            uploadToCloud()
                        } label: {
                            HStack {
                                Text("Upload to cloud")
                                if isSyncing { Spacer(); ProgressView() }
                            }
                        }
                        .disabled(isSyncing)
                        Button {
                            downloadFromCloud()
                        } label: {
                            HStack {
                                Text("Download from cloud")
                                if isSyncing { Spacer(); ProgressView() }
                            }
                        }
                        .disabled(isSyncing)
                    }
                }
                
                Section("Backup & Restore") {
                    Button("Export backup") {
                        exportBackup()
                    }
                    Button("Restore from backup") {
                        showingBackupImport = true
                    }
                }
                
                Section("About") {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("1.0.0")
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("Settings")
            .sheet(isPresented: $showingBackupImport) {
                BackupImportView(
                    onImport: { json in
                        try? BackupService.importFromJson(json, modelContext: modelContext)
                        NotificationScheduler.shared.rescheduleAll(modelContext: modelContext)
                        showingBackupImport = false
                    },
                    onCancel: {
                        showingBackupImport = false
                    }
                )
            }
        }
    }
    
    private func exportBackup() {
        let json = (try? BackupService.exportToJson(modelContext: modelContext, settings: settings)) ?? "{}"
        let filename = "studyasist_backup_\(Int(Date().timeIntervalSince1970)).json"
        let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent(filename)
        try? json.write(to: tempURL, atomically: true, encoding: .utf8)
        // Share sheet - in a real app we'd use UIActivityViewController
        // For now we show an alert with the path
        syncStatus = "Exported to \(tempURL.path)"
    }
    
    private func uploadToCloud() {
        isSyncing = true
        syncStatus = nil
        Task {
            do {
                let backupData = try BackupService.exportToBackupData(modelContext: modelContext, settings: settings)
                let client = ApiClient(baseURL: settings.backendBaseURL)
                let _: SyncUploadResponse = try await client.request(
                    path: "api/sync/upload",
                    method: "POST",
                    body: SyncUploadRequest(payload: backupData, version: 2),
                    authenticated: true
                )
                await MainActor.run {
                    syncStatus = "Uploaded successfully"
                    isSyncing = false
                }
            } catch {
                await MainActor.run {
                    syncStatus = "Upload failed: \(error.localizedDescription)"
                    isSyncing = false
                }
            }
        }
    }
    
    private func downloadFromCloud() {
        isSyncing = true
        syncStatus = nil
        Task {
            do {
                let client = ApiClient(baseURL: settings.backendBaseURL)
                let response: SyncDownloadResponse = try await client.request(path: "api/sync/download", method: "GET")
                let encoder = JSONEncoder()
                let payloadData = try encoder.encode(response.payload)
                let json = String(data: payloadData, encoding: .utf8) ?? "{}"
                try await MainActor.run {
                    try BackupService.importFromJson(json, modelContext: modelContext)
                    NotificationScheduler.shared.rescheduleAll(modelContext: modelContext)
                    syncStatus = "Downloaded successfully"
                    isSyncing = false
                }
            } catch {
                await MainActor.run {
                    syncStatus = "Download failed: \(error.localizedDescription)"
                    isSyncing = false
                }
            }
        }
    }
}

struct BackupImportView: View {
    let onImport: (String) throws -> Void
    let onCancel: () -> Void
    
    @State private var pastedText = ""
    @State private var errorMessage: String?
    
    var body: some View {
        NavigationStack {
            Form {
                TextField("Paste backup JSON", text: $pastedText, axis: .vertical)
                    .lineLimit(5...20)
                if let err = errorMessage {
                    Text(err)
                        .foregroundStyle(.red)
                }
            }
            .navigationTitle("Restore Backup")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { onCancel() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Restore") {
                        do {
                            try onImport(pastedText)
                        } catch {
                            errorMessage = error.localizedDescription
                        }
                    }
                    .disabled(pastedText.isEmpty)
                }
            }
        }
    }
}

// API types for sync
struct SyncUploadRequest: Encodable {
    let payload: BackupData
    let version: Int
}
struct SyncUploadResponse: Decodable {
    let ok: Bool
    let id: Int?
}
struct SyncDownloadResponse: Decodable {
    let payload: BackupData
    let version: Int
}

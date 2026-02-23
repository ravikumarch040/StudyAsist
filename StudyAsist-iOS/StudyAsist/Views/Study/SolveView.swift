//
//  SolveView.swift
//  StudyAsist
//
//  AI step-by-step solution via Gemini API
//

import SwiftUI

struct SolveView: View {
    @AppStorage("geminiApiKey") private var apiKey = ""
    @State private var problemText = ""
    @State private var solution = ""
    @State private var isLoading = false
    
    var body: some View {
        Form {
            Section("Problem") {
                TextField("Enter problem or question...", text: $problemText, axis: .vertical)
                    .lineLimit(4...10)
            }
            Section("Solution") {
                if isLoading {
                    ProgressView()
                } else if !solution.isEmpty {
                    Text(solution)
                }
            }
        }
        .navigationTitle("Solve")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Solve") {
                    Task { await solve() }
                }
                .disabled(problemText.isEmpty || apiKey.isEmpty || isLoading)
            }
        }
    }
    
    private func solve() async {
        guard !apiKey.isEmpty else { return }
        isLoading = true
        solution = ""
        defer { isLoading = false }
        
        do {
            let url = URL(string: "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash:generateContent?key=\(apiKey)")!
            var req = URLRequest(url: url)
            req.httpMethod = "POST"
            req.setValue("application/json", forHTTPHeaderField: "Content-Type")
            req.httpBody = try JSONSerialization.data(withJSONObject: [
                "contents": [["parts": [["text": "Solve this step by step:\n\n\(problemText)"]]]]
            ])
            let (data, _) = try await URLSession.shared.data(for: req)
            let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
            let candidates = json?["candidates"] as? [[String: Any]]
            let content = candidates?.first?["content"] as? [String: Any]
            let parts = content?["parts"] as? [[String: Any]]
            solution = (parts?.first?["text"] as? String) ?? "No response"
        } catch {
            solution = "Error: \(error.localizedDescription)"
        }
    }
}

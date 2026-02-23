//
//  ExplainView.swift
//  StudyAsist
//
//  AI explanation via Gemini API
//

import SwiftUI

struct ExplainView: View {
    @AppStorage("geminiApiKey") private var apiKey = ""
    @State private var inputText = ""
    @State private var explanation = ""
    @State private var isLoading = false
    
    var body: some View {
        Form {
            Section("Text to explain") {
                TextField("Paste or type text...", text: $inputText, axis: .vertical)
                    .lineLimit(4...10)
            }
            Section("Explanation") {
                if isLoading {
                    ProgressView()
                } else if !explanation.isEmpty {
                    Text(explanation)
                }
            }
        }
        .navigationTitle("Explain")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Explain") {
                    Task { await explain() }
                }
                .disabled(inputText.isEmpty || apiKey.isEmpty || isLoading)
            }
        }
    }
    
    private func explain() async {
        guard !apiKey.isEmpty else { return }
        isLoading = true
        explanation = ""
        defer { isLoading = false }
        
        do {
            let url = URL(string: "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash:generateContent?key=\(apiKey)")!
            var req = URLRequest(url: url)
            req.httpMethod = "POST"
            req.setValue("application/json", forHTTPHeaderField: "Content-Type")
            req.httpBody = try JSONSerialization.data(withJSONObject: [
                "contents": [["parts": [["text": "Explain this in simple terms:\n\n\(inputText)"]]]]
            ])
            let (data, _) = try await URLSession.shared.data(for: req)
            let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
            let candidates = json?["candidates"] as? [[String: Any]]
            let content = candidates?.first?["content"] as? [String: Any]
            let parts = content?["parts"] as? [[String: Any]]
            explanation = (parts?.first?["text"] as? String) ?? "No response"
        } catch {
            explanation = "Error: \(error.localizedDescription)"
        }
    }
}

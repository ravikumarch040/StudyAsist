//
//  QAEditView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct QAEditView: View {
    var qa: QA?
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    
    @State private var questionText: String
    @State private var answerText: String
    @State private var subject: String
    @State private var chapter: String
    @State private var questionType: QuestionType
    @State private var showingScan = false
    
    init(qa: QA? = nil) {
        self.qa = qa
        if let q = qa {
            _questionText = State(initialValue: q.questionText)
            _answerText = State(initialValue: q.answerText)
            _subject = State(initialValue: q.subject ?? "")
            _chapter = State(initialValue: q.chapter ?? "")
            _questionType = State(initialValue: q.questionType)
        } else {
            _questionText = State(initialValue: "")
            _answerText = State(initialValue: "")
            _subject = State(initialValue: "")
            _chapter = State(initialValue: "")
            _questionType = State(initialValue: .short)
        }
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Question") {
                    TextField("Question", text: $questionText, axis: .vertical)
                        .lineLimit(3...8)
                    Picker("Type", selection: $questionType) {
                        ForEach([QuestionType.short, .mcq, .trueFalse, .numeric, .essay], id: \.self) { t in
                            Text(typeLabel(t)).tag(t)
                        }
                    }
                }
                
                Section("Answer") {
                    TextField("Answer", text: $answerText, axis: .vertical)
                        .lineLimit(2...6)
                }
                
                Section("Organization") {
                    TextField("Subject", text: $subject)
                    TextField("Chapter", text: $chapter)
                }
            }
            .navigationTitle(qa != nil ? "Edit Q&A" : "Add Q&A")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { save() }
                        .disabled(questionText.trimmingCharacters(in: .whitespaces).isEmpty || answerText.trimmingCharacters(in: .whitespaces).isEmpty)
                }
                ToolbarItem(placement: .primaryAction) {
                    if qa == nil {
                        Button {
                            showingScan = true
                        } label: {
                            Image(systemName: "camera")
                        }
                    }
                }
            }
            .sheet(isPresented: $showingScan) {
                QAScanView { extractedText in
                    questionText = extractedText
                    showingScan = false
                }
            }
        }
    }
    
    private func typeLabel(_ t: QuestionType) -> String {
        switch t {
        case .short: return "Short"
        case .mcq: return "MCQ"
        case .trueFalse: return "True/False"
        case .numeric: return "Numeric"
        case .essay: return "Essay"
        default: return t.rawValue
        }
    }
    
    private func save() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let q = questionText.trimmingCharacters(in: .whitespaces)
        let a = answerText.trimmingCharacters(in: .whitespaces)
        guard !q.isEmpty, !a.isEmpty else { return }
        
        if let existing = qa {
            existing.questionText = q
            existing.answerText = a
            existing.subject = subject.isEmpty ? nil : subject
            existing.chapter = chapter.isEmpty ? nil : chapter
            existing.questionType = questionType
        } else {
            let maxId = (try? modelContext.fetch(FetchDescriptor<QA>()).map(\.id).max()) ?? 0
            let newQA = QA(
                id: maxId + 1,
                questionText: q,
                answerText: a,
                subject: subject.isEmpty ? nil : subject,
                chapter: chapter.isEmpty ? nil : chapter,
                questionType: questionType,
                createdAt: now
            )
            modelContext.insert(newQA)
        }
        try? modelContext.save()
        dismiss()
    }
}

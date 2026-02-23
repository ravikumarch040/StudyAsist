//
//  AssessmentCreateView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct AssessmentCreateView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @Query(sort: \Goal.name) private var goals: [Goal]
    @Query private var qaBank: [QA]
    
    @State private var title = ""
    @State private var selectedGoalId: Int64?
    @State private var filterSubject: String?
    @State private var filterChapter: String?
    @State private var totalTimeMinutes = 30
    @State private var randomizeQuestions = true
    @State private var selectedQAIds: Set<Int64> = []
    
    private var subjects: [String] {
        Array(Set(qaBank.compactMap(\.subject).filter { !$0.isEmpty })).sorted()
    }
    
    private var chapters: [String] {
        Array(Set(qaBank.compactMap(\.chapter).filter { !$0.isEmpty })).sorted()
    }
    
    private var filteredQA: [QA] {
        var result = qaBank
        if let s = filterSubject, !s.isEmpty {
            result = result.filter { $0.subject == s }
        }
        if let c = filterChapter, !c.isEmpty {
            result = result.filter { $0.chapter == c }
        }
        return result
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Details") {
                    TextField("Title", text: $title)
                    Picker("Goal", selection: $selectedGoalId) {
                        Text("None").tag(nil as Int64?)
                        ForEach(goals.filter { $0.isActive }, id: \.id) { g in
                            Text(g.name).tag(g.id as Int64?)
                        }
                    }
                    Stepper("Time: \(totalTimeMinutes) min", value: $totalTimeMinutes, in: 5...120, step: 5)
                    Toggle("Randomize questions", isOn: $randomizeQuestions)
                }
                
                Section("Filter") {
                    Picker("Subject", selection: $filterSubject) {
                        Text("All").tag(nil as String?)
                        ForEach(subjects, id: \.self) { Text($0).tag($0 as String?) }
                    }
                    Picker("Chapter", selection: $filterChapter) {
                        Text("All").tag(nil as String?)
                        ForEach(chapters, id: \.self) { Text($0).tag($0 as String?) }
                    }
                }
                
                Section("Questions (\(selectedQAIds.count) selected)") {
                    List {
                        ForEach(filteredQA, id: \.id) { qa in
                            Button {
                                if selectedQAIds.contains(qa.id) {
                                    selectedQAIds.remove(qa.id)
                                } else {
                                    selectedQAIds.insert(qa.id)
                                }
                            } label: {
                                HStack {
                                    Text(qa.questionText)
                                        .lineLimit(2)
                                        .foregroundStyle(.primary)
                                    Spacer()
                                    if selectedQAIds.contains(qa.id) {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundStyle(.green)
                                    }
                                }
                            }
                        }
                    }
                    .frame(maxHeight: 200)
                }
            }
            .navigationTitle("Create Assessment")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Create") { create() }
                        .disabled(title.isEmpty || selectedQAIds.isEmpty)
                }
            }
        }
    }
    
    private func create() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let maxAssessId = (try? modelContext.fetch(FetchDescriptor<Assessment>()).map(\.id).max()) ?? 0
        let assessment = Assessment(
            id: maxAssessId + 1,
            title: title.trimmingCharacters(in: .whitespaces),
            goalId: selectedGoalId,
            subject: filterSubject,
            chapter: filterChapter,
            totalTimeSeconds: totalTimeMinutes * 60,
            randomizeQuestions: randomizeQuestions,
            createdAt: now
        )
        modelContext.insert(assessment)
        
        let qaIds = randomizeQuestions ? Array(selectedQAIds).shuffled() : Array(selectedQAIds).sorted()
        let maxQId = (try? modelContext.fetch(FetchDescriptor<AssessmentQuestion>()).map(\.id).max()) ?? 0
        for (i, qid) in qaIds.enumerated() {
            let aq = AssessmentQuestion(
                id: maxQId + Int64(i) + 1,
                assessmentId: assessment.id,
                qaId: qid,
                sequence: i + 1
            )
            modelContext.insert(aq)
        }
        try? modelContext.save()
        dismiss()
    }
}

//
//  GoalEditView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct GoalEditView: View {
    var goal: Goal?
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    
    @State private var name: String
    @State private var description: String
    @State private var examDate: Date
    @State private var isActive: Bool
    @State private var subjectItems: [SubjectChapterRow]
    
    struct SubjectChapterRow: Identifiable {
        let id = UUID()
        var subject: String
        var chapterList: String
    }
    
    init(goal: Goal? = nil) {
        self.goal = goal
        if let g = goal {
            _name = State(initialValue: g.name)
            _description = State(initialValue: g.goalDescription ?? "")
            _examDate = State(initialValue: Date(timeIntervalSince1970: TimeInterval(g.examDate) / 1000))
            _isActive = State(initialValue: g.isActive)
            _subjectItems = State(initialValue: [])
        } else {
            _name = State(initialValue: "")
            _description = State(initialValue: "")
            _examDate = State(initialValue: Date().addingTimeInterval(30 * 24 * 3600))
            _isActive = State(initialValue: true)
            _subjectItems = State(initialValue: [])
        }
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Details") {
                    TextField("Goal name", text: $name)
                        .textContentType(.name)
                    TextField("Description (optional)", text: $description, axis: .vertical)
                        .lineLimit(2...4)
                    DatePicker("Exam date", selection: $examDate, displayedComponents: .date)
                    Toggle("Active", isOn: $isActive)
                }
                
                Section("Subjects & Chapters") {
                    ForEach($subjectItems) { $item in
                        HStack {
                            TextField("Subject", text: $item.subject)
                            TextField("Chapters", text: $item.chapterList)
                            Button(role: .destructive) {
                                subjectItems.removeAll { $0.id == item.id }
                            } label: {
                                Image(systemName: "trash")
                            }
                        }
                    }
                    Button("Add subject") {
                        subjectItems.append(SubjectChapterRow(subject: "", chapterList: ""))
                    }
                }
            }
            .navigationTitle(goal != nil ? "Edit Goal" : "Add Goal")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { save() }
                        .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
            .onAppear {
                if let g = goal {
                    let items = try? modelContext.fetch(FetchDescriptor<GoalItem>(predicate: #Predicate { $0.goalId == g.id }))
                    subjectItems = (items ?? []).map { SubjectChapterRow(subject: $0.subject, chapterList: $0.chapterList) }
                }
            }
        }
    }
    
    private func save() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let examMillis = Int64(examDate.timeIntervalSince1970 * 1000)
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        
        if let existing = goal {
            existing.name = trimmedName
            existing.goalDescription = description.isEmpty ? nil : description
            existing.examDate = examMillis
            existing.isActive = isActive
            
            let oldItems = try? modelContext.fetch(FetchDescriptor<GoalItem>(predicate: #Predicate { $0.goalId == existing.id }))
            oldItems?.forEach { modelContext.delete($0) }
        } else {
            let maxId = (try? modelContext.fetch(FetchDescriptor<Goal>()).map(\.id).max()) ?? 0
            let newGoal = Goal(
                id: maxId + 1,
                name: trimmedName,
                description: description.isEmpty ? nil : description,
                examDate: examMillis,
                createdAt: now,
                isActive: isActive
            )
            modelContext.insert(newGoal)
            
            let maxItemId = (try? modelContext.fetch(FetchDescriptor<GoalItem>()).map(\.id).max()) ?? 0
            for (i, item) in subjectItems.enumerated() where !item.subject.trimmingCharacters(in: .whitespaces).isEmpty {
                let gi = GoalItem(
                    id: maxItemId + Int64(i) + 1,
                    goalId: newGoal.id,
                    subject: item.subject,
                    chapterList: item.chapterList
                )
                modelContext.insert(gi)
            }
            dismiss()
            return
        }
        
        let maxItemId = (try? modelContext.fetch(FetchDescriptor<GoalItem>()).map(\.id).max()) ?? 0
        for (i, item) in subjectItems.enumerated() where !item.subject.trimmingCharacters(in: .whitespaces).isEmpty {
            let gi = GoalItem(
                id: maxItemId + Int64(i) + 1,
                goalId: goal!.id,
                subject: item.subject,
                chapterList: item.chapterList
            )
            modelContext.insert(gi)
        }
        try? modelContext.save()
        dismiss()
    }
}

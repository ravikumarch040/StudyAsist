//
//  GoalDetailView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct GoalDetailView: View {
    @Bindable var goal: Goal
    @Environment(\.modelContext) private var modelContext
    @Query private var goalItems: [GoalItem]
    @Query private var qaBank: [QA]
    @Query private var assessments: [Assessment]
    @Query private var results: [Result]
    @State private var showingEditSheet = false
    
    private var items: [GoalItem] {
        goalItems.filter { $0.goalId == goal.id }
    }
    
    private var daysRemaining: Int {
        let now = Date()
        let exam = Date(timeIntervalSince1970: TimeInterval(goal.examDate) / 1000)
        return max(0, Calendar.current.dateComponents([.day], from: now, to: exam).day ?? 0)
    }
    
    private var qaCount: Int {
        let subjects = Set(items.map(\.subject))
        let chapters = Set(items.flatMap { $0.chapterList.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) } })
        return qaBank.filter { q in
            let subMatch = q.subject.map { subjects.contains($0) } ?? false
            let chMatch = q.chapter.map { chapters.contains($0) } ?? (chapters.isEmpty)
            return subMatch && chMatch
        }.count
    }
    
    private var assessmentCount: Int {
        assessments.filter { $0.goalId == goal.id }.count
    }
    
    var body: some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text("\(daysRemaining)")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                        Text("days remaining")
                            .font(.body)
                            .foregroundStyle(.secondary)
                            .padding(.top, 12)
                    }
                    if let desc = goal.goalDescription, !desc.isEmpty {
                        Text(desc)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                }
                .padding(.vertical, 8)
            }
            
            Section("Subjects") {
                if items.isEmpty {
                    Text("No subjects added")
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(items, id: \.id) { item in
                        VStack(alignment: .leading) {
                            Text(item.subject)
                                .font(.headline)
                            Text(item.chapterList)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            
            Section("Progress") {
                Label("Q&A in bank: \(qaCount)", systemImage: "doc.text")
                Label("Assessments: \(assessmentCount)", systemImage: "list.bullet.clipboard")
            }
        }
        .navigationTitle(goal.name)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Edit") {
                    showingEditSheet = true
                }
            }
        }
        .sheet(isPresented: $showingEditSheet) {
            GoalEditView(goal: goal)
        }
    }
}

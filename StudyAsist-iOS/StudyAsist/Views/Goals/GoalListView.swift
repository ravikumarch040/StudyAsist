//
//  GoalListView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct GoalListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Goal.examDate) private var goals: [Goal]
    @Query private var goalItems: [GoalItem]
    @State private var showingAddSheet = false
    @State private var goalToEdit: Goal?
    
    var body: some View {
        NavigationStack {
            List {
                ForEach(goals.filter { $0.isActive }, id: \.id) { goal in
                    NavigationLink(value: goal) {
                        GoalRowView(goal: goal, goalItems: goalItems.filter { $0.goalId == goal.id })
                    }
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button(role: .destructive) {
                            deleteGoal(goal)
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                        Button {
                            goalToEdit = goal
                        } label: {
                            Label("Edit", systemImage: "pencil")
                        }
                    }
                }
            }
            .navigationTitle("Goals")
            .navigationDestination(for: Goal.self) { goal in
                GoalDetailView(goal: goal)
            }
            .overlay {
                if goals.filter({ $0.isActive }).isEmpty {
                    ContentUnavailableView(
                        "No goals",
                        systemImage: "target",
                        description: Text("Add an exam goal to track your progress")
                    )
                }
            }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showingAddSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAddSheet) {
                GoalEditView()
            }
            .sheet(item: $goalToEdit) { goal in
                GoalEditView(goal: goal)
            }
        }
    }
    
    private func deleteGoal(_ goal: Goal) {
        let items = goalItems.filter { $0.goalId == goal.id }
        items.forEach { modelContext.delete($0) }
        modelContext.delete(goal)
        try? modelContext.save()
    }
}

struct GoalRowView: View {
    let goal: Goal
    let goalItems: [GoalItem]
    
    private var daysRemaining: Int {
        let now = Date()
        let exam = Date(timeIntervalSince1970: TimeInterval(goal.examDate) / 1000)
        return Calendar.current.dateComponents([.day], from: now, to: exam).day ?? 0
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(goal.name)
                .font(.headline)
            if !goalItems.isEmpty {
                Text(goalItems.map { "\($0.subject): \($0.chapterList)" }.joined(separator: "; "))
                    .font(.caption)
                    .lineLimit(2)
                    .foregroundStyle(.secondary)
            }
            Text("\(max(0, daysRemaining)) days remaining")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}

extension Goal: @retroactive Identifiable {}

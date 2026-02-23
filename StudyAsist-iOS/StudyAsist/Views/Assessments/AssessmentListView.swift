//
//  AssessmentListView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct AssessmentListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Assessment.createdAt, order: .reverse) private var assessments: [Assessment]
    @State private var showingCreateSheet = false
    
    var body: some View {
        NavigationStack {
            List {
                ForEach(assessments, id: \.id) { assessment in
                    NavigationLink(value: assessment) {
                        AssessmentRowView(assessment: assessment)
                    }
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button(role: .destructive) {
                            deleteAssessment(assessment)
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                    }
                }
            }
            .navigationTitle("Assessments")
            .navigationDestination(for: Assessment.self) { assessment in
                AssessmentRunView(assessment: assessment)
            }
            .overlay {
                if assessments.isEmpty {
                    ContentUnavailableView(
                        "No assessments",
                        systemImage: "list.bullet.clipboard",
                        description: Text("Create a practice test from your Q&A bank")
                    )
                }
            }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showingCreateSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingCreateSheet) {
                AssessmentCreateView()
            }
        }
    }
    
    private func deleteAssessment(_ assessment: Assessment) {
        let questions = try? modelContext.fetch(FetchDescriptor<AssessmentQuestion>(predicate: #Predicate { $0.assessmentId == assessment.id }))
        questions?.forEach { modelContext.delete($0) }
        modelContext.delete(assessment)
        try? modelContext.save()
    }
}

struct AssessmentRowView: View {
    let assessment: Assessment
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(assessment.title)
                .font(.headline)
            Text("\(assessment.totalTimeSeconds / 60) min")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}

//
//  ResultListView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct ResultListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Result.id, order: .reverse) private var results: [Result]
    @Query private var attempts: [Attempt]
    @Query private var assessments: [Assessment]
    
    private func assessmentTitle(for attemptId: Int64) -> String {
        guard let attempt = attempts.first(where: { $0.id == attemptId }),
              let assessment = assessments.first(where: { $0.id == attempt.assessmentId }) else {
            return "Unknown"
        }
        return assessment.title
    }
    
    var body: some View {
        NavigationStack {
            List {
                ForEach(results, id: \.id) { result in
                    NavigationLink(value: result) {
                        HStack {
                            VStack(alignment: .leading) {
                                Text(assessmentTitle(for: result.attemptId))
                                    .font(.headline)
                                Text("\(Int(result.percent))% · \(Int(result.score))/\(Int(result.maxScore))")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Text("\(Int(result.percent))%")
                                .font(.title3.bold())
                                .foregroundStyle(percentColor(result.percent))
                        }
                    }
                }
            }
            .navigationTitle("Results")
            .navigationDestination(for: Result.self) { result in
                ResultDetailView(result: result)
            }
            .overlay {
                if results.isEmpty {
                    ContentUnavailableView(
                        "No results",
                        systemImage: "checkmark.circle",
                        description: Text("Complete an assessment to see your results")
                    )
                }
            }
        }
    }
    
    private func percentColor(_ p: Float) -> Color {
        if p >= 70 { return .green }
        if p >= 50 { return .orange }
        return .red
    }
}

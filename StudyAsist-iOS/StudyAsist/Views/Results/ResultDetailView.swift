//
//  ResultDetailView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct ResultDetailView: View {
    let result: Result
    @Environment(\.modelContext) private var modelContext
    @Query private var attempts: [Attempt]
    @Query private var assessments: [Assessment]
    @Query private var attemptAnswers: [AttemptAnswer]
    @Query private var qaBank: [QA]
    
    private var attempt: Attempt? {
        attempts.first { $0.id == result.attemptId }
    }
    
    private var assessment: Assessment? {
        attempt.flatMap { a in assessments.first { $0.id == a.assessmentId } }
    }
    
    private var answers: [AttemptAnswer] {
        attemptAnswers.filter { $0.attemptId == result.attemptId }
    }
    
    var body: some View {
        List {
            Section {
                HStack {
                    Text("Score")
                    Spacer()
                    Text("\(Int(result.score)) / \(Int(result.maxScore)) (\(Int(result.percent))%)")
                        .fontWeight(.semibold)
                }
            }
            
            Section("Questions") {
                ForEach(answers, id: \.id) { ans in
                    let qa = qaBank.first { $0.id == ans.qaId }
                    if let qa {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(qa.questionText)
                                .font(.subheadline)
                                .lineLimit(2)
                            Text("Your answer: \(ans.answerText ?? "(none)")")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Text("Correct: \(qa.answerText)")
                                .font(.caption)
                                .foregroundStyle(.green)
                        }
                    }
                }
            }
        }
        .navigationTitle(assessment?.title ?? "Result")
    }
}

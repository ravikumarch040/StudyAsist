//
//  AssessmentRunView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct AssessmentRunView: View {
    let assessment: Assessment
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    
    @State private var questions: [(qa: QA, aq: AssessmentQuestion)] = []
    @State private var currentIndex = 0
    @State private var answers: [Int64: String] = [:]
    @State private var timeRemaining: Int
    @State private var timer: Timer?
    @State private var isRunning = false
    @State private var showingSubmitConfirm = false
    @State private var result: Result?
    
    init(assessment: Assessment) {
        self.assessment = assessment
        _timeRemaining = State(initialValue: assessment.totalTimeSeconds)
    }
    
    var body: some View {
        Group {
            if let r = result {
                AssessmentResultView(result: r)
                    .toolbar(.hidden, for: .navigationBar)
            } else if questions.isEmpty {
                ProgressView("Loading...")
            } else {
                runUI
            }
        }
        .navigationBarBackButtonHidden(isRunning)
        .onAppear {
            loadQuestions()
        }
    }
    
    private var runUI: some View {
        VStack(spacing: 0) {
            if isRunning {
                HStack {
                    Text(timeString(timeRemaining))
                        .font(.title2.monospacedDigit())
                        .foregroundStyle(timeRemaining < 60 ? .red : .primary)
                    Spacer()
                    Text("\(currentIndex + 1) / \(questions.count)")
                        .foregroundStyle(.secondary)
                }
                .padding()
                .background(Color(.systemGroupedBackground))
            }
            
            if currentIndex < questions.count {
                let item = questions[currentIndex]
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text(item.qa.questionText)
                            .font(.headline)
                        TextField("Your answer", text: Binding(
                            get: { answers[item.qa.id] ?? "" },
                            set: { answers[item.qa.id] = $0 }
                        ), axis: .vertical)
                        .textFieldStyle(.roundedBorder)
                        .lineLimit(3...8)
                    }
                    .padding()
                }
                
                HStack {
                    if currentIndex > 0 {
                        Button("Previous") {
                            currentIndex -= 1
                        }
                    }
                    Spacer()
                    if currentIndex < questions.count - 1 {
                        Button("Next") {
                            currentIndex += 1
                        }
                        .buttonStyle(.borderedProminent)
                    } else {
                        Button("Submit") {
                            showingSubmitConfirm = true
                        }
                        .buttonStyle(.borderedProminent)
                    }
                }
                .padding()
            }
        }
        .onAppear {
            if !isRunning && !questions.isEmpty {
                startTimer()
            }
        }
        .alert("Submit assessment?", isPresented: $showingSubmitConfirm) {
            Button("Cancel", role: .cancel) {}
            Button("Submit") {
                submitAssessment()
            }
        } message: {
            Text("You have \(questions.count - answers.count) unanswered questions.")
        }
    }
    
    private func loadQuestions() {
        let aqDescriptor = FetchDescriptor<AssessmentQuestion>(predicate: #Predicate { $0.assessmentId == assessment.id }, sort: [SortDescriptor(\.sequence)])
        let aqList = (try? modelContext.fetch(aqDescriptor)) ?? []
        let qaDescriptor = FetchDescriptor<QA>()
        let allQA = (try? modelContext.fetch(qaDescriptor)) ?? []
        let qaMap = Dictionary(uniqueKeysWithValues: allQA.map { ($0.id, $0) })
        questions = aqList.compactMap { aq in
            guard let qa = qaMap[aq.qaId] else { return nil }
            return (qa, aq)
        }
    }
    
    private func startTimer() {
        isRunning = true
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            if timeRemaining > 0 {
                timeRemaining -= 1
            } else {
                timer?.invalidate()
                submitAssessment()
            }
        }
    }
    
    private func submitAssessment() {
        timer?.invalidate()
        isRunning = false
        
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let maxAttemptId = (try? modelContext.fetch(FetchDescriptor<Attempt>()).map(\.id).max()) ?? 0
        let attempt = Attempt(
            id: maxAttemptId + 1,
            assessmentId: assessment.id,
            startedAt: now - Int64(assessment.totalTimeSeconds - timeRemaining),
            endedAt: now,
            needsManualReview: false
        )
        modelContext.insert(attempt)
        
        let maxAnswerId = (try? modelContext.fetch(FetchDescriptor<AttemptAnswer>()).map(\.id).max()) ?? 0
        for (i, item) in questions.enumerated() {
            let ans = AttemptAnswer(
                id: maxAnswerId + Int64(i) + 1,
                attemptId: attempt.id,
                qaId: item.qa.id,
                answerText: answers[item.qa.id],
                submittedAt: now
            )
            modelContext.insert(ans)
        }
        
        var score: Float = 0
        var maxScore: Float = Float(questions.count)
        for item in questions {
            let userAns = (answers[item.qa.id] ?? "").trimmingCharacters(in: .whitespaces).lowercased()
            let correctAns = item.qa.answerText.trimmingCharacters(in: .whitespaces).lowercased()
            if !userAns.isEmpty && userAns == correctAns {
                score += 1
            }
        }
        let percent = maxScore > 0 ? (score / maxScore) * 100 : 0
        let detailsJson = "[]"
        
        let maxResultId = (try? modelContext.fetch(FetchDescriptor<Result>()).map(\.id).max()) ?? 0
        let r = Result(
            id: maxResultId + 1,
            attemptId: attempt.id,
            score: score,
            maxScore: maxScore,
            percent: percent,
            detailsJson: detailsJson
        )
        modelContext.insert(r)
        try? modelContext.save()
        
        result = r
    }
    
    private func timeString(_ seconds: Int) -> String {
        let m = seconds / 60
        let s = seconds % 60
        return String(format: "%d:%02d", m, s)
    }
}

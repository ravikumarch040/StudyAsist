//
//  AssessmentResultView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct AssessmentResultView: View {
    let result: Result
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        VStack(spacing: 24) {
            Text("Assessment Complete")
                .font(.title)
            
            ZStack {
                Circle()
                    .stroke(Color.gray.opacity(0.3), lineWidth: 12)
                    .frame(width: 120, height: 120)
                Circle()
                    .trim(from: 0, to: CGFloat(result.percent) / 100)
                    .stroke(Color.green, style: StrokeStyle(lineWidth: 12, lineCap: .round))
                    .frame(width: 120, height: 120)
                    .rotationEffect(.degrees(-90))
                Text("\(Int(result.percent))%")
                    .font(.title.bold())
            }
            
            Text("\(Int(result.score)) / \(Int(result.maxScore)) correct")
                .font(.headline)
            
            Button("Done") {
                dismiss()
            }
            .buttonStyle(.borderedProminent)
            .padding(.top)
        }
        .padding()
    }
}

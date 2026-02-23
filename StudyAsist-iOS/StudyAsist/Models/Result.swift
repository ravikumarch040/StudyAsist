//
//  Result.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class Result {
    @Attribute(.unique) var id: Int64
    var attemptId: Int64
    var score: Float
    var maxScore: Float
    var percent: Float
    var detailsJson: String
    var manualFeedback: String?
    
    init(
        id: Int64 = 0,
        attemptId: Int64,
        score: Float,
        maxScore: Float,
        percent: Float,
        detailsJson: String,
        manualFeedback: String? = nil
    ) {
        self.id = id
        self.attemptId = attemptId
        self.score = score
        self.maxScore = maxScore
        self.percent = percent
        self.detailsJson = detailsJson
        self.manualFeedback = manualFeedback
    }
}

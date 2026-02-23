//
//  AttemptAnswer.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class AttemptAnswer {
    @Attribute(.unique) var id: Int64
    var attemptId: Int64
    var qaId: Int64
    var answerText: String?
    var answerImageUri: String?
    var answerVoiceUri: String?
    var submittedAt: Int64
    var timeSpentSeconds: Int
    
    init(
        id: Int64 = 0,
        attemptId: Int64,
        qaId: Int64,
        answerText: String? = nil,
        answerImageUri: String? = nil,
        answerVoiceUri: String? = nil,
        submittedAt: Int64,
        timeSpentSeconds: Int = 0
    ) {
        self.id = id
        self.attemptId = attemptId
        self.qaId = qaId
        self.answerText = answerText
        self.answerImageUri = answerImageUri
        self.answerVoiceUri = answerVoiceUri
        self.submittedAt = submittedAt
        self.timeSpentSeconds = timeSpentSeconds
    }
}

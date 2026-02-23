//
//  Assessment.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class Assessment {
    @Attribute(.unique) var id: Int64
    var title: String
    var goalId: Int64?
    var subject: String?
    var chapter: String?
    var totalTimeSeconds: Int
    var randomizeQuestions: Bool
    var createdAt: Int64
    
    init(
        id: Int64 = 0,
        title: String,
        goalId: Int64? = nil,
        subject: String? = nil,
        chapter: String? = nil,
        totalTimeSeconds: Int,
        randomizeQuestions: Bool = true,
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.title = title
        self.goalId = goalId
        self.subject = subject
        self.chapter = chapter
        self.totalTimeSeconds = totalTimeSeconds
        self.randomizeQuestions = randomizeQuestions
        self.createdAt = createdAt
    }
}

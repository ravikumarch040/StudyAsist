//
//  GoalItem.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class GoalItem {
    @Attribute(.unique) var id: Int64
    var goalId: Int64
    var subject: String
    var chapterList: String
    var targetHours: Int?
    
    init(
        id: Int64 = 0,
        goalId: Int64,
        subject: String,
        chapterList: String,
        targetHours: Int? = nil
    ) {
        self.id = id
        self.goalId = goalId
        self.subject = subject
        self.chapterList = chapterList
        self.targetHours = targetHours
    }
}

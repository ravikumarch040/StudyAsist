//
//  Attempt.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class Attempt {
    @Attribute(.unique) var id: Int64
    var assessmentId: Int64
    var startedAt: Int64
    var endedAt: Int64?
    var userNotes: String?
    var needsManualReview: Bool
    
    init(
        id: Int64 = 0,
        assessmentId: Int64,
        startedAt: Int64,
        endedAt: Int64? = nil,
        userNotes: String? = nil,
        needsManualReview: Bool = false
    ) {
        self.id = id
        self.assessmentId = assessmentId
        self.startedAt = startedAt
        self.endedAt = endedAt
        self.userNotes = userNotes
        self.needsManualReview = needsManualReview
    }
}

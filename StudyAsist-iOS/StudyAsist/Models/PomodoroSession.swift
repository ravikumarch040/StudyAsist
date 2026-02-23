//
//  PomodoroSession.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class PomodoroSession {
    @Attribute(.unique) var id: Int64
    var startedAt: Int64
    var endedAt: Int64?
    var durationMinutes: Int
    var typeRaw: String
    var subject: String?
    var completed: Bool
    
    init(
        id: Int64 = 0,
        startedAt: Int64,
        endedAt: Int64? = nil,
        durationMinutes: Int,
        type: String,
        subject: String? = nil,
        completed: Bool = false
    ) {
        self.id = id
        self.startedAt = startedAt
        self.endedAt = endedAt
        self.durationMinutes = durationMinutes
        self.typeRaw = type
        self.subject = subject
        self.completed = completed
    }
}

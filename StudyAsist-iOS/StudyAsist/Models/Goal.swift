//
//  Goal.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class Goal {
    @Attribute(.unique) var id: Int64
    var name: String
    var goalDescription: String?
    var examDate: Int64
    var createdAt: Int64
    var isActive: Bool
    
    init(
        id: Int64 = 0,
        name: String,
        description: String? = nil,
        examDate: Int64,
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        isActive: Bool = true
    ) {
        self.id = id
        self.name = name
        self.goalDescription = description
        self.examDate = examDate
        self.createdAt = createdAt
        self.isActive = isActive
    }
}

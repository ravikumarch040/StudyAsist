//
//  StudyToolHistory.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class StudyToolHistory {
    @Attribute(.unique) var id: Int64
    var toolType: String
    var inputText: String
    var usedAt: Int64
    
    init(
        id: Int64 = 0,
        toolType: String,
        inputText: String,
        usedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.toolType = toolType
        self.inputText = inputText
        self.usedAt = usedAt
    }
}

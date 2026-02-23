//
//  ChatMessage.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class ChatMessage {
    @Attribute(.unique) var id: Int64
    var role: String
    var content: String
    var subject: String?
    var chapter: String?
    var createdAt: Int64
    
    init(
        id: Int64 = 0,
        role: String,
        content: String,
        subject: String? = nil,
        chapter: String? = nil,
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.role = role
        self.content = content
        self.subject = subject
        self.chapter = chapter
        self.createdAt = createdAt
    }
}

//
//  BadgeEarned.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class BadgeEarned {
    @Attribute(.unique) var id: Int64
    var badgeId: String
    var earnedAt: Int64
    
    init(
        id: Int64 = 0,
        badgeId: String,
        earnedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.badgeId = badgeId
        self.earnedAt = earnedAt
    }
}

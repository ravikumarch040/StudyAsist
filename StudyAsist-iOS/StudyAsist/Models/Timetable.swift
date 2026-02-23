//
//  Timetable.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class Timetable {
    @Attribute(.unique) var id: Int64
    var name: String
    var weekTypeRaw: String
    var startDate: Int64?
    var createdAt: Int64
    var updatedAt: Int64
    
    var weekType: WeekType {
        get { WeekType(rawValue: weekTypeRaw) ?? .monSun }
        set { weekTypeRaw = newValue.rawValue }
    }
    
    init(
        id: Int64 = 0,
        name: String,
        weekType: WeekType = .monSun,
        startDate: Int64? = nil,
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        updatedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.name = name
        self.weekTypeRaw = weekType.rawValue
        self.startDate = startDate
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}

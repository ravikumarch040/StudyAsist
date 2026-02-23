//
//  Activity.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class Activity {
    @Attribute(.unique) var id: Int64
    var timetableId: Int64
    var dayOfWeek: Int // 1=Monday .. 7=Sunday
    var startTimeMinutes: Int
    var endTimeMinutes: Int
    var title: String
    var typeRaw: String
    var note: String?
    var notifyEnabled: Bool
    var notifyLeadMinutes: Int
    var useSpeechSound: Bool
    var alarmTtsMessage: String?
    var sortOrder: Int
    
    var type: ActivityType {
        get { ActivityType(rawValue: typeRaw) ?? .study }
        set { typeRaw = newValue.rawValue }
    }
    
    init(
        id: Int64 = 0,
        timetableId: Int64,
        dayOfWeek: Int,
        startTimeMinutes: Int,
        endTimeMinutes: Int,
        title: String,
        type: ActivityType = .study,
        note: String? = nil,
        notifyEnabled: Bool = false,
        notifyLeadMinutes: Int = 0,
        useSpeechSound: Bool = false,
        alarmTtsMessage: String? = nil,
        sortOrder: Int = 0
    ) {
        self.id = id
        self.timetableId = timetableId
        self.dayOfWeek = dayOfWeek
        self.startTimeMinutes = startTimeMinutes
        self.endTimeMinutes = endTimeMinutes
        self.title = title
        self.typeRaw = type.rawValue
        self.note = note
        self.notifyEnabled = notifyEnabled
        self.notifyLeadMinutes = notifyLeadMinutes
        self.useSpeechSound = useSpeechSound
        self.alarmTtsMessage = alarmTtsMessage
        self.sortOrder = sortOrder
    }
}

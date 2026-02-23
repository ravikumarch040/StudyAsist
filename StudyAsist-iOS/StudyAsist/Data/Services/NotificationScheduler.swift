//
//  NotificationScheduler.swift
//  StudyAsist
//

import Foundation
import UserNotifications
import SwiftData

final class NotificationScheduler {
    static let shared = NotificationScheduler()
    
    private init() {}
    
    func rescheduleAll(modelContext: ModelContext) {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
        
        let descriptor = FetchDescriptor<Activity>(predicate: #Predicate { $0.notifyEnabled == true })
        guard let activities = try? modelContext.fetch(descriptor) else { return }
        
        let timetableDescriptor = FetchDescriptor<Timetable>()
        guard let timetables = try? modelContext.fetch(timetableDescriptor) else { return }
        let timetableNames = Dictionary(uniqueKeysWithValues: timetables.map { ($0.id, $0.name) })
        
        let settings = SettingsStore.shared
        let defaultLead = settings.defaultLeadMinutes
        
        for activity in activities {
            let lead = activity.notifyLeadMinutes > 0 ? activity.notifyLeadMinutes : defaultLead
            let triggerMinutes = activity.startTimeMinutes - lead
            let timetableName = timetableNames[activity.timetableId] ?? "Timetable"
            let title = "Study Reminder"
            let body = "\(timeString(activity.startTimeMinutes))–\(timeString(activity.endTimeMinutes)) – \(activity.title)"
            if let note = activity.note, !note.isEmpty {
                let bodyWithNote = "\(body) (\(note))"
                scheduleWeeklyReminder(
                    identifier: "activity-\(activity.id)",
                    weekday: activity.dayOfWeek,
                    triggerMinutes: max(0, triggerMinutes),
                    title: title,
                    body: bodyWithNote
                )
            } else {
                scheduleWeeklyReminder(
                    identifier: "activity-\(activity.id)",
                    weekday: activity.dayOfWeek,
                    triggerMinutes: max(0, triggerMinutes),
                    title: title,
                    body: body
                )
            }
        }
    }
    
    private func scheduleWeeklyReminder(
        identifier: String,
        weekday: Int,
        triggerMinutes: Int,
        title: String,
        body: String
    ) {
        var dateComponents = DateComponents()
        dateComponents.weekday = weekday
        dateComponents.hour = triggerMinutes / 60
        dateComponents.minute = triggerMinutes % 60
        
        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }
    
    private func timeString(_ minutes: Int) -> String {
        let h = minutes / 60
        let m = minutes % 60
        return String(format: "%d:%02d", h, m)
    }
    
    func requestAuthorization(completion: @escaping (Bool) -> Void) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            DispatchQueue.main.async {
                completion(granted)
            }
        }
    }
}

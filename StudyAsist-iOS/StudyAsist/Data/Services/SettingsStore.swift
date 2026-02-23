//
//  SettingsStore.swift
//  StudyAsist
//

import Foundation
import SwiftUI

final class SettingsStore: ObservableObject {
    static let shared = SettingsStore()
    
    private let defaults = UserDefaults.standard
    
    @AppStorage("defaultLeadMinutes") var defaultLeadMinutes: Int = 5
    @AppStorage("vibrationEnabled") var vibrationEnabled: Bool = true
    @AppStorage("userName") var userName: String = ""
    @AppStorage("activeTimetableId") var activeTimetableId: Int64 = 0
    @AppStorage("backendBaseURL") var backendBaseURL: String = "http://localhost:8000"
    @AppStorage("geminiApiKey") var geminiApiKey: String = ""
    @AppStorage("pomodoroFocus") var pomodoroFocusMinutes: Int = 25
    @AppStorage("pomodoroShortBreak") var pomodoroShortBreakMinutes: Int = 5
    
    private init() {}
}

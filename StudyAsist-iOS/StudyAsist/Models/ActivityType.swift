//
//  ActivityType.swift
//  StudyAsist
//

import Foundation

enum ActivityType: String, Codable, CaseIterable {
    case study = "STUDY"
    case breakType = "BREAK"
    case school = "SCHOOL"
    case tuition = "TUITION"
    case sleep = "SLEEP"
}

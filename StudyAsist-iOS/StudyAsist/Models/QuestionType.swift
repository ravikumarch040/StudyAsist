//
//  QuestionType.swift
//  StudyAsist
//

import Foundation

enum QuestionType: String, Codable, CaseIterable {
    case mcq = "MCQ"
    case fillBlank = "FILL_BLANK"
    case short = "SHORT"
    case essay = "ESSAY"
    case numeric = "NUMERIC"
    case trueFalse = "TRUE_FALSE"
    case matching = "MATCHING"
    case diagram = "DIAGRAM"
}

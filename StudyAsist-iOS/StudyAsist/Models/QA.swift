//
//  QA.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class QA {
    @Attribute(.unique) var id: Int64
    var sourceCaptureId: Int64?
    var subject: String?
    var chapter: String?
    var questionText: String
    var answerText: String
    var questionTypeRaw: String
    var optionsJson: String?
    var metadataJson: String?
    var createdAt: Int64
    var easeFactor: Double
    var srsInterval: Int
    var repetitions: Int
    var nextReviewDate: Int64
    var lastReviewDate: Int64?
    
    var questionType: QuestionType {
        get { QuestionType(rawValue: questionTypeRaw) ?? .short }
        set { questionTypeRaw = newValue.rawValue }
    }
    
    init(
        id: Int64 = 0,
        sourceCaptureId: Int64? = nil,
        subject: String? = nil,
        chapter: String? = nil,
        questionText: String,
        answerText: String,
        questionType: QuestionType = .short,
        optionsJson: String? = nil,
        metadataJson: String? = nil,
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        easeFactor: Double = 2.5,
        srsInterval: Int = 0,
        repetitions: Int = 0,
        nextReviewDate: Int64 = 0,
        lastReviewDate: Int64? = nil
    ) {
        self.id = id
        self.sourceCaptureId = sourceCaptureId
        self.subject = subject
        self.chapter = chapter
        self.questionText = questionText
        self.answerText = answerText
        self.questionTypeRaw = questionType.rawValue
        self.optionsJson = optionsJson
        self.metadataJson = metadataJson
        self.createdAt = createdAt
        self.easeFactor = easeFactor
        self.srsInterval = srsInterval
        self.repetitions = repetitions
        self.nextReviewDate = nextReviewDate
        self.lastReviewDate = lastReviewDate
    }
}

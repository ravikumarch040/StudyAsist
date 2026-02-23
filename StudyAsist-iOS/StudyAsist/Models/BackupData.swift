//
//  BackupData.swift
//  StudyAsist
//
//  Codable structures for backup/sync - must match Android BackupRepository JSON format.
//

import Foundation

// MARK: - Backup Data (Sync Payload)

struct BackupData: Codable {
    let version: Int
    let exportedAt: Int64
    let timetables: [TimetableDTO]
    let activities: [ActivityDTO]
    let goals: [GoalDTO]
    let goalItems: [GoalItemDTO]
    let qaBank: [QADTO]
    let assessments: [AssessmentDTO]
    let assessmentQuestions: [AssessmentQuestionDTO]
    let attempts: [AttemptDTO]
    let attemptAnswers: [AttemptAnswerDTO]
    let results: [ResultDTO]
    let studyToolHistory: [StudyToolHistoryDTO]?
    let badgesEarned: [BadgeEarnedDTO]?
    let settings: BackupSettingsDTO?
}

// MARK: - DTOs (matching Android entity JSON)

struct TimetableDTO: Codable {
    let id: Int64
    let name: String
    let weekType: String
    let startDate: Int64?
    let createdAt: Int64
    let updatedAt: Int64
}

struct ActivityDTO: Codable {
    let id: Int64
    let timetableId: Int64
    let dayOfWeek: Int
    let startTimeMinutes: Int
    let endTimeMinutes: Int
    let title: String
    let type: String
    let note: String?
    let notifyEnabled: Bool
    let notifyLeadMinutes: Int
    let useSpeechSound: Bool?
    let alarmTtsMessage: String?
    let sortOrder: Int
}

struct GoalDTO: Codable {
    let id: Int64
    let name: String
    let description: String?
    let examDate: Int64
    let createdAt: Int64
    let isActive: Bool
}

struct GoalItemDTO: Codable {
    let id: Int64
    let goalId: Int64
    let subject: String
    let chapterList: String
    let targetHours: Int?
}

struct QADTO: Codable {
    let id: Int64
    let sourceCaptureId: Int64?
    let subject: String?
    let chapter: String?
    let questionText: String
    let answerText: String
    let questionType: String
    let optionsJson: String?
    let metadataJson: String?
    let createdAt: Int64
    let easeFactor: Double?
    let srsInterval: Int?
    let repetitions: Int?
    let nextReviewDate: Int64?
    let lastReviewDate: Int64?
}

struct AssessmentDTO: Codable {
    let id: Int64
    let title: String
    let goalId: Int64?
    let subject: String?
    let chapter: String?
    let totalTimeSeconds: Int
    let randomizeQuestions: Bool
    let createdAt: Int64
}

struct AssessmentQuestionDTO: Codable {
    let id: Int64
    let assessmentId: Int64
    let qaId: Int64
    let weight: Float?
    let sequence: Int
}

struct AttemptDTO: Codable {
    let id: Int64
    let assessmentId: Int64
    let startedAt: Int64
    let endedAt: Int64?
    let userNotes: String?
    let needsManualReview: Bool
}

struct AttemptAnswerDTO: Codable {
    let id: Int64
    let attemptId: Int64
    let qaId: Int64
    let answerText: String?
    let answerImageUri: String?
    let answerVoiceUri: String?
    let submittedAt: Int64
    let timeSpentSeconds: Int?
}

struct ResultDTO: Codable {
    let id: Int64
    let attemptId: Int64
    let score: Float
    let maxScore: Float
    let percent: Float
    let detailsJson: String
    let manualFeedback: String?
}

struct StudyToolHistoryDTO: Codable {
    let id: Int64
    let toolType: String
    let inputText: String
    let usedAt: Int64
}

struct BadgeEarnedDTO: Codable {
    let id: Int64
    let badgeId: String
    let earnedAt: Int64
}

struct BackupSettingsDTO: Codable {
    let defaultLeadMinutes: Int?
    let vibrationEnabled: Bool?
    let userName: String?
    let ttsVoiceName: String?
    let geminiApiKey: String?
    let focusGuardEnabled: Bool?
    let focusGuardRestrictedExtra: String?
    let blockOverlap: Bool?
    let cloudBackupFolderUri: String?
    let cloudBackupTarget: String?
    let cloudBackupAuto: Bool?
    let useCloudForParsing: Bool?
    let useCloudForGrading: Bool?
    let dictateLanguage: String?
    let explainLanguage: String?
    let solveLanguage: String?
    let darkMode: String?
    let appLocale: String?
    let examGoalAlertDaysThreshold: Int?
    let examGoalAlertPercentThreshold: Int?
}

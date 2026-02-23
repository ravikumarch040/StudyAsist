//
//  BackupService.swift
//  StudyAsist
//

import Foundation
import SwiftData

struct BackupService {
    
    static func exportToBackupData(modelContext: ModelContext, settings: SettingsStore) throws -> BackupData {
        let descriptor = FetchDescriptor<NSManagedObject>()
        // We'll fetch each type separately
        let timetables = try modelContext.fetch(FetchDescriptor<Timetable>(sortBy: [SortDescriptor(\.createdAt)]))
        let activities = try modelContext.fetch(FetchDescriptor<Activity>(sortBy: [SortDescriptor(\.id)]))
        let goals = try modelContext.fetch(FetchDescriptor<Goal>(sortBy: [SortDescriptor(\.createdAt)]))
        let goalItems = try modelContext.fetch(FetchDescriptor<GoalItem>(sortBy: [SortDescriptor(\.id)]))
        let qaBank = try modelContext.fetch(FetchDescriptor<QA>(sortBy: [SortDescriptor(\.createdAt)]))
        let assessments = try modelContext.fetch(FetchDescriptor<Assessment>(sortBy: [SortDescriptor(\.createdAt)]))
        let assessmentQuestions = try modelContext.fetch(FetchDescriptor<AssessmentQuestion>(sortBy: [SortDescriptor(\.sequence)]))
        let attempts = try modelContext.fetch(FetchDescriptor<Attempt>(sortBy: [SortDescriptor(\.startedAt)]))
        let attemptAnswers = try modelContext.fetch(FetchDescriptor<AttemptAnswer>(sortBy: [SortDescriptor(\.id)]))
        let results = try modelContext.fetch(FetchDescriptor<Result>(sortBy: [SortDescriptor(\.id)]))
        let studyToolHistory = try? modelContext.fetch(FetchDescriptor<StudyToolHistory>(sortBy: [SortDescriptor(\.usedAt, order: .reverse)]))
        let badgesEarned = try? modelContext.fetch(FetchDescriptor<BadgeEarned>(sortBy: [SortDescriptor(\.earnedAt)]))
        
        let backup = BackupData(
            version: 2,
            exportedAt: Int64(Date().timeIntervalSince1970 * 1000),
            timetables: timetables.map { TimetableDTO(
                id: $0.id,
                name: $0.name,
                weekType: $0.weekTypeRaw,
                startDate: $0.startDate,
                createdAt: $0.createdAt,
                updatedAt: $0.updatedAt
            )},
            activities: activities.map { ActivityDTO(
                id: $0.id,
                timetableId: $0.timetableId,
                dayOfWeek: $0.dayOfWeek,
                startTimeMinutes: $0.startTimeMinutes,
                endTimeMinutes: $0.endTimeMinutes,
                title: $0.title,
                type: $0.typeRaw,
                note: $0.note,
                notifyEnabled: $0.notifyEnabled,
                notifyLeadMinutes: $0.notifyLeadMinutes,
                useSpeechSound: $0.useSpeechSound,
                alarmTtsMessage: $0.alarmTtsMessage,
                sortOrder: $0.sortOrder
            )},
            goals: goals.map { GoalDTO(
                id: $0.id,
                name: $0.name,
                description: $0.goalDescription,
                examDate: $0.examDate,
                createdAt: $0.createdAt,
                isActive: $0.isActive
            )},
            goalItems: goalItems.map { GoalItemDTO(
                id: $0.id,
                goalId: $0.goalId,
                subject: $0.subject,
                chapterList: $0.chapterList,
                targetHours: $0.targetHours
            )},
            qaBank: qaBank.map { q in
                QADTO(
                    id: q.id,
                    sourceCaptureId: q.sourceCaptureId,
                    subject: q.subject,
                    chapter: q.chapter,
                    questionText: q.questionText,
                    answerText: q.answerText,
                    questionType: q.questionTypeRaw,
                    optionsJson: q.optionsJson,
                    metadataJson: q.metadataJson,
                    createdAt: q.createdAt,
                    easeFactor: q.easeFactor,
                    srsInterval: q.srsInterval,
                    repetitions: q.repetitions,
                    nextReviewDate: q.nextReviewDate,
                    lastReviewDate: q.lastReviewDate
                )
            },
            assessments: assessments.map { AssessmentDTO(
                id: $0.id,
                title: $0.title,
                goalId: $0.goalId,
                subject: $0.subject,
                chapter: $0.chapter,
                totalTimeSeconds: $0.totalTimeSeconds,
                randomizeQuestions: $0.randomizeQuestions,
                createdAt: $0.createdAt
            )},
            assessmentQuestions: assessmentQuestions.map { AssessmentQuestionDTO(
                id: $0.id,
                assessmentId: $0.assessmentId,
                qaId: $0.qaId,
                weight: $0.weight,
                sequence: $0.sequence
            )},
            attempts: attempts.map { AttemptDTO(
                id: $0.id,
                assessmentId: $0.assessmentId,
                startedAt: $0.startedAt,
                endedAt: $0.endedAt,
                userNotes: $0.userNotes,
                needsManualReview: $0.needsManualReview
            )},
            attemptAnswers: attemptAnswers.map { AttemptAnswerDTO(
                id: $0.id,
                attemptId: $0.attemptId,
                qaId: $0.qaId,
                answerText: $0.answerText,
                answerImageUri: $0.answerImageUri,
                answerVoiceUri: $0.answerVoiceUri,
                submittedAt: $0.submittedAt,
                timeSpentSeconds: $0.timeSpentSeconds
            )},
            results: results.map { ResultDTO(
                id: $0.id,
                attemptId: $0.attemptId,
                score: $0.score,
                maxScore: $0.maxScore,
                percent: $0.percent,
                detailsJson: $0.detailsJson,
                manualFeedback: $0.manualFeedback
            )},
            studyToolHistory: studyToolHistory?.map { StudyToolHistoryDTO(
                id: $0.id,
                toolType: $0.toolType,
                inputText: $0.inputText,
                usedAt: $0.usedAt
            )},
            badgesEarned: badgesEarned?.map { BadgeEarnedDTO(
                id: $0.id,
                badgeId: $0.badgeId,
                earnedAt: $0.earnedAt
            )},
            settings: BackupSettingsDTO(
                defaultLeadMinutes: settings.defaultLeadMinutes,
                vibrationEnabled: settings.vibrationEnabled,
                userName: settings.userName.isEmpty ? nil : settings.userName,
                ttsVoiceName: nil,
                geminiApiKey: nil,
                focusGuardEnabled: nil,
                focusGuardRestrictedExtra: nil,
                blockOverlap: nil,
                cloudBackupFolderUri: nil,
                cloudBackupTarget: nil,
                cloudBackupAuto: nil,
                useCloudForParsing: nil,
                useCloudForGrading: nil,
                dictateLanguage: nil,
                explainLanguage: nil,
                solveLanguage: nil,
                darkMode: nil,
                appLocale: nil,
                examGoalAlertDaysThreshold: nil,
                examGoalAlertPercentThreshold: nil
            )
        )
        return backup
    }
    
    static func exportToJson(modelContext: ModelContext, settings: SettingsStore) throws -> String {
        let backup = try exportToBackupData(modelContext: modelContext, settings: settings)
        let encoder = JSONEncoder()
        let data = try encoder.encode(backup)
        return String(data: data, encoding: .utf8) ?? ""
    }
    
    static func importFromJson(_ json: String, modelContext: ModelContext) throws {
        let decoder = JSONDecoder()
        let backup = try decoder.decode(BackupData.self, from: json.data(using: .utf8)!)
        
        // Delete existing data (children before parents for FK consistency)
        try modelContext.delete(model: AttemptAnswer.self)
        try modelContext.delete(model: Attempt.self)
        try modelContext.delete(model: Result.self)
        try modelContext.delete(model: AssessmentQuestion.self)
        try modelContext.delete(model: Assessment.self)
        try modelContext.delete(model: GoalItem.self)
        try modelContext.delete(model: Goal.self)
        try modelContext.delete(model: Activity.self)
        try modelContext.delete(model: Timetable.self)
        try modelContext.delete(model: QA.self)
        try modelContext.delete(model: StudyToolHistory.self)
        try modelContext.delete(model: BadgeEarned.self)
        try modelContext.save()
        
        // Insert from backup
        for dto in backup.timetables {
            let t = Timetable(
                id: dto.id,
                name: dto.name,
                weekType: WeekType(rawValue: dto.weekType) ?? .monSun,
                startDate: dto.startDate,
                createdAt: dto.createdAt,
                updatedAt: dto.updatedAt
            )
            modelContext.insert(t)
        }
        for dto in backup.activities {
            let a = Activity(
                id: dto.id,
                timetableId: dto.timetableId,
                dayOfWeek: dto.dayOfWeek,
                startTimeMinutes: dto.startTimeMinutes,
                endTimeMinutes: dto.endTimeMinutes,
                title: dto.title,
                type: ActivityType(rawValue: dto.type) ?? .study,
                note: dto.note,
                notifyEnabled: dto.notifyEnabled,
                notifyLeadMinutes: dto.notifyLeadMinutes,
                useSpeechSound: dto.useSpeechSound ?? false,
                alarmTtsMessage: dto.alarmTtsMessage,
                sortOrder: dto.sortOrder
            )
            modelContext.insert(a)
        }
        for dto in backup.goals {
            let g = Goal(
                id: dto.id,
                name: dto.name,
                description: dto.description,
                examDate: dto.examDate,
                createdAt: dto.createdAt,
                isActive: dto.isActive
            )
            modelContext.insert(g)
        }
        for dto in backup.goalItems {
            let gi = GoalItem(
                id: dto.id,
                goalId: dto.goalId,
                subject: dto.subject,
                chapterList: dto.chapterList,
                targetHours: dto.targetHours
            )
            modelContext.insert(gi)
        }
        for dto in backup.qaBank {
            let q = QA(
                id: dto.id,
                sourceCaptureId: dto.sourceCaptureId,
                subject: dto.subject,
                chapter: dto.chapter,
                questionText: dto.questionText,
                answerText: dto.answerText,
                questionType: QuestionType(rawValue: dto.questionType) ?? .short,
                optionsJson: dto.optionsJson,
                metadataJson: dto.metadataJson,
                createdAt: dto.createdAt,
                easeFactor: dto.easeFactor ?? 2.5,
                srsInterval: dto.srsInterval ?? 0,
                repetitions: dto.repetitions ?? 0,
                nextReviewDate: dto.nextReviewDate ?? 0,
                lastReviewDate: dto.lastReviewDate
            )
            modelContext.insert(q)
        }
        for dto in backup.assessments {
            let a = Assessment(
                id: dto.id,
                title: dto.title,
                goalId: dto.goalId,
                subject: dto.subject,
                chapter: dto.chapter,
                totalTimeSeconds: dto.totalTimeSeconds,
                randomizeQuestions: dto.randomizeQuestions,
                createdAt: dto.createdAt
            )
            modelContext.insert(a)
        }
        for dto in backup.assessmentQuestions {
            let aq = AssessmentQuestion(
                id: dto.id,
                assessmentId: dto.assessmentId,
                qaId: dto.qaId,
                weight: dto.weight ?? 1.0,
                sequence: dto.sequence
            )
            modelContext.insert(aq)
        }
        for dto in backup.attempts {
            let a = Attempt(
                id: dto.id,
                assessmentId: dto.assessmentId,
                startedAt: dto.startedAt,
                endedAt: dto.endedAt,
                userNotes: dto.userNotes,
                needsManualReview: dto.needsManualReview
            )
            modelContext.insert(a)
        }
        for dto in backup.attemptAnswers {
            let aa = AttemptAnswer(
                id: dto.id,
                attemptId: dto.attemptId,
                qaId: dto.qaId,
                answerText: dto.answerText,
                answerImageUri: dto.answerImageUri,
                answerVoiceUri: dto.answerVoiceUri,
                submittedAt: dto.submittedAt,
                timeSpentSeconds: dto.timeSpentSeconds ?? 0
            )
            modelContext.insert(aa)
        }
        for dto in backup.results {
            let r = Result(
                id: dto.id,
                attemptId: dto.attemptId,
                score: dto.score,
                maxScore: dto.maxScore,
                percent: dto.percent,
                detailsJson: dto.detailsJson,
                manualFeedback: dto.manualFeedback
            )
            modelContext.insert(r)
        }
        for dto in backup.studyToolHistory ?? [] {
            let s = StudyToolHistory(
                id: dto.id,
                toolType: dto.toolType,
                inputText: dto.inputText,
                usedAt: dto.usedAt
            )
            modelContext.insert(s)
        }
        for dto in backup.badgesEarned ?? [] {
            let b = BadgeEarned(
                id: dto.id,
                badgeId: dto.badgeId,
                earnedAt: dto.earnedAt
            )
            modelContext.insert(b)
        }
        
        try modelContext.save()
    }
}

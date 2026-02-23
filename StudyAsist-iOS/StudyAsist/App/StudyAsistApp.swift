//
//  StudyAsistApp.swift
//  StudyAsist
//
//  Native iOS app for StudyAsist - AI-powered study companion.
//

import SwiftUI
import SwiftData

@main
struct StudyAsistApp: App {
    let modelContainer: ModelContainer
    @StateObject private var appState = AppState()
    
    init() {
        do {
            let schema = Schema([
                Timetable.self,
                Activity.self,
                Goal.self,
                GoalItem.self,
                QA.self,
                Assessment.self,
                AssessmentQuestion.self,
                Attempt.self,
                AttemptAnswer.self,
                Result.self,
                BadgeEarned.self,
                StudyToolHistory.self,
                ChatMessage.self,
                PomodoroSession.self
            ])
            let config = ModelConfiguration(
                schema: schema,
                isStoredInMemoryOnly: false
            )
            modelContainer = try ModelContainer(for: schema, configurations: [config])
        } catch {
            fatalError("Could not initialize ModelContainer: \(error)")
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.modelContext, modelContainer.mainContext)
                .environmentObject(appState)
                .task {
                    NotificationScheduler.shared.requestAuthorization { _ in }
                    appState.isSignedIn = KeychainService.shared.getJWT() != nil
                }
        }
    }
}

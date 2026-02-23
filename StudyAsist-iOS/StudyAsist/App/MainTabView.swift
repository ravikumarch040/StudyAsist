//
//  MainTabView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct MainTabView: View {
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            TimetableListView()
                .tabItem {
                    Label("Timetables", systemImage: "calendar")
                }
                .tag(0)
            
            GoalListView()
                .tabItem {
                    Label("Goals", systemImage: "target")
                }
                .tag(1)
            
            StudyHubView()
                .tabItem {
                    Label("Study", systemImage: "book")
                }
                .tag(2)
            
            QABankListView()
                .tabItem {
                    Label("Q&A Bank", systemImage: "doc.text.magnifyingglass")
                }
                .tag(3)
            
            AssessmentListView()
                .tabItem {
                    Label("Assessments", systemImage: "checklist")
                }
                .tag(4)
            
            ResultListView()
                .tabItem {
                    Label("Results", systemImage: "list.bullet.clipboard")
                }
                .tag(5)
            
            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
                .tag(6)
        }
        .tint(.accentColor)
    }
}

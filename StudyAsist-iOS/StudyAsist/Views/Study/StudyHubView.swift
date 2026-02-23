//
//  StudyHubView.swift
//  StudyAsist
//

import SwiftUI

struct StudyHubView: View {
    var body: some View {
        NavigationStack {
            List {
                Section("Study Tools") {
                    NavigationLink("Dictate") {
                        DictateView()
                    }
                    NavigationLink("Explain") {
                        ExplainView()
                    }
                    NavigationLink("Solve") {
                        SolveView()
                    }
                    NavigationLink("Pomodoro") {
                        PomodoroView()
                    }
                }
            }
            .navigationTitle("Study")
        }
    }
}

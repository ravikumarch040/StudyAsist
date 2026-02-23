//
//  ContentView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @EnvironmentObject private var appState: AppState
    
    var body: some View {
        MainTabView()
    }
}

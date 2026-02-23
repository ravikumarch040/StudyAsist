//
//  AppState.swift
//  StudyAsist
//

import Foundation
import SwiftUI

@MainActor
final class AppState: ObservableObject {
    @Published var isSignedIn: Bool = false
    @Published var userName: String?
    @Published var activeTimetableId: Int64?
}

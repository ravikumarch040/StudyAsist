//
//  PomodoroView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct PomodoroView: View {
    @ObservedObject private var settings = SettingsStore.shared
    @State private var totalSeconds: Int = 25 * 60
    @State private var remainingSeconds: Int = 25 * 60
    @State private var isRunning = false
    @State private var isFocus = true
    @State private var timer: Timer?
    @State private var sessionCount = 0
    
    var body: some View {
        VStack(spacing: 24) {
            ZStack {
                Circle()
                    .stroke(Color.gray.opacity(0.3), lineWidth: 8)
                    .frame(width: 200, height: 200)
                Circle()
                    .trim(from: 0, to: CGFloat(remainingSeconds) / CGFloat(totalSeconds))
                    .stroke(isFocus ? Color.blue : Color.green, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                    .frame(width: 200, height: 200)
                    .rotationEffect(.degrees(-90))
                VStack {
                    Text(timeString(remainingSeconds))
                        .font(.system(size: 48, design: .monospaced))
                    Text(isFocus ? "Focus" : "Break")
                        .font(.caption)
                }
            }
            
            HStack(spacing: 16) {
                Button(isRunning ? "Pause" : "Start") {
                    if isRunning {
                        timer?.invalidate()
                        timer = nil
                    } else {
                        startTimer()
                    }
                    isRunning.toggle()
                }
                .buttonStyle(.borderedProminent)
                Button("Reset") {
                    timer?.invalidate()
                    timer = nil
                    isRunning = false
                    remainingSeconds = totalSeconds
                }
            }
        }
        .padding()
        .navigationTitle("Pomodoro")
        .onAppear {
            totalSeconds = settings.pomodoroFocusMinutes * 60
            remainingSeconds = totalSeconds
        }
    }
    
    private func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            if remainingSeconds > 0 {
                remainingSeconds -= 1
            } else {
                timer?.invalidate()
                timer = nil
                isRunning = false
                if isFocus {
                    sessionCount += 1
                    if sessionCount % 4 == 0 {
                        totalSeconds = 15 * 60
                    } else {
                        totalSeconds = settings.pomodoroShortBreakMinutes * 60
                    }
                    isFocus = false
                } else {
                    totalSeconds = settings.pomodoroFocusMinutes * 60
                    isFocus = true
                }
                remainingSeconds = totalSeconds
            }
        }
    }
    
    private func timeString(_ s: Int) -> String {
        "\(s / 60):\(String(format: "%02d", s % 60))"
    }
}

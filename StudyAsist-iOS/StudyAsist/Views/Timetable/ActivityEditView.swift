//
//  ActivityEditView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct ActivityEditView: View {
    let timetable: Timetable
    var activity: Activity?
    var preselectedDay: Int?
    
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    
    @State private var dayOfWeek: Int
    @State private var startTimeMinutes: Int
    @State private var endTimeMinutes: Int
    @State private var title: String
    @State private var type: ActivityType
    @State private var note: String
    @State private var notifyEnabled: Bool
    @State private var notifyLeadMinutes: Int
    @State private var showingOverlapWarning = false
    @State private var overlapActivity: Activity?
    
    init(timetable: Timetable, activity: Activity? = nil, preselectedDay: Int? = nil) {
        self.timetable = timetable
        self.activity = activity
        self.preselectedDay = preselectedDay
        let calendar = Calendar.current
        let w = calendar.component(.weekday, from: Date())
        let defaultDay = w == 1 ? 7 : w - 1
        
        if let a = activity {
            _dayOfWeek = State(initialValue: a.dayOfWeek)
            _startTimeMinutes = State(initialValue: a.startTimeMinutes)
            _endTimeMinutes = State(initialValue: a.endTimeMinutes)
            _title = State(initialValue: a.title)
            _type = State(initialValue: a.type)
            _note = State(initialValue: a.note ?? "")
            _notifyEnabled = State(initialValue: a.notifyEnabled)
            _notifyLeadMinutes = State(initialValue: a.notifyLeadMinutes)
        } else {
            _dayOfWeek = State(initialValue: preselectedDay ?? defaultDay)
            _startTimeMinutes = State(initialValue: 9 * 60)
            _endTimeMinutes = State(initialValue: 10 * 60)
            _title = State(initialValue: "")
            _type = State(initialValue: .study)
            _note = State(initialValue: "")
            _notifyEnabled = State(initialValue: false)
            _notifyLeadMinutes = State(initialValue: 5)
        }
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Schedule") {
                    Picker("Day", selection: $dayOfWeek) {
                        Text("Monday").tag(1)
                        Text("Tuesday").tag(2)
                        Text("Wednesday").tag(3)
                        Text("Thursday").tag(4)
                        Text("Friday").tag(5)
                        Text("Saturday").tag(6)
                        Text("Sunday").tag(7)
                    }
                    
                    HStack {
                        Text("Start")
                        Spacer()
                        TimePicker(minutes: $startTimeMinutes)
                    }
                    HStack {
                        Text("End")
                        Spacer()
                        TimePicker(minutes: $endTimeMinutes)
                    }
                }
                
                Section("Details") {
                    TextField("Title", text: $title)
                        .textContentType(.name)
                    Picker("Type", selection: $type) {
                        Text("Study").tag(ActivityType.study)
                        Text("Break").tag(ActivityType.breakType)
                        Text("School").tag(ActivityType.school)
                        Text("Tuition").tag(ActivityType.tuition)
                        Text("Sleep").tag(ActivityType.sleep)
                    }
                    TextField("Note (optional)", text: $note, axis: .vertical)
                        .lineLimit(2...4)
                }
                
                Section("Reminder") {
                    Toggle("Notify me", isOn: $notifyEnabled)
                    if notifyEnabled {
                        Picker("Notify before", selection: $notifyLeadMinutes) {
                            Text("5 min").tag(5)
                            Text("10 min").tag(10)
                            Text("15 min").tag(15)
                            Text("30 min").tag(30)
                            Text("60 min").tag(60)
                        }
                    }
                }
            }
            .navigationTitle(activity != nil ? "Edit Activity" : "Add Activity")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        saveOrWarnOverlap()
                    }
                    .disabled(title.trimmingCharacters(in: .whitespaces).isEmpty || endTimeMinutes <= startTimeMinutes)
                }
            }
            .alert("Overlap warning", isPresented: $showingOverlapWarning) {
                Button("Save anyway") {
                    performSave()
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                if let overlap = overlapActivity {
                    Text("Another activity (\(overlap.title)) overlaps this slot. Save anyway?")
                }
            }
        }
    }
    
    private func saveOrWarnOverlap() {
        let overlap = findOverlappingActivity()
        if let overlap {
            overlapActivity = overlap
            showingOverlapWarning = true
        } else {
            performSave()
        }
    }
    
    private func findOverlappingActivity() -> Activity? {
        let descriptor = FetchDescriptor<Activity>(predicate: #Predicate<Activity> {
            $0.timetableId == timetable.id && $0.dayOfWeek == dayOfWeek
        })
        let others = (try? modelContext.fetch(descriptor)) ?? []
        let excludeId = activity?.id ?? -1
        return others.first { other in
            other.id != excludeId &&
            other.startTimeMinutes < endTimeMinutes &&
            other.endTimeMinutes > startTimeMinutes
        }
    }
    
    private func performSave() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let trimmedTitle = title.trimmingCharacters(in: .whitespaces)
        guard !trimmedTitle.isEmpty, endTimeMinutes > startTimeMinutes else { return }
        
        if let existing = activity {
            existing.dayOfWeek = dayOfWeek
            existing.startTimeMinutes = startTimeMinutes
            existing.endTimeMinutes = endTimeMinutes
            existing.title = trimmedTitle
            existing.type = type
            existing.note = note.isEmpty ? nil : note
            existing.notifyEnabled = notifyEnabled
            existing.notifyLeadMinutes = notifyLeadMinutes
            existing.updatedAt = now
        } else {
            let maxId = (try? modelContext.fetch(FetchDescriptor<Activity>()).map(\.id).max()) ?? 0
            let newId = max(maxId + 1, now)
            let newActivity = Activity(
                id: newId,
                timetableId: timetable.id,
                dayOfWeek: dayOfWeek,
                startTimeMinutes: startTimeMinutes,
                endTimeMinutes: endTimeMinutes,
                title: trimmedTitle,
                type: type,
                note: note.isEmpty ? nil : note,
                notifyEnabled: notifyEnabled,
                notifyLeadMinutes: notifyLeadMinutes
            )
            modelContext.insert(newActivity)
        }
        try? modelContext.save()
        NotificationScheduler.shared.rescheduleAll(modelContext: modelContext)
        dismiss()
    }
}

struct TimePicker: View {
    @Binding var minutes: Int
    
    var body: some View {
        HStack(spacing: 4) {
            Picker("Hour", selection: Binding(
                get: { minutes / 60 },
                set: { h in minutes = h * 60 + (minutes % 60) }
            )) {
                ForEach(0..<24, id: \.self) { h in
                    Text(String(format: "%02d", h)).tag(h)
                }
            }
            .pickerStyle(.menu)
            .frame(width: 60)
            Text(":")
            Picker("Minute", selection: Binding(
                get: { minutes % 60 },
                set: { m in minutes = (minutes / 60) * 60 + m }
            )) {
                ForEach([0, 15, 30, 45], id: \.self) { m in
                    Text(String(format: "%02d", m)).tag(m)
                }
            }
            .pickerStyle(.menu)
            .frame(width: 60)
        }
    }
}

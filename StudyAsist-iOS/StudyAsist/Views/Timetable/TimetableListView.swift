//
//  TimetableListView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct TimetableListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Timetable.createdAt, order: .reverse) private var timetables: [Timetable]
    @State private var showingAddSheet = false
    @State private var newTimetableName = ""
    @State private var newWeekType: WeekType = .monSun
    
    var body: some View {
        NavigationStack {
            List {
                ForEach(timetables, id: \.id) { timetable in
                    NavigationLink(value: timetable) {
                        TimetableRowView(timetable: timetable)
                    }
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button(role: .destructive) {
                            deleteTimetable(timetable)
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                        Button {
                            duplicateTimetable(timetable)
                        } label: {
                            Label("Duplicate", systemImage: "doc.on.doc")
                        }
                    }
                }
            }
            .navigationTitle("Timetables")
            .navigationDestination(for: Timetable.self) { timetable in
                TimetableDetailView(timetable: timetable)
            }
            .overlay {
                if timetables.isEmpty {
                    ContentUnavailableView(
                        "No timetables",
                        systemImage: "calendar.badge.plus",
                        description: Text("Tap + to create your first timetable")
                    )
                }
            }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showingAddSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAddSheet) {
                NavigationStack {
                    Form {
                        TextField("Name", text: $newTimetableName)
                            .textContentType(.name)
                        Picker("Week type", selection: $newWeekType) {
                            Text("Mon–Sun").tag(WeekType.monSun)
                            Text("Mon–Sat + Sunday").tag(WeekType.monSatPlusSunday)
                        }
                    }
                    .navigationTitle("New Timetable")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .cancellationAction) {
                            Button("Cancel") {
                                showingAddSheet = false
                                resetForm()
                            }
                        }
                        ToolbarItem(placement: .confirmationAction) {
                            Button("Save") {
                                createTimetable()
                            }
                            .disabled(newTimetableName.trimmingCharacters(in: .whitespaces).isEmpty)
                        }
                    }
                }
                .onDisappear { resetForm() }
            }
        }
    }
    
    private func createTimetable() {
        let name = newTimetableName.trimmingCharacters(in: .whitespaces)
        guard !name.isEmpty else { return }
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let id = now
        let timetable = Timetable(
            id: id,
            name: name,
            weekType: newWeekType,
            createdAt: now,
            updatedAt: now
        )
        modelContext.insert(timetable)
        try? modelContext.save()
        showingAddSheet = false
        resetForm()
    }
    
    private func deleteTimetable(_ timetable: Timetable) {
        let activities = try? modelContext.fetch(FetchDescriptor<Activity>(predicate: #Predicate { $0.timetableId == timetable.id }))
        activities?.forEach { modelContext.delete($0) }
        modelContext.delete(timetable)
        try? modelContext.save()
    }
    
    private func duplicateTimetable(_ original: Timetable) {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let newId = now
        let copy = Timetable(
            id: newId,
            name: "\(original.name) (Copy)",
            weekType: original.weekType,
            startDate: original.startDate,
            createdAt: now,
            updatedAt: now
        )
        modelContext.insert(copy)
        
        let activities = try? modelContext.fetch(FetchDescriptor<Activity>(predicate: #Predicate { $0.timetableId == original.id }))
        let allActivities = try? modelContext.fetch(FetchDescriptor<Activity>())
        let maxId = allActivities?.map(\.id).max() ?? 0
        for (index, act) in (activities ?? []).enumerated() {
            let newAct = Activity(
                id: maxId + Int64(index) + 1,
                timetableId: newId,
                dayOfWeek: act.dayOfWeek,
                startTimeMinutes: act.startTimeMinutes,
                endTimeMinutes: act.endTimeMinutes,
                title: act.title,
                type: act.type,
                note: act.note,
                notifyEnabled: act.notifyEnabled,
                notifyLeadMinutes: act.notifyLeadMinutes,
                useSpeechSound: act.useSpeechSound,
                alarmTtsMessage: act.alarmTtsMessage,
                sortOrder: act.sortOrder
            )
            modelContext.insert(newAct)
        }
        try? modelContext.save()
    }
    
    private func resetForm() {
        newTimetableName = ""
        newWeekType = .monSun
    }
}

struct TimetableRowView: View {
    let timetable: Timetable
    @Query private var activities: [Activity]
    
    init(timetable: Timetable) {
        self.timetable = timetable
        let tid = timetable.id
        self._activities = Query(filter: #Predicate<Activity> { $0.timetableId == tid })
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(timetable.name)
                .font(.headline)
            Text("\(timetable.weekType == .monSun ? "Mon–Sun" : "Mon–Sat+Sun") · \(activities.count) activities")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}

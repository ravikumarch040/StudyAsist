//
//  TimetableDetailView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct TimetableDetailView: View {
    @Bindable var timetable: Timetable
    @Environment(\.modelContext) private var modelContext
    @Query private var activities: [Activity]
    @State private var viewMode: ViewMode = .week
    @State private var selectedDay: Int = {
        let c = Calendar.current
        let weekday = c.component(.weekday, from: Date())
        return weekday == 1 ? 7 : weekday - 1
    }()
    @State private var selectedActivity: Activity?
    @State private var showingActivitySheet = false
    @State private var filterType: ActivityType?
    
    enum ViewMode: String, CaseIterable {
        case day = "Day"
        case week = "Week"
    }
    
    init(timetable: Timetable) {
        self.timetable = timetable
        let tid = timetable.id
        self._activities = Query(filter: #Predicate<Activity> { $0.timetableId == tid }, sort: [SortDescriptor(\.dayOfWeek), SortDescriptor(\.startTimeMinutes)])
    }
    
    private var filteredActivities: [Activity] {
        guard let filterType else { return activities }
        return activities.filter { $0.type == filterType }
    }
    
    private var dayActivities: [Activity] {
        filteredActivities.filter { $0.dayOfWeek == selectedDay }
    }
    
    private let dayNames = ["", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
    private let dayNumbers = [1, 2, 3, 4, 5, 6, 7] // Mon=1 .. Sun=7
    
    var body: some View {
        VStack(spacing: 0) {
            Picker("View", selection: $viewMode) {
                ForEach(ViewMode.allCases, id: \.self) { Text($0.rawValue).tag($0) }
            }
            .pickerStyle(.segmented)
            .padding()
            
            if viewMode == .week {
                WeekGridView(
                    timetable: timetable,
                    activities: filteredActivities,
                    onTapSlot: { day, _ in
                        selectedDay = day
                        selectedActivity = nil
                        showingActivitySheet = true
                    },
                    onTapActivity: { activity in
                        selectedActivity = activity
                        showingActivitySheet = true
                    }
                )
            } else {
                DayView(
                    activities: dayActivities,
                    selectedDay: $selectedDay,
                    onTapSlot: {
                        selectedActivity = nil
                        showingActivitySheet = true
                    },
                    onTapActivity: { activity in
                        selectedActivity = activity
                        showingActivitySheet = true
                    }
                )
            }
        }
        .navigationTitle(timetable.name)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Picker("Filter", selection: $filterType) {
                        Text("All").tag(nil as ActivityType?)
                        ForEach(ActivityType.allCases, id: \.self) { type in
                            Text(typeDisplayName(type)).tag(type as ActivityType?)
                        }
                    }
                } label: {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button {
                    selectedActivity = nil
                    let w = Calendar.current.component(.weekday, from: Date())
                    selectedDay = viewMode == .day ? selectedDay : (w == 1 ? 7 : w - 1)
                    showingActivitySheet = true
                } label: {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showingActivitySheet) {
            if let activity = selectedActivity {
                ActivityEditView(timetable: timetable, activity: activity)
            } else {
                ActivityEditView(timetable: timetable, preselectedDay: selectedDay)
            }
        }
    }
    
    private func typeDisplayName(_ type: ActivityType) -> String {
        switch type {
        case .study: return "Study"
        case .breakType: return "Break"
        case .school: return "School"
        case .tuition: return "Tuition"
        case .sleep: return "Sleep"
        }
    }
}

struct WeekGridView: View {
    let timetable: Timetable
    let activities: [Activity]
    let onTapSlot: (Int, Int) -> Void
    let onTapActivity: (Activity) -> Void
    
    private let hourRange = 6..<22
    private let dayNames = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
    private let dayNumbers = [1, 2, 3, 4, 5, 6, 7]
    
    var body: some View {
        ScrollView([.horizontal, .vertical]) {
            LazyVStack(alignment: .leading, spacing: 0) {
                HStack(alignment: .top, spacing: 0) {
                    Text("Time")
                        .frame(width: 50, alignment: .leading)
                        .font(.caption2)
                    ForEach(dayNumbers, id: \.self) { day in
                        Text(dayNames[day - 1])
                            .frame(width: 70, alignment: .center)
                            .font(.caption2)
                    }
                }
                .padding(.horizontal, 8)
                .padding(.bottom, 4)
                
                ForEach(Array(hourRange), id: \.self) { hour in
                    HStack(alignment: .top, spacing: 0) {
                        Text(String(format: "%d:00", hour))
                            .frame(width: 50, alignment: .leading)
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                        ForEach(dayNumbers, id: \.self) { day in
                            CellView(
                                hour: hour,
                                day: day,
                                activities: activities.filter { a in
                                    a.dayOfWeek == day && a.startTimeMinutes < (hour + 1) * 60 && a.endTimeMinutes > hour * 60
                                },
                                onTapEmpty: { onTapSlot(day, hour * 60) },
                                onTapActivity: onTapActivity
                            )
                            .frame(width: 70, height: 44)
                        }
                    }
                }
            }
        }
    }
}

struct CellView: View {
    let hour: Int
    let day: Int
    let activities: [Activity]
    let onTapEmpty: () -> Void
    let onTapActivity: (Activity) -> Void
    
    var body: some View {
        Group {
            if activities.isEmpty {
                Button(action: onTapEmpty) {
                    RoundedRectangle(cornerRadius: 4)
                        .strokeBorder(Color.gray.opacity(0.3), lineWidth: 1)
                        .background(RoundedRectangle(cornerRadius: 4).fill(Color.clear))
                }
                .buttonStyle(.plain)
            } else {
                ForEach(activities.prefix(1), id: \.id) { act in
                    Button {
                        onTapActivity(act)
                    } label: {
                        RoundedRectangle(cornerRadius: 4)
                            .fill(colorForType(act.type))
                            .overlay(
                                Text(act.title)
                                    .font(.caption2)
                                    .lineLimit(2)
                                    .multilineTextAlignment(.center)
                                    .padding(2)
                            )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
    
    private func colorForType(_ type: ActivityType) -> Color {
        switch type {
        case .study: return .blue.opacity(0.3)
        case .breakType: return .green.opacity(0.3)
        case .school: return .orange.opacity(0.3)
        case .tuition: return .orange.opacity(0.4)
        case .sleep: return .indigo.opacity(0.3)
        }
    }
}

struct DayView: View {
    let activities: [Activity]
    @Binding var selectedDay: Int
    let onTapSlot: () -> Void
    let onTapActivity: (Activity) -> Void
    
    private let dayNames = ["", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Picker("Day", selection: $selectedDay) {
                    ForEach([1, 2, 3, 4, 5, 6, 7], id: \.self) { d in
                        Text(dayNames[d]).tag(d)
                    }
                }
                .pickerStyle(.menu)
            }
            .padding(.horizontal)
            
            if activities.isEmpty {
                Button(action: onTapSlot) {
                    Text("Add activity")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.gray.opacity(0.2))
                        .cornerRadius(8)
                }
                .padding(.horizontal)
            } else {
                List {
                    ForEach(activities.sorted(by: { $0.startTimeMinutes < $1.startTimeMinutes }), id: \.id) { act in
                        Button {
                            onTapActivity(act)
                        } label: {
                            HStack {
                                Text(timeString(act.startTimeMinutes))
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                    .frame(width: 50, alignment: .leading)
                                VStack(alignment: .leading) {
                                    Text(act.title)
                                        .font(.headline)
                                    if let note = act.note, !note.isEmpty {
                                        Text(note)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                }
                                Spacer()
                            }
                            .padding(.vertical, 4)
                        }
                    }
                }
            }
        }
    }
    
    private func timeString(_ minutes: Int) -> String {
        let h = minutes / 60
        let m = minutes % 60
        return String(format: "%d:%02d", h, m)
    }
}

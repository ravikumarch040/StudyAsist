//
//  QABankListView.swift
//  StudyAsist
//

import SwiftUI
import SwiftData

struct QABankListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \QA.createdAt, order: .reverse) private var allQA: [QA]
    @State private var filterSubject: String?
    @State private var filterChapter: String?
    @State private var showingAddSheet = false
    @State private var qaToEdit: QA?
    
    private var subjects: [String] {
        Array(Set(allQA.compactMap(\.subject).filter { !$0.isEmpty })).sorted()
    }
    
    private var chapters: [String] {
        Array(Set(allQA.compactMap(\.chapter).filter { !$0.isEmpty })).sorted()
    }
    
    private var filteredQA: [QA] {
        var result = allQA
        if let s = filterSubject, !s.isEmpty {
            result = result.filter { $0.subject == s }
        }
        if let c = filterChapter, !c.isEmpty {
            result = result.filter { $0.chapter == c }
        }
        return result
    }
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack {
                        FilterChip(title: "All", selected: filterSubject == nil) {
                            filterSubject = nil
                            filterChapter = nil
                        }
                        ForEach(subjects, id: \.self) { s in
                            FilterChip(title: s, selected: filterSubject == s) {
                                filterSubject = s
                                filterChapter = nil
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical, 8)
                .background(Color(.systemGroupedBackground))
                
                List {
                    ForEach(filteredQA, id: \.id) { qa in
                        QARowView(qa: qa)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                qaToEdit = qa
                            }
                            .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                Button(role: .destructive) {
                                    modelContext.delete(qa)
                                    try? modelContext.save()
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                    }
                }
            }
            .navigationTitle("Q&A Bank")
            .overlay {
                if filteredQA.isEmpty {
                    ContentUnavailableView(
                        "No questions",
                        systemImage: "doc.text.magnifyingglass",
                        description: Text("Add questions manually or scan from a photo")
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
                QAEditView()
            }
            .sheet(item: $qaToEdit) { qa in
                QAEditView(qa: qa)
            }
        }
    }
}

struct QARowView: View {
    let qa: QA
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(qa.questionText)
                .font(.subheadline)
                .lineLimit(2)
            if let sub = qa.subject {
                Text("\(sub)" + (qa.chapter.map { " · \($0)" } ?? ""))
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}

struct FilterChip: View {
    let title: String
    let selected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(selected ? Color.accentColor : Color(.systemGray5))
                .foregroundColor(selected ? .white : .primary)
                .cornerRadius(16)
        }
        .buttonStyle(.plain)
    }
}

extension QA: @retroactive Identifiable {}

# StudyAsist – UI Wireframes (Text)

## 1. Timetable List

```
┌─────────────────────────────────────────┐
│  ☰  StudyAsist                    [⚙]  │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ ICSE Class 5 Exam Timetable         ││
│  │ Mon–Sun · 12 activities             ││
│  │                    [⋮] Duplicate…   ││
│  └─────────────────────────────────────┘│
│  ┌─────────────────────────────────────┐│
│  │ Regular Week                        ││
│  │ Mon–Sun · 8 activities              ││
│  └─────────────────────────────────────┘│
│                                         │
│                                    (+)  │  ← FAB: New timetable
└─────────────────────────────────────────┘
```

- Tap row → Timetable detail.
- Long-press or [⋮] → Duplicate, Delete, Export Excel.

---

## 2. Timetable Detail (Week View)

```
┌─────────────────────────────────────────┐
│  ←  ICSE Class 5 Exam          [⋮]      │
├─────────────────────────────────────────┤
│  [Day]  [Week]   Filter: [All ▾]        │
├──────┬──────┬──────┬──────┬──────┬──────┤
│ Time │ Mon  │ Tue  │ Wed  │ Thu  │ ...  │
├──────┼──────┼──────┼──────┼──────┼──────┤
│ 6:00 │ Maths│      │ Sci  │      │      │
│ 7:00 │      │ Engl │      │ Maths│      │
│ 8:00 │ Break│      │      │      │      │
│ ...  │      │      │      │      │      │
└──────┴──────┴──────┴──────┴──────┴──────┘
│  [Export Excel] [Print] [Share]         │
│                                    (+)  │  ← Add activity
└─────────────────────────────────────────┘
```

- Tap empty slot → Add activity (day preselected).
- Tap filled slot → Edit activity.
- Overflow [⋮]: Duplicate timetable, Edit name, Export, Print, Share.

---

## 3. Timetable Detail (Day View)

```
┌─────────────────────────────────────────┐
│  ←  ICSE Class 5 Exam          [⋮]      │
├─────────────────────────────────────────┤
│  [Day]  [Week]   Mon 15 Jan  [<] [>]    │
├─────────────────────────────────────────┤
│  6:00  ┌─────────────────────────────┐  │
│        │ Maths · Chapter: Fractions   │  │
│  7:00  └─────────────────────────────┘  │
│  7:00  ┌─────────────────────────────┐  │
│        │ Break                        │  │
│  7:30  └─────────────────────────────┘  │
│  ...                                     │
└─────────────────────────────────────────┘
```

- [<] [>] previous/next day. Tap slot to add/edit.

---

## 4. Activity Add/Edit

```
┌─────────────────────────────────────────┐
│  ←  Add activity                        │
├─────────────────────────────────────────┤
│  Day of week      [Monday        ▾]     │
│  Start time       [06 : 00       ]     │
│  End time         [07 : 00       ]     │
│  Title            [Maths             ]  │
│  Type             [Study          ▾]   │
│  Note (optional)  [Ch. Fractions  ]     │
│  ─────────────────────────────────────  │
│  Notify me        [====●──] ON         │
│  Notify before    [5 min         ▾]     │
│  ─────────────────────────────────────  │
│  ⚠ Another activity overlaps this slot │
│     (Lunch 12:00–12:30). Save anyway?   │
│                                         │
│  [Cancel]              [Save]           │
└─────────────────────────────────────────┘
```

---

## 5. Settings

```
┌─────────────────────────────────────────┐
│  ←  Settings                            │
├─────────────────────────────────────────┤
│  Notifications                          │
│  Default reminder     [5 min before ▾]  │
│  Sound                [====●──] ON      │
│  Vibration            [====●──] ON      │
│  Channel importance   [Default    ▾]    │
│  ─────────────────────────────────────  │
│  Export (optional)                       │
│  Save exports to      [Documents/…  ]   │
└─────────────────────────────────────────┘
```

---

## Navigation Flow

```
TimetableList ──► TimetableDetail ──► ActivityEdit (add/edit)
       │                   │
       │                   ├──► Export → Share sheet
       │                   ├──► Print  → Print dialog
       │                   └──► Duplicate → back to List
       │
       └──► Settings (from app bar or drawer)
```

Use these as reference for Compose layout (Column/Row, LazyVerticalGrid for week, time slots as cards or list items).

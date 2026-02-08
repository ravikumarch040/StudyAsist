# StudyAsist – Design Summary (Quick Reference)

## Tech Stack (Proposed)

| Item | Choice |
|------|--------|
| Language | **Kotlin** |
| UI | **Jetpack Compose** + Material 3 |
| Architecture | **MVVM** |
| Local DB | **Room** (SQLite) |
| Settings | **DataStore** (Preferences) |
| DI | **Hilt** |
| Min / Target SDK | **24 / 34** |
| Excel export | **Apache POI** (.xlsx) |
| Print | **PrintManager** + **PdfDocument** (system print/PDF) |
| Notifications | **AlarmManager** (exact) + **NotificationManager** + channels |

## Screens (5 main)

1. **Timetable list** – All timetables, FAB add, duplicate/delete/export from list.
2. **Timetable detail** – Day | Week view, filter by type, Export / Print / Share / Duplicate.
3. **Activity add/edit** – Form (day, time, title, type, note, notification), overlap warning.
4. **Settings** – Default lead time, sound, vibration, channel importance.
5. **Dialogs** – Delete confirm, overlap warning, export success + share.

## Data (Core)

- **Timetable:** id, name, weekType (Mon–Sun | Mon–Sat+Sunday), startDate?, createdAt, updatedAt.
- **Activity:** id, timetableId, dayOfWeek, start/end (minutes from midnight), title, type, note, notifyEnabled, notifyLeadMinutes.
- **Settings:** defaultLeadMinutes, soundEnabled, vibrationEnabled (DataStore).

## Flows

- **Export:** Build grid → POI .xlsx → save in app Documents → open Share sheet.
- **Print:** Build same grid → PdfDocument → PrintManager → user picks printer or “Save as PDF”.
- **Notify:** Per activity with notify on → AlarmManager weekly at (start − lead); notification with sound/vibration from Settings; tap opens app to that day/activity.

## Decisions to Confirm

- Overlap: **Warn** (allow save) vs block.
- Export: **.xlsx only** (POI) vs CSV/other.
- “Mon–Sat + Sunday”: treat as **same 7-day grid** with Sunday as 7th column.

Once you finalize these and the stack above, we can start implementation in the order given in `DESIGN.md` §10.

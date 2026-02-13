package com.studyasist.data.repository

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.TimetableDao
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekType
import com.studyasist.util.formatTimeMinutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(
    private val timetableDao: TimetableDao,
    private val activityDao: ActivityDao
) {

    fun getAllTimetables(): Flow<List<TimetableEntity>> = timetableDao.getAll()

    fun getTimetableFlow(id: Long): Flow<TimetableEntity?> = timetableDao.getByIdFlow(id)

    suspend fun getTimetable(id: Long): TimetableEntity? = timetableDao.getById(id)

    suspend fun createTimetable(name: String, weekType: WeekType, startDate: Long? = null): Long {
        val now = System.currentTimeMillis()
        val entity = TimetableEntity(
            name = name,
            weekType = weekType,
            startDate = startDate,
            createdAt = now,
            updatedAt = now
        )
        return timetableDao.insert(entity)
    }

    suspend fun updateTimetable(entity: TimetableEntity) {
        timetableDao.update(entity.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTimetable(id: Long) {
        activityDao.deleteByTimetableId(id)
        timetableDao.deleteById(id)
    }

    suspend fun duplicateTimetable(sourceId: Long, newName: String): Long {
        val source = timetableDao.getById(sourceId) ?: return 0L
        val activities = activityDao.getAllForTimetable(sourceId).first()
        val now = System.currentTimeMillis()
        val newEntity = TimetableEntity(
            name = newName,
            weekType = source.weekType,
            startDate = source.startDate,
            createdAt = now,
            updatedAt = now
        )
        val newId = timetableDao.insert(newEntity)
        activities.forEach { act ->
            activityDao.insert(
                act.copy(id = 0, timetableId = newId)
            )
        }
        return newId
    }

    /**
     * Returns CSV content of a timetable for export.
     * Columns: Day,Start,End,Title,Type,Note,Notify
     */
    suspend fun getExportCsv(timetableId: Long): String {
        val timetable = timetableDao.getById(timetableId) ?: return ""
        val activities = activityDao.getAllForTimetableOnce(timetableId)
        return buildTimetableExportCsv(timetable.name, activities)
    }

    /**
     * Generates a PDF of a timetable.
     */
    suspend fun getExportPdf(timetableId: Long): ByteArray {
        val timetable = timetableDao.getById(timetableId) ?: return ByteArray(0)
        val activities = activityDao.getAllForTimetableOnce(timetableId)
        return buildTimetablePdf(timetable.name, activities)
    }

    /**
     * Generates Excel-compatible XML for a timetable.
     */
    suspend fun getExportExcel(timetableId: Long): ByteArray {
        val timetable = timetableDao.getById(timetableId) ?: return ByteArray(0)
        val activities = activityDao.getAllForTimetableOnce(timetableId)
        return buildTimetableExcel(timetable.name, activities)
    }

    private fun buildTimetableExportCsv(timetableName: String, activities: List<ActivityEntity>): String {
        val dayNames = listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val header = "Timetable: $timetableName\nDay,Start,End,Title,Type,Note,Notify"
        val lines = mutableListOf(header)
        for (act in activities) {
            val day = if (act.dayOfWeek in 1..7) dayNames[act.dayOfWeek] else "Day${act.dayOfWeek}"
            lines.add("$day,${formatTimeMinutes(act.startTimeMinutes)},${formatTimeMinutes(act.endTimeMinutes)},${act.title.escapeCsv()},${act.type.name},${(act.note ?: "").escapeCsv()},${act.notifyEnabled}")
        }
        return lines.joinToString("\n")
    }

    private fun buildTimetablePdf(timetableName: String, activities: List<ActivityEntity>): ByteArray {
        val dayNames = listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40
        val lineHeight = 20
        val titlePaint = Paint().apply {
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val normalPaint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
        }
        val document = PdfDocument()
        var y = margin
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        canvas.drawText(timetableName, margin.toFloat(), y.toFloat(), titlePaint)
        y += lineHeight * 2
        canvas.drawText("Day | Start | End | Title | Type | Note", margin.toFloat(), y.toFloat(), headerPaint)
        y += lineHeight
        for (act in activities) {
            if (y > pageHeight - margin - lineHeight * 2) {
                document.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                y = margin
            }
            val day = if (act.dayOfWeek in 1..7) dayNames[act.dayOfWeek] else "D${act.dayOfWeek}"
            val note = act.note?.take(30)?.plus(if ((act.note?.length ?: 0) > 30) "…" else "") ?: ""
            val line = "$day | ${formatTimeMinutes(act.startTimeMinutes)} | ${formatTimeMinutes(act.endTimeMinutes)} | ${act.title.take(25)} | ${act.type.name} | $note"
            canvas.drawText(line, margin.toFloat(), y.toFloat(), normalPaint)
            y += lineHeight
        }
        document.finishPage(page)
        val output = java.io.ByteArrayOutputStream()
        document.writeTo(output)
        document.close()
        return output.toByteArray()
    }

    private fun buildTimetableExcel(timetableName: String, activities: List<ActivityEntity>): ByteArray {
        val dayNames = listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n")
        val sheetName = timetableName.replace(Regex("[:\\\\/?*\\[\\]]"), "").take(31).ifEmpty { "Timetable" }
        sb.append("  <Worksheet ss:Name=\"${sheetName.escapeXml()}\">\n")
        sb.append("    <Table>\n")
        sb.append("      <Row><Cell><Data ss:Type=\"String\">Day</Data></Cell><Cell><Data ss:Type=\"String\">Start</Data></Cell><Cell><Data ss:Type=\"String\">End</Data></Cell><Cell><Data ss:Type=\"String\">Title</Data></Cell><Cell><Data ss:Type=\"String\">Type</Data></Cell><Cell><Data ss:Type=\"String\">Note</Data></Cell><Cell><Data ss:Type=\"String\">Notify</Data></Cell></Row>\n")
        for (act in activities) {
            val day = if (act.dayOfWeek in 1..7) dayNames[act.dayOfWeek] else "Day${act.dayOfWeek}"
            val title = act.title.escapeXml()
            val note = (act.note ?: "").escapeXml()
            sb.append("      <Row><Cell><Data ss:Type=\"String\">$day</Data></Cell><Cell><Data ss:Type=\"String\">${formatTimeMinutes(act.startTimeMinutes)}</Data></Cell><Cell><Data ss:Type=\"String\">${formatTimeMinutes(act.endTimeMinutes)}</Data></Cell><Cell><Data ss:Type=\"String\">$title</Data></Cell><Cell><Data ss:Type=\"String\">${act.type.name}</Data></Cell><Cell><Data ss:Type=\"String\">$note</Data></Cell><Cell><Data ss:Type=\"String\">${act.notifyEnabled}</Data></Cell></Row>\n")
        }
        sb.append("    </Table>\n")
        sb.append("  </Worksheet>\n")
        sb.append("</Workbook>")
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun String.escapeCsv(): String =
        if (contains(',') || contains('"') || contains('\n')) "\"" + replace("\"", "\"\"") + "\""
        else this

    private fun String.escapeXml(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

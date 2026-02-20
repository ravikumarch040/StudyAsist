package com.studyasist.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Converts PDF bytes to a PNG file and starts the system share sheet.
 * Renders the first page at 2x scale for display quality.
 * Must be called from a coroutine.
 */
suspend fun sharePdfAsImage(context: Context, pdfBytes: ByteArray, filePrefix: String, chooserTitle: String) = withContext(Dispatchers.IO) {
    if (pdfBytes.isEmpty()) return@withContext
    val cacheDir = context.cacheDir
    val tempPdf = File(cacheDir, "${filePrefix}_${System.currentTimeMillis()}.pdf")
    val tempPng = File(cacheDir, "${filePrefix}_${System.currentTimeMillis()}.png")
    try {
        tempPdf.writeBytes(pdfBytes)
        val pfd = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY)
        try {
            PdfRenderer(pfd).use { renderer ->
                if (renderer.pageCount == 0) return@withContext
                val page = renderer.openPage(0)
                val scale = 2f
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                java.io.FileOutputStream(tempPng).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        } finally {
            pfd.close()
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempPng)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        withContext(Dispatchers.Main) {
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        }
    } finally {
        tempPdf.delete()
    }
}

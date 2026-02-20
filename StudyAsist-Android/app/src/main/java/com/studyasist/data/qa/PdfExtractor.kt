package com.studyasist.data.qa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts text from PDF files using Android's PdfRenderer.
 * Renders each page to a bitmap for OCR processing.
 */
@Singleton
class PdfExtractor @Inject constructor() {

    data class PdfPage(
        val pageIndex: Int,
        val bitmap: Bitmap
    )

    /**
     * Extract page bitmaps from a PDF file.
     * The caller is responsible for recycling the bitmaps.
     */
    fun extractPages(context: Context, uri: Uri): List<PdfPage> {
        val pages = mutableListOf<PdfPage>()
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return emptyList()
        try {
            val renderer = PdfRenderer(pfd)
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2, page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pages.add(PdfPage(i, bitmap))
                page.close()
            }
            renderer.close()
        } finally {
            pfd.close()
        }
        return pages
    }

    fun getPageCount(context: Context, uri: Uri): Int {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return 0
        return try {
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            renderer.close()
            count
        } finally {
            pfd.close()
        }
    }
}

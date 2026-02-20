package com.studyasist.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume

private const val MAX_IMAGE_DIMENSION = 1024

private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
    var inSampleSize = 1
    if (width > maxDim || height > maxDim) {
        val halfW = width / 2
        val halfH = height / 2
        while (halfW / inSampleSize >= maxDim && halfH / inSampleSize >= maxDim) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/**
 * Extracts text from an image URI using ML Kit Text Recognition.
 * Reads the URI once into memory then decodes with inSampleSize when large to avoid OOM.
 * (Opening the same content URI twice can fail on some gallery providers.)
 */
suspend fun extractTextFromImage(context: Context, imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
    try {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { input ->
            input.readBytes()
        } ?: return@withContext Result.failure(IOException("Could not open image"))
        if (bytes.isEmpty()) return@withContext Result.failure(IOException("Empty image"))
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
        val sampleSize = calculateInSampleSize(
            boundsOptions.outWidth,
            boundsOptions.outHeight,
            MAX_IMAGE_DIMENSION
        )
        val bitmap = BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize }
        ) ?: return@withContext Result.failure(IOException("Could not decode image"))
        val image = InputImage.fromBitmap(bitmap, 0)
        suspendCancellableCoroutine { cont ->
            val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val text = result.text?.trim() ?: ""
                    recognizer.close()
                    cont.resume(Result.success(text))
                }
                .addOnFailureListener { e ->
                    recognizer.close()
                    cont.resume(Result.failure(e))
                }
            cont.invokeOnCancellation { recognizer.close() }
        }
    } catch (e: IOException) {
        Result.failure(e)
    }
}

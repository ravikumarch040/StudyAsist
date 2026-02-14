package com.studyasist.util

import android.content.Context
import com.studyasist.R
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Wraps Android SpeechRecognizer for voice-to-text.
 * Call startListening() then use the callback for results.
 */
class SpeechToTextHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(
        locale: Locale = Locale.getDefault(),
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError(context.getString(R.string.err_speech_not_available))
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> context.getString(R.string.err_speech_audio)
                        SpeechRecognizer.ERROR_CLIENT -> context.getString(R.string.err_speech_client)
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> context.getString(R.string.err_speech_permission)
                        SpeechRecognizer.ERROR_NETWORK -> context.getString(R.string.err_speech_network)
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> context.getString(R.string.err_speech_network_timeout)
                        SpeechRecognizer.ERROR_NO_MATCH -> context.getString(R.string.err_speech_no_match)
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> context.getString(R.string.err_speech_busy)
                        SpeechRecognizer.ERROR_SERVER -> context.getString(R.string.err_speech_server)
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> context.getString(R.string.err_speech_timeout)
                        else -> context.getString(R.string.err_speech_generic, error)
                    }
                    onError(message)
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim()
                    if (!text.isNullOrBlank()) {
                        onResult(text)
                    } else {
                        onError(context.getString(R.string.err_speech_no_detected))
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

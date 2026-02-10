package com.studyasist.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

private object TtsHolder {
    @Volatile var tts: TextToSpeech? = null
    @Volatile var onDone: (() -> Unit)? = null
}

/**
 * One-shot TTS: speaks the given text with optional voice and locale.
 * Call from main thread. Use stopSpeaking() to stop before it finishes.
 */
fun speakText(
    context: Context,
    text: String,
    locale: Locale = Locale.getDefault(),
    voiceName: String? = null,
    onDone: (() -> Unit)? = null
) {
    if (text.isBlank()) {
        onDone?.invoke()
        return
    }
    stopSpeaking()
    var ttsRef: TextToSpeech? = null
    ttsRef = TextToSpeech(context) { status ->
        val tts = ttsRef ?: return@TextToSpeech
        if (status == TextToSpeech.SUCCESS) {
            TtsHolder.tts = tts
            TtsHolder.onDone = onDone
            tts.setLanguage(locale)
            voiceName?.let { name -> findVoiceByName(tts.voices, name)?.let { v -> tts.setVoice(v) } }
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    clearAndInvoke()
                    tts.stop()
                    tts.shutdown()
                }
                override fun onError(utteranceId: String?) {
                    clearAndInvoke()
                    tts.shutdown()
                }
            })
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_one_shot")
        } else {
            tts.shutdown()
            onDone?.invoke()
        }
    }
}

private fun clearAndInvoke() {
    val cb = TtsHolder.onDone
    TtsHolder.tts = null
    TtsHolder.onDone = null
    cb?.invoke()
}

/**
 * Stops any ongoing TTS started by speakText(). Safe to call from any thread.
 */
fun stopSpeaking() {
    val tts = TtsHolder.tts
    val cb = TtsHolder.onDone
    TtsHolder.tts = null
    TtsHolder.onDone = null
    tts?.stop()
    cb?.invoke()
    tts?.shutdown()
}

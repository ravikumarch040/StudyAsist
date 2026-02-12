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

/**
 * Splits text into sentences and speaks them one by one, calling [onSentenceProgress]
 * with the current sentence index (0-based) after each sentence finishes.
 * Use for Dictate highlight feature. Call stopSpeaking() to cancel.
 */
fun speakTextWithProgress(
    context: Context,
    text: String,
    locale: Locale = Locale.getDefault(),
    voiceName: String? = null,
    onSentenceProgress: (Int, Int) -> Unit,
    onDone: (() -> Unit)? = null
) {
    if (text.isBlank()) {
        onDone?.invoke()
        return
    }
    stopSpeaking()
    val sentences = splitIntoSentences(text.trim())
    if (sentences.isEmpty()) {
        onDone?.invoke()
        return
    }
    val total = sentences.size
    var spokenCount = 0

    var ttsRef: TextToSpeech? = null
    ttsRef = TextToSpeech(context) { status ->
        val tts = ttsRef ?: return@TextToSpeech
        if (status != TextToSpeech.SUCCESS) {
            tts.shutdown()
            onDone?.invoke()
            return@TextToSpeech
        }
        TtsHolder.tts = tts
        TtsHolder.onDone = onDone
        tts.setLanguage(locale)
        voiceName?.let { name -> findVoiceByName(tts.voices, name)?.let { v -> tts.setVoice(v) } }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val idx = utteranceId?.removePrefix("sent_")?.toIntOrNull() ?: spokenCount
                onSentenceProgress(idx, total)
            }
            override fun onDone(utteranceId: String?) {
                spokenCount++
                if (spokenCount >= total) {
                    clearAndInvoke()
                    tts.stop()
                    tts.shutdown()
                }
            }
            override fun onError(utteranceId: String?) {
                spokenCount++
                if (spokenCount >= total) {
                    clearAndInvoke()
                    tts.shutdown()
                }
            }
        })
        for (i in sentences.indices) {
            val s = sentences[i].trim()
            if (s.isNotEmpty()) {
                tts.speak(s, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "sent_$i")
            }
        }
    }
}

private fun splitIntoSentences(text: String): List<String> {
    if (text.isBlank()) return emptyList()
    return text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
}

private fun clearAndInvoke() {
    val cb = TtsHolder.onDone
    TtsHolder.tts = null
    TtsHolder.onDone = null
    cb?.invoke()
}

/**
 * Stops any ongoing TTS started by speakText() or speakTextWithProgress().
 * Safe to call from any thread.
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

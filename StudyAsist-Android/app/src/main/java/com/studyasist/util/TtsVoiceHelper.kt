package com.studyasist.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class VoiceOption(val voiceName: String, val displayName: String)

/**
 * Loads available TTS voices by initializing TextToSpeech and querying getVoices().
 * Call from Main thread or use from a coroutine with Main dispatcher.
 */
suspend fun loadAvailableVoices(context: Context): List<VoiceOption> = loadAvailableVoicesFiltered(context) { true }

/**
 * Loads TTS voices for India only (locale country = IN).
 * Use in Settings for alarm/speech voice selection.
 */
suspend fun loadAvailableVoicesIndia(context: Context): List<VoiceOption> = loadAvailableVoicesFiltered(context) { voice ->
    voice.locale?.country?.equals("IN", ignoreCase = true) == true
}

private suspend fun loadAvailableVoicesFiltered(
    context: Context,
    filter: (android.speech.tts.Voice) -> Boolean
): List<VoiceOption> = suspendCancellableCoroutine { cont ->
    var ttsRef: TextToSpeech? = null
    ttsRef = TextToSpeech(context) { status ->
        val tts = ttsRef ?: return@TextToSpeech
        if (status == TextToSpeech.SUCCESS) {
            val voices = tts.voices
                .filter { voice -> voice.locale != null && filter(voice) }
                .sortedWith(compareBy({ it.locale!!.language }, { it.name }))
                .map { voice -> VoiceOption(voice.name, "${voice.locale?.displayName ?: voice.locale?.language ?: "?"} - ${voice.name}") }
            tts.shutdown()
            cont.resume(voices)
        } else {
            tts.shutdown()
            cont.resume(emptyList())
        }
    }
    cont.invokeOnCancellation { ttsRef?.shutdown() }
}

/**
 * Finds a Voice from the given set by name, or null if not found.
 */
fun findVoiceByName(voices: Set<Voice>, name: String): Voice? =
    voices.firstOrNull { it.name == name }

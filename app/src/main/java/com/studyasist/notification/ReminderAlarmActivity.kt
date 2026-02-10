package com.studyasist.notification

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.studyasist.R
import com.studyasist.ui.theme.StudyAsistTheme
import com.studyasist.util.findVoiceByName
import java.util.Locale

/**
 * Full-screen alarm screen: plays custom TTS message or system alarm sound in a loop until the user taps Dismiss.
 */
class ReminderAlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var dismissed = false
    private var ttsMessageToLoop: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLockedAndTurnScreenOn()
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""
        val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, 0L)
        val soundEnabled = intent.getBooleanExtra(EXTRA_SOUND_ENABLED, true)
        val alarmTtsMessage = intent.getStringExtra(EXTRA_ALARM_TTS_MESSAGE)?.trim() ?: ""
        val fromTtsService = intent.getBooleanExtra(EXTRA_FROM_TTS_SERVICE, false)
        val notificationId = activityId.toInt().and(0x7FFFFFFF)
        if (!fromTtsService) NotificationManagerCompat.from(this).cancel(notificationId)

        setContent {
            StudyAsistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    ReminderAlarmScreen(
                        title = title,
                        body = body,
                        onDismiss = { finishAndStopSound() }
                    )
                }
            }
        }
        val ttsVoiceName = intent.getStringExtra(EXTRA_TTS_VOICE_NAME)?.takeIf { it.isNotEmpty() }
        if (soundEnabled && !fromTtsService) {
            if (alarmTtsMessage.isNotBlank()) {
                ttsMessageToLoop = alarmTtsMessage
                startTtsAlarm(alarmTtsMessage, ttsVoiceName)
            } else {
                startAlarmSound()
            }
        }
    }

    private fun setShowWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun startTtsAlarm(message: String, voiceName: String?) {
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS && !dismissed) {
                textToSpeech?.apply {
                    setLanguage(Locale.getDefault())
                    voiceName?.let { name -> findVoiceByName(voices, name)?.let { setVoice(it) } }
                    setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            if (!dismissed && !isFinishing && utteranceId == UTTERANCE_ID) {
                                mainHandler.postDelayed({ speakTtsMessage() }, 500)
                            }
                        }
                        override fun onError(utteranceId: String?) {}
                    })
                    speakTtsMessage()
                }
            }
        }
    }

    private fun speakTtsMessage() {
        val message = ttsMessageToLoop ?: return
        if (dismissed || isFinishing) return
        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            UTTERANCE_ID
        )
    }

    private fun startAlarmSound() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        if (uri == null) return
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun finishAndStopSound() {
        dismissed = true
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } finally {
            mediaPlayer = null
        }
        try {
            textToSpeech?.apply {
                stop()
                shutdown()
            }
        } finally {
            textToSpeech = null
        }
        if (intent.getBooleanExtra(EXTRA_FROM_TTS_SERVICE, false)) {
            stopService(Intent(this, AlarmTtsService::class.java))
        }
        finish()
    }

    companion object {
        private const val UTTERANCE_ID = "study_alarm_tts"
    }

    override fun onDestroy() {
        dismissed = true
        mediaPlayer?.apply { try { if (isPlaying) stop(); release() } catch (_: Exception) {} }
        mediaPlayer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in API 33")
    override fun onBackPressed() {
        finishAndStopSound()
    }
}

@Composable
private fun ReminderAlarmScreen(
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = context.getString(R.string.study_reminder),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (body.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.dismiss))
        }
    }
}

package com.studyasist.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import com.studyasist.R
import com.studyasist.util.findVoiceByName
import java.util.Locale

/**
 * Foreground service that plays the custom TTS message in a loop as soon as the alarm fires,
 * so the user hears speech immediately without tapping the notification.
 */
class AlarmTtsService : Service() {

    private var textToSpeech: TextToSpeech? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var stopped = false
    private var ttsMessage: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISMISS_ALARM_TTS -> {
                stopTtsAndSelf()
                return START_NOT_STICKY
            }
        }
        val message = intent?.getStringExtra(EXTRA_ALARM_TTS_MESSAGE)?.trim() ?: return START_NOT_STICKY
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""
        val timetableName = intent.getStringExtra(EXTRA_TIMETABLE_NAME) ?: getString(R.string.app_name)
        val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, 0L)
        val voiceName = intent.getStringExtra(EXTRA_TTS_VOICE_NAME)?.takeIf { it.isNotEmpty() }
        val notificationId = activityId.toInt().and(0x7FFFFFFF)

        createChannelIfNeeded()
        val activityIntent = Intent(this, ReminderAlarmActivity::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_TIMETABLE_NAME, timetableName)
            putExtra(EXTRA_ACTIVITY_ID, activityId)
            putExtra(EXTRA_ALARM_TTS_MESSAGE, message)
            putExtra(EXTRA_SOUND_ENABLED, true)
            putExtra(EXTRA_FROM_TTS_SERVICE, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentPending = PendingIntent.getActivity(
            this, notificationId, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissIntent = Intent(this, AlarmTtsService::class.java).apply {
            action = ACTION_DISMISS_ALARM_TTS
        }
        val dismissPending = PendingIntent.getService(
            this, notificationId + 1, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val contentText = getString(R.string.reminder_title_body_format, title, body)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALARM_TTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(timetableName)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(contentPending)
            .setFullScreenIntent(contentPending, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.dismiss), dismissPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(notificationId, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(notificationId, notification)
        }

        ttsMessage = message
        startTts(voiceName)
        return START_NOT_STICKY
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID_ALARM_TTS,
            getString(R.string.channel_study_alarm_tts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_alarm_tts_description)
            setSound(null, null)
            enableVibration(true)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun startTts(voiceName: String?) {
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS && !stopped) {
                textToSpeech?.apply {
                    setLanguage(Locale.getDefault())
                    voiceName?.let { name -> findVoiceByName(voices, name)?.let { setVoice(it) } }
                    setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            if (!stopped && utteranceId == UTTERANCE_ID) {
                                mainHandler.postDelayed({ speakMessage() }, 500)
                            }
                        }
                        override fun onError(utteranceId: String?) {}
                    })
                    speakMessage()
                }
            }
        }
    }

    private fun speakMessage() {
        if (stopped || ttsMessage.isEmpty()) return
        textToSpeech?.speak(ttsMessage, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    private fun stopTtsAndSelf() {
        stopped = true
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } finally {
            textToSpeech = null
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopTtsAndSelf()
        super.onDestroy()
    }

    companion object {
        private const val UTTERANCE_ID = "alarm_tts_service"
    }
}

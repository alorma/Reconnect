package com.alorma.reconnect

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager
import android.view.KeyEvent
import com.alorma.reconnect.Constants.VOLUME_MAX_PROGRESS
import extensions.preferences

class ReconnectListenerService : Service() {
    private var receiver: BroadcastReceiver? = null
    private var handler: Handler? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.isMusicActive && isEnabled) {
            handler = Handler()
            registerHeadset()
        }
        return Service.START_STICKY
    }

    private val isEnabled: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.ENABLED, false)

    private fun registerHeadset() {
        val intentFilter = IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
        if (receiver == null) {
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    if (isHeadsetConnect(am)) {
                        delayPlay()
                        setVolume(am)
                    } else {
                        pause()
                    }
                }
            }
        }
        unregister()
        registerReceiver(receiver, intentFilter)
    }

    private fun isHeadsetConnect(am: AudioManager): Boolean {
        return am.isWiredHeadsetOn
    }

    private fun delayPlay() {
        handler!!.postDelayed({
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (isEnabled && am.isWiredHeadsetOn && !am.isMusicActive) {
                play()
                unregister()
                stopSelf()
            }
        }, 500)
    }

    private fun unregister() {
        if (receiver != null) {
            try {
                unregisterReceiver(receiver)
                receiver = null
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun pause() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        var eventTime = SystemClock.uptimeMillis() - 1
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0)
        am.dispatchMediaKeyEvent(downEvent)

        eventTime++
        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0)
        am.dispatchMediaKeyEvent(upEvent)
    }

    private fun play() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        var eventtime = SystemClock.uptimeMillis() - 1
        val downEvent = KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0)
        am.dispatchMediaKeyEvent(downEvent)

        eventtime++
        val upEvent = KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0)
        am.dispatchMediaKeyEvent(upEvent)
    }

    private fun setVolume(am: AudioManager) {
        if (isSetVolumeEnable(am)) {
            val streamType = AudioManager.STREAM_MUSIC
            val maxVolume = preferences().getInt(VOLUME_MAX_PROGRESS, am.getStreamMaxVolume(streamType))
            am.setStreamVolume(streamType, maxVolume, 0)
        }
    }

    private fun isSetVolumeEnable(am: AudioManager): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return !am.isVolumeFixed
        } else {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return !am.isVolumeFixed || notificationManager.isNotificationPolicyAccessGranted
        }
    }
}

package com.alorma.reconnect

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import android.widget.Switch
import extensions.preferences
import org.jetbrains.anko.startService

class MainActivity : AppCompatActivity() {

    private var switchReconnect: Switch? = null
    private var seekBarVolumeReconnect: SeekBar? = null

    private val VOLUME_MAX_PROGRESS: String = "VOLUME_MAX_LEVEL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getViews()
        setupSwitch()
        setupVolumeLevel()
    }

    private fun getViews() {
        switchReconnect = findViewById(R.id.reconnectSwitch) as Switch
        seekBarVolumeReconnect = findViewById(R.id.reconnectVolume) as SeekBar
    }

    private fun setupSwitch() {
        switchReconnect?.isChecked?.let { changeSeekBarState(it) }
        switchReconnect?.setOnCheckedChangeListener { _, isChecked ->
            onStatusChanged(isChecked)
            changeSeekBarState(isChecked)
        }
    }

    private fun changeSeekBarState(checked: Boolean) {
        seekBarVolumeReconnect?.isEnabled = checked
    }

    private fun onStatusChanged(isChecked: Boolean) {
        val editor = preferences().edit()
        editor.putBoolean(Constants.ENABLED, isChecked)
        editor.apply()
        if (isChecked) {
            startReconnectService()
            launchNotification()
        } else {
            dismissNotification()
        }
    }


    private fun setupVolumeLevel() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxSystemVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val maxSavedVolume = preferences().getInt(VOLUME_MAX_PROGRESS, currentVolume)

        configureSeekBarValues(maxSavedVolume, maxSystemVolume)
        configureSeekBarListener()
    }

    private fun configureSeekBarValues(maxSavedVolume: Int, maxSystemVolume: Int) {
        seekBarVolumeReconnect?.progress = maxSavedVolume
        seekBarVolumeReconnect?.max = maxSystemVolume
    }

    private fun configureSeekBarListener() {
        seekBarVolumeReconnect?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    savePrefVolume(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Just an empty method
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Another empty method
            }
        })
    }

    private fun savePrefVolume(progress: Int) {
        val editor = preferences().edit()
        editor.putInt(VOLUME_MAX_PROGRESS, progress)
        editor.apply()
    }

    private val isEnabled: Boolean
        get() = preferences().getBoolean(Constants.ENABLED, false)

    override fun onStart() {
        super.onStart()
        switchReconnect?.isChecked = isEnabled
        if (isEnabled) {
            launchNotification()
        }
    }

    private fun launchNotification() {
        val nm = NotificationManagerCompat.from(this)

        val builder = NotificationCompat.Builder(this)
        builder.setContentTitle("Reconnect listener")
        builder.setContentText("Touch to disable")
        val dismissBroadcast = Intent(this, DisableService::class.java)
        val intent = PendingIntent.getService(this, 0, dismissBroadcast, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentIntent(intent)
        nm.notify(Constants.NOTIFICATION_ID, builder.build())
    }

    private fun dismissNotification() {
        val nm = NotificationManagerCompat.from(this)
        nm.cancel(Constants.NOTIFICATION_ID)
    }

    private fun startReconnectService() {
        startService<ReconnectListenerService>()
    }
}

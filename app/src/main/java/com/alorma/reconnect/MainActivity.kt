package com.alorma.reconnect

import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Switch
import org.jetbrains.anko.startService

class MainActivity : AppCompatActivity() {

    private var preferences: SharedPreferences? = null
    private var switchReconnect: Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        switchReconnect = findViewById(R.id.reconnectSwitch) as Switch

        switchReconnect!!.setOnCheckedChangeListener { _, isChecked -> onStatusChanged(isChecked) }
    }

    private val isEnabled: Boolean
        get() = preferences!!.getBoolean(Constants.ENABLED, false)

    override fun onStart() {
        super.onStart()
        switchReconnect!!.isChecked = isEnabled
        if (isEnabled) {
            launchNotification()
        }
    }

    private fun onStatusChanged(isChecked: Boolean) {
        val editor = preferences!!.edit()
        editor.putBoolean(Constants.ENABLED, isChecked)
        editor.apply()
        if (isChecked) {
            startReconnectService()
            launchNotification()
        } else {
            dismissNotification()
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

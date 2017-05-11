package com.alorma.reconnect

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationManagerCompat

class DisableService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putBoolean(Constants.ENABLED, false)
        editor.apply()

        NotificationManagerCompat.from(this).cancel(Constants.NOTIFICATION_ID)

        return super.onStartCommand(intent, flags, startId)
    }
}

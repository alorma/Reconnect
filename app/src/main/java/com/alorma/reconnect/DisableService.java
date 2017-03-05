package com.alorma.reconnect;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

public class DisableService extends Service {
  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    editor.putBoolean(Constants.ENABLED, false);
    editor.apply();

    NotificationManagerCompat.from(this).cancel(Constants.NOTIFICATION_ID);

    return super.onStartCommand(intent, flags, startId);
  }
}

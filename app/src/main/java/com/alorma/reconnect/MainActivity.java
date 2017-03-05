package com.alorma.reconnect;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

  private SharedPreferences preferences;
  private Switch switchReconnect;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    preferences = PreferenceManager.getDefaultSharedPreferences(this);

    switchReconnect = (Switch) findViewById(R.id.reconnectSwitch);

    switchReconnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        onStatusChanged(isChecked);
      }
    });
  }

  private boolean isEnabled() {
    return preferences.getBoolean(Constants.ENABLED, false);
  }

  @Override
  protected void onStart() {
    super.onStart();
    switchReconnect.setChecked(isEnabled());
    if (isEnabled()) {
      launchNotification();
    }
  }

  private void onStatusChanged(boolean isChecked) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.putBoolean(Constants.ENABLED, isChecked);
    editor.apply();
    if (isChecked) {
      startService();
      launchNotification();
    } else {
      dismissNotification();
    }
  }

  private void launchNotification() {
    NotificationManagerCompat nm = NotificationManagerCompat.from(this);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    builder.setContentTitle("Reconnect listener");
    builder.setContentText("Touch to disable");
    Intent dismissBroadcast = new Intent(this, DisableService.class);
    PendingIntent intent = PendingIntent.getService(this, 0, dismissBroadcast, PendingIntent.FLAG_UPDATE_CURRENT);
    builder.setSmallIcon(R.mipmap.ic_launcher);
    builder.setContentIntent(intent);
    nm.notify(Constants.NOTIFICATION_ID, builder.build());
  }

  private void dismissNotification() {
    NotificationManagerCompat nm = NotificationManagerCompat.from(this);
    nm.cancel(Constants.NOTIFICATION_ID);
  }

  private void startService() {
    Intent intent = new Intent(this, ReconnectListenerService.class);
    startService(intent);
  }
}

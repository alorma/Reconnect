package com.alorma.reconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

  private SharedPreferences preferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    preferences = PreferenceManager.getDefaultSharedPreferences(this);

    Switch switchReconnect = (Switch) findViewById(R.id.reconnectSwitch);
    switchReconnect.setChecked(isEnabled());
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

  private void onStatusChanged(boolean isChecked) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.putBoolean(Constants.ENABLED, isChecked);
    editor.apply();
    if (isChecked) {
      Intent intent = new Intent(this, ReconnectListenerService.class);
      startService(intent);
    }
  }
}

package com.alorma.reconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReconnectReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Intent serviceIntent = new Intent(context, ReconnectListenerService.class);
    context.startService(serviceIntent);
  }
}

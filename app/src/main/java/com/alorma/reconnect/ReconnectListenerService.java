package com.alorma.reconnect;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

public class ReconnectListenerService extends Service {
  private AudioManager audioManager;
  private BroadcastReceiver receiver;
  private Handler handler;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    if (audioManager.isMusicActive() && isEnabled()) {
      handler = new Handler();
      registerHeadset();
    }
    return Service.START_STICKY;
  }

  private boolean isEnabled() {
    return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.ENABLED, false);
  }

  private void registerHeadset() {
    IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
    if (receiver == null) {
      receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (audioManager.isWiredHeadsetOn()) {
            delayPlay();
          } else {
            pause();
          }
        }
      };
    }
    unregister();
    registerReceiver(receiver, intentFilter);
  }

  private void delayPlay() {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (isEnabled() && audioManager.isWiredHeadsetOn() && !audioManager.isMusicActive()) {
          play();
          unregister();
          stopSelf();
        }
      }
    }, 500);
  }

  private void unregister() {
    if (receiver != null) {
      try {
        unregisterReceiver(receiver);
        receiver = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void pause() {
    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    long eventtime = SystemClock.uptimeMillis() - 1;
    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
    am.dispatchMediaKeyEvent(downEvent);

    eventtime++;
    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
    am.dispatchMediaKeyEvent(upEvent);
  }

  private void play() {
    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    long eventtime = SystemClock.uptimeMillis() - 1;
    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
    am.dispatchMediaKeyEvent(downEvent);

    eventtime++;
    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
    am.dispatchMediaKeyEvent(upEvent);
  }
}

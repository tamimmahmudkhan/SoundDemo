package com.example.igneel.sounddemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.SeekBar;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class AudioService extends Service
{
	private static final String TAG = "AudioService";
	MediaPlayer mediaPlayer;
	AudioManager audioManager;
	Timer progressTicker;
	MusicEventListener musicEventListener;
	Controller controller;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "onCreate: Starts");
		mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pirate);
		controller = new Controller();
		Log.d(TAG, "onCreate: Ends");
	}

	class ServiceProvider extends Binder {
		AudioService getService()
		{
			return AudioService.this;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return new ServiceProvider();
	}

	interface MusicEventListener
	{
		void onMusicProgress(int progress);
		void onVolumeChange(int volume);
	}

	public class Controller extends Binder
	{

		void setListener(MusicEventListener listener)
		{
			musicEventListener = listener;
		}

		void play()
		{
			mediaPlayer.start();
		}

		void pause()
		{
			mediaPlayer.pause();
		}

		void stop()
		{
			mediaPlayer.stop();
		}

		MediaPlayer getPlayer()
		{
			return mediaPlayer;
		}
	}
}

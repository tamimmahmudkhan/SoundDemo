package com.example.igneel.sounddemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.rtp.AudioStream;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AudioService.MusicEventListener
{
    private static final String TAG = "MainActivity";
    SeekBar volumeControl;
    SeekBar streamProgress;
    AudioService audioService;
    TextView nowPlayingText;
    ServiceConnection serviceConnection;
    AudioService.Controller controller;

    private static final String MEDIA_VOLUME = "mediaVolume";
    private static final String MEDIA_PROGRESS = "mediaProgress";
    private static final String MEDIA_PLAYER = "mediaPlayer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        streamProgress = findViewById(R.id.streamProgress);
        volumeControl = findViewById(R.id.volumeBar);
        nowPlayingText = findViewById(R.id.text_now_playing);

        Intent intent = new Intent(this, AudioService.class);

        startService(intent);
        serviceConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                Log.d(TAG, "onServiceConnected: Service binded");
                AudioService.ServiceProvider provider = (AudioService.ServiceProvider) service;
                audioService = provider.getService();

                new Timer().scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        streamProgress.setMax(audioService.mediaPlayer.getDuration());
                        streamProgress.setProgress(audioService.mediaPlayer.getCurrentPosition());
                        nowPlayingText.setText(getString(R.string.now_playing)+ " : " + streamProgress.getProgress() + " - " + streamProgress.getMax());
                    }
                }, 0, 500);

            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                Toast.makeText(MainActivity.this, "Unbound Service", Toast.LENGTH_SHORT).show();
            }
        };

        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeControl.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser)audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        Log.d(TAG, "onCreate: Ends");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unbindService(serviceConnection);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
	    Intent intent = new Intent(this, AudioService.class);
	    bindService(intent, serviceConnection, 0);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
//        Intent intent = new Intent(this, AudioService.class);
//        stopService(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(MEDIA_VOLUME, volumeControl.getProgress());
        outState.putInt(MEDIA_PROGRESS, streamProgress.getProgress());
        outState.putString(MEDIA_PLAYER, nowPlayingText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        streamProgress.setProgress(savedInstanceState.getInt(MEDIA_PROGRESS));
        volumeControl.setProgress(savedInstanceState.getInt(MEDIA_VOLUME));
        nowPlayingText.setText(savedInstanceState.getString(MEDIA_PLAYER));
    }

    public void play(View view)
    {
        Log.d(TAG, "play: Playing sweet tunes");
        audioService.mediaPlayer.start();
    }

    public void pause(View view)
    {
        audioService.mediaPlayer.pause();
    }
    
    public void stop(View view)
    {
        controller.stop();
    }

    @Override
    public void onMusicProgress(int progress)
    {
//        streamProgress.setProgress(controller.getProgress());
    }

    @Override
    public void onVolumeChange(int volume)
    {
        volumeControl.setProgress(volume);
    }
}

package com.arukai.uajpspeak.model;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import static com.arukai.uajpspeak.activity.MainActivity.context;

public class AudioPlayer {
    private static final String LOG_TAG = "PLAYER";
    private static MediaPlayer mMediaPlayer = null;
    private static SoundPool soundPool = null;
    private static int streamId;

    private static void stopMP() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.release();
            } catch (Exception e){
                Log.e(LOG_TAG, "reset/release failed");
            }
            mMediaPlayer = null;
        }
    }

    private static void stopSP() {
        if (soundPool != null) {
            try {
                soundPool.stop(streamId);
                soundPool.release();
            } catch (Exception e){
                Log.e(LOG_TAG, "stop/release failed");
            }
            soundPool = null;
        }
    }

    public static void play(Context c, int rid) {
        stopMP();
        stopSP();

        mMediaPlayer = MediaPlayer.create(c, rid);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopMP();
            }
        });

        try {
            mMediaPlayer.start();
        } catch (Exception e){
            Log.e(LOG_TAG, "start failed");
        }
    }

    public static void slowPlay(Context c, int rid) {
        stopMP();
        stopSP();

        final float playbackSpeed=0.75f;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);

        final int soundId = soundPool.load(c, rid, 1);
        AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final float volume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
        {
            @Override
            public void onLoadComplete(SoundPool arg0, int arg1, int arg2)
            {
                streamId = soundPool.play(soundId, volume, volume, 1, 0, playbackSpeed);
            }
        });
    }

}
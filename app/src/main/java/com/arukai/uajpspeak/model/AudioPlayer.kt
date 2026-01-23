package com.arukai.uajpspeak.model

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.arukai.uajpspeak.activity.MainActivity

object AudioPlayer {
    private const val LOG_TAG = "PLAYER"
    private var mMediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var streamId: Int = 0

    private fun stopMP() {
        mMediaPlayer?.let {
            try {
                it.reset()
                it.release()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "reset/release failed")
            }
            mMediaPlayer = null
        }
    }

    private fun stopSP() {
        soundPool?.let {
            try {
                it.stop(streamId)
                it.release()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "stop/release failed")
            }
            soundPool = null
        }
    }

    fun play(c: Context, rid: Int) {
        stopMP()
        stopSP()

        mMediaPlayer = MediaPlayer.create(c, rid)
        mMediaPlayer?.setOnCompletionListener {
            stopMP()
        }

        try {
            mMediaPlayer?.start()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "start failed")
        }
    }

    fun slowPlay(c: Context, rid: Int) {
        stopMP()
        stopSP()

        val playbackSpeed = 0.75f
        soundPool = SoundPool(4, AudioManager.STREAM_MUSIC, 100)

        val soundId = soundPool?.load(c, rid, 1) ?: return
        val mgr = MainActivity.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()

        soundPool?.setOnLoadCompleteListener { _, _, _ ->
            streamId = soundPool?.play(soundId, volume, volume, 1, 0, playbackSpeed) ?: 0
        }
    }
}



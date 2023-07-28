package com.ndds.freedomclouds;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public class SoundClip {
    Activity context;
    private SoundPool soundPool;
    private MediaPlayer emblemRotateSound;

    SoundClip(Activity context) {
        this.context = context;
        initSoundPool();
    }

    public void pauseEmblemSound(){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emblemRotateSound.pause();
            }
        });
    }

    public void release() {
        soundPool.release();
    }

    public void playEmblemSound(){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emblemRotateSound.start();
            }
        });

    }
    private void initSoundPool(){
        AudioAttributes attributes;

        emblemRotateSound = MediaPlayer.create(context, R.raw.rotation);
        emblemRotateSound.setLooping(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(1)
                    .build();

            emblemRotateSound.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
            emblemRotateSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

    }
    public void playSound(int id){
        int soundId = soundPool.load(context, id, 1);
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPool.play(soundId, 1, 1, 1, 0, 1));
    }
}

package com.ndds.freedomclouds;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class SoundClip {
    Activity context;
    private final SoundPool soundPool;
    private final MediaPlayer emblemRotateSound;

    SoundClip(Activity context) {
        this.context = context;
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

    public void pauseEmblemSound(){
        if(emblemRotateSound.isPlaying()) {
            emblemRotateSound.pause();
        }
    }

    public void pauseAll() {
        soundPool.autoPause();
        emblemRotateSound.pause();
    }

    public void resumeAll() {
        soundPool.autoResume();
    }

    public void release() {
        soundPool.release();
    }

    public void playEmblemSound(){
        emblemRotateSound.start();
    }

    public void playSound(int id){
        int soundId = soundPool.load(context, id, 1);
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPool.play(soundId, 1, 1, 1, 0, 1));
    }
}

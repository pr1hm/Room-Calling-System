package com.pratham.roomcalling.util;

import android.content.Context;
import android.media.MediaPlayer;

import com.pratham.roomcalling.R;
import com.pratham.roomcalling.model.Room;

import java.util.List;

public class RoomAudioManager {

    private Context context;
    private MediaPlayer mediaPlayer;
    private int currentPlayingStatus = 0;
    private int playCount = 0;

    // Default settings injected by the Activity
    private boolean isSoundEnabled = true;
    private int maxRepeats = 1;
    private boolean isAlarmCompleted = false;

    public RoomAudioManager(Context context) {
        this.context = context;
    }

    // NEW: The Activity will pass the settings here
    public void updateSettings(boolean isEnabled, int repeats) {
        this.isSoundEnabled = isEnabled;
        this.maxRepeats = repeats;

        // Instantly kill sound if admin toggles it OFF while playing
        if (!isEnabled && mediaPlayer != null && mediaPlayer.isPlaying()) {
            stopInnerAudio();
        }
    }

    public void evaluateAndPlay(List<Room> rooms) {
        int highestStatus = 0;
        for (Room room : rooms) {
            if (room.getStatus() > highestStatus) {
                highestStatus = room.getStatus();
            }
        }

        if (highestStatus == 0) {
            stopAudio();
            return;
        }

        // Only play if it's a new emergency, OR if it's a higher priority emergency
        if (highestStatus != currentPlayingStatus) {
            isAlarmCompleted = false; // Reset completion flag
            playCustomSound(highestStatus);
        }
    }

    private void playCustomSound(int status) {
        stopInnerAudio();

        if (!isSoundEnabled) {
            currentPlayingStatus = status; // Acknowledge silently
            return;
        }

        int soundResource;
        switch (status) {
            case 4: soundResource = R.raw.emergency_code_blue; break;
            case 3: soundResource = R.raw.care_required; break;
            case 2: soundResource = R.raw.assistance_needed; break;
            case 1: default: soundResource = R.raw.standard_call; break;
        }

        mediaPlayer = MediaPlayer.create(context, soundResource);
        if (mediaPlayer != null) {
            playCount = 1;
            currentPlayingStatus = status;

            if (maxRepeats == 0) {
                // Native infinite looping is perfectly stable
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            } else {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    if (playCount < maxRepeats) {
                        playCount++;
                        mp.start();
                    } else {
                        isAlarmCompleted = true; // Mark as finished
                        stopInnerAudio();
                    }
                });
            }
        }
    }

    private void stopInnerAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void stopAudio() {
        stopInnerAudio();
        currentPlayingStatus = 0;
        playCount = 0;
        isAlarmCompleted = false;
    }
}
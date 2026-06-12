package com.pratham.roomcalling.util;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import com.pratham.roomcalling.model.Room;
import java.util.List;

public class RoomAudioManager {
    private Ringtone currentRingtone;
    private final Context context;
    private int currentPlayingPriority = 0;

    public RoomAudioManager(Context context) {
        this.context = context;
    }

    public void evaluateAndPlay(List<Room> rooms) {
        int highestPriority = 0;

        // 1. Find the highest active status across all rooms
        for (Room room : rooms) {
            if (room.getStatus() > highestPriority) {
                highestPriority = room.getStatus();
            }
        }

        // 2. If the emergency level hasn't changed, keep doing what we are doing
        if (highestPriority == currentPlayingPriority) {
            return;
        }

        // 3. Status changed! Stop the old sound.
        stopAudio();
        currentPlayingPriority = highestPriority;

        // 4. If everything is Idle (0), stay silent and exit.
        if (highestPriority == 0) {
            return;
        }

        // 5. Pick the right sound based on the emergency level
        Uri soundUri = null;
        if (highestPriority == 4 || highestPriority == 3) {
            // Emergency (4) or Care Required (3) -> Play loud system ALARM
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        } else if (highestPriority == 2 || highestPriority == 1) {
            // Assistance (2) or Call (1) -> Play short system NOTIFICATION
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // 6. Play the new sound
        if (soundUri != null) {
            currentRingtone = RingtoneManager.getRingtone(context, soundUri);
            if (currentRingtone != null) {
                currentRingtone.play();
            }
        }
    }

    public void stopAudio() {
        if (currentRingtone != null && currentRingtone.isPlaying()) {
            currentRingtone.stop();
        }
    }
}
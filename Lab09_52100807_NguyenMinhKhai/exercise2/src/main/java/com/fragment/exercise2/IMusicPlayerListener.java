package com.fragment.exercise2;

public interface IMusicPlayerListener {
    void onUpdate(
            long currentDuration,
            long maxDuration,
            String songName
    );

    void onStartNotificationForegroundDone();

    void onServiceStopped();

    void onMusicPause();
}

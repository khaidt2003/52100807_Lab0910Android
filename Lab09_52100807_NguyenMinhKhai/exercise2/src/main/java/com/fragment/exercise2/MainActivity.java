package com.fragment.exercise2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements IMusicPlayerListener, SeekBar.OnSeekBarChangeListener {

    ObjectAnimator rotateAnimation;
    Button btBack, btPauseOrPlay, btNext;
    ImageView ivDisk;
    TextView tvCurrentTimePlaying, tvSongName, tvTotalTimeOfSong;
    SeekBar sbProcess;
    Boolean isPressedPlay = false;
    private long totalDuration = 0L;
    private static final int NOTIFICATION_REQUEST = 12371823;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {android.Manifest.permission.POST_NOTIFICATIONS}
                    , NOTIFICATION_REQUEST);
        }
    }

    private void initView() {
        ivDisk = findViewById(R.id.ivDisk);
        btBack = findViewById(R.id.btBack);
        btPauseOrPlay = findViewById(R.id.btPauseOrPlay);
        btNext = findViewById(R.id.btNext);
        tvCurrentTimePlaying = findViewById(R.id.tvCurrentTimePlaying);
        tvSongName = findViewById(R.id.tvSongName);
        tvTotalTimeOfSong = findViewById(R.id.tvTotalTimeOfSong);
        sbProcess = findViewById(R.id.sbProcess);
        sbProcess.setOnSeekBarChangeListener(this);
        initAnimation();

        Intent musicBroadcast = new Intent(getApplicationContext(), MusicActionReceiver.class);
        btPauseOrPlay.setOnClickListener(v -> {
            if(getApplication() instanceof  MusicApplication) {
                final MusicApplication musicApplication = (MusicApplication) getApplication();
                if (!musicApplication.isBound) {
                    musicBroadcast.setAction(MusicActionConstant.INIT_MUSIC_PLAYER_SERVICE);
                    sendBroadcast(musicBroadcast,null);
                } else {
                    if(isPressedPlay) {
                        isPressedPlay = false;
                        btPauseOrPlay.setText(R.string.play);
                        musicBroadcast.setAction(MusicActionConstant.PAUSE);
                    } else {
                        isPressedPlay = true;
                        btPauseOrPlay.setText(R.string.pause);
                        resumeDiskAnimation();
                        musicBroadcast.setAction(MusicActionConstant.PLAY);
                    }
                    sendBroadcast(musicBroadcast,null);
                }
            }
        });
        btNext.setOnClickListener(v -> {
            clearDiskAnimation();
            startDiskAnimation();
            musicBroadcast.setAction(MusicActionConstant.PLAY_NEXT);
            sendBroadcast(musicBroadcast);
        });
        btBack.setOnClickListener(v -> {
            clearDiskAnimation();
            startDiskAnimation();
            musicBroadcast.setAction(MusicActionConstant.PLAY_PREVIOUS);
            sendBroadcast(musicBroadcast);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == NOTIFICATION_REQUEST && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getApplication() instanceof MusicApplication) {
            final MusicApplication musicApplication =(MusicApplication) getApplication();
            musicApplication.setMusicPlayerListener(this);
            if(musicApplication.isBound) {
                musicApplication.startNotificationForeground();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getApplication() instanceof MusicApplication) {
            final MusicApplication musicApplication =(MusicApplication) getApplication();
            musicApplication.removeMusicPlayerListener();
        }
    }

    @Override
    public void onUpdate(long currentDuration, long maxDuration, String songName) {
        tvCurrentTimePlaying.setText(Utils.milliSecondsToTimer(currentDuration));
        tvTotalTimeOfSong.setText(Utils.milliSecondsToTimer(maxDuration));
        totalDuration = maxDuration;
        int progress = (int)(Utils.getProgressPercentage(currentDuration, maxDuration));
        tvSongName.setText(songName);
        sbProcess.setProgress(progress);
        if(!isPressedPlay) {
            isPressedPlay = true;
            btPauseOrPlay.setText(R.string.pause);
            startDiskAnimation();
        }
    }

    @Override
    public void onStartNotificationForegroundDone() {
        Intent musicBroadcast = new Intent(this, MusicActionReceiver.class);
        isPressedPlay = true;
        btPauseOrPlay.setText(R.string.pause);
        musicBroadcast.setAction(MusicActionConstant.PLAY);
        sendOrderedBroadcast(musicBroadcast,null);
        startDiskAnimation();
    }

    @Override
    public void onServiceStopped() {
        pauseDiskAnimation();
        isPressedPlay = false;
        btPauseOrPlay.setText(R.string.play);
        sbProcess.setProgress(0);
        tvCurrentTimePlaying.setText("0:00");
    }

    @Override
    public void onMusicPause() {
        pauseDiskAnimation();
        isPressedPlay = false;
        btPauseOrPlay.setText(R.string.play);
    }

    private void initAnimation(){
        rotateAnimation = ObjectAnimator.ofFloat(ivDisk, View.ROTATION, 0f, 360f);
        rotateAnimation.setDuration(6000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
    }

    private void startDiskAnimation() {
        rotateAnimation.start();
    }

    private void clearDiskAnimation() {
        ivDisk.clearAnimation();
    }

    private void pauseDiskAnimation() {
        rotateAnimation.pause();
    }

    private void resumeDiskAnimation() {
        rotateAnimation.resume();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isPressedPlay = false;
        btPauseOrPlay.setText(R.string.play);
        pauseDiskAnimation();
        Intent musicBroadcast = new Intent(getApplicationContext(), MusicActionReceiver.class);
        musicBroadcast.setAction(MusicActionConstant.PAUSE);
        sendBroadcast(musicBroadcast);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int currentProcessing = Utils.progressToTimer(seekBar.getProgress(), Math.toIntExact(totalDuration));
        Intent musicBroadcast = new Intent(getApplicationContext(), MusicActionReceiver.class);
        musicBroadcast.putExtra(MusicPlayerService.DURATION_SEEK_KEY, currentProcessing);
        musicBroadcast.setAction(MusicActionConstant.SEEK);
        sendOrderedBroadcast(musicBroadcast, null);
        musicBroadcast.removeExtra(MusicPlayerService.DURATION_SEEK_KEY);
        musicBroadcast.setAction(MusicActionConstant.PLAY);
        sendOrderedBroadcast(musicBroadcast, null);
    }
}
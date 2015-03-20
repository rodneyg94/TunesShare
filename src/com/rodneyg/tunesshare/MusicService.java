package com.rodneyg.tunesshare;

import java.util.ArrayList;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    // Media player
    private MediaPlayer player;
    // Song list
    private ArrayList<Song> songs;
    // Current position
    private int songPosn;
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    public void onCreate() {
        // Create the service
        super.onCreate();
        // Initialize position
        songPosn = 0;
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onPrepared(MediaPlayer mp) {
        // Start playback
        mp.start();
        Intent notIntent = new Intent(this, MusicActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    public void initMusicPlayer() {
        // Set player properties
        // Wake lock lets playback continue when device becomes idle
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // Sets stream type to music
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Sets class as listener when
        //  - MediaPlayer instance is prepared
        //  - Song has completed playback
        //  - Error is thrown
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setLooping(true);
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    private final IBinder musicBind = new MusicBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!player.isPlaying()) {
            stopForeground(true);
            player.stop();
            player.reset();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void playSong(){
        // Play a song
        player.reset();
        // Get song
        Song playSong = songs.get(songPosn);
        // Set song title
        songTitle = playSong.getTitle();
        // Get id
        long currSong = playSong.getID();
        // Set uri
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        if (player.isPlaying()) {
            player.pause();
        }
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        songPosn--;
        if(songPosn < 0) songPosn = songs.size()-1;
        playSong();
    }

    public void playNext(){
        songPosn++;
        songPosn++;
        if (songPosn >= songs.size()) songPosn = 0;
        playSong();
    }
}

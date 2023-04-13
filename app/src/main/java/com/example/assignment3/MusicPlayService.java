package com.example.assignment3;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
//this MusicPlayService class inherits JobIntentService class
public class MusicPlayService extends JobIntentService {

    private MediaPlayer mediaPlayer;
    private Timer timer;

    public MusicPlayService() {
    }

    //use this function when binding service
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return new MusicController();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }

    //create music playing service
    @Override
    public void onCreate(){
        super.onCreate();
        //instantiate the media player
        mediaPlayer = new MediaPlayer();
    }

    //destroy music playing service
    @Override
    public void onDestroy(){
        super.onDestroy();
        //stop playing music
        mediaPlayer.stop();
        //release the resource
        mediaPlayer.release();
        //set mediaPlayer as null
        mediaPlayer = null;
    }

    //PLaying music function
    public void play(String music){

        try{
            if(mediaPlayer==null){
                mediaPlayer = new MediaPlayer();
            }
            //reset media player
            mediaPlayer.reset();
            //set resource of music file
            mediaPlayer.setDataSource(music);
            //prepare media player
            mediaPlayer.prepare();
            //start to play music
            mediaPlayer.start();
            //give a timer
            addTimer();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //pausing music function
    public void pause(){
        mediaPlayer.pause();
    }
    //continuing play music function
    public void continuePlay(){
        mediaPlayer.start();
    }
    //modifing the position of playing time function
    public void seek(int time){
        mediaPlayer.seekTo(time);
    }

    //add timer function
    public void addTimer() {
        //if the timer is not instantiated
        if(timer == null){
            //instantiate a new timer
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //get music duration
                    int mDuration = mediaPlayer.getDuration();
                    //get the current position of music playing
                    int mCurrentPosition = mediaPlayer.getCurrentPosition();
                    //instantiate a message object
                    Message mMessage = MusicPlayActivity.handler.obtainMessage();
                    //encapsulate the current music playing progress into message object
                    Bundle bundle = new Bundle();
                    bundle.putInt("Duration", mDuration);
                    bundle.putInt("CurrentPosition", mCurrentPosition);
                    mMessage.setData(bundle);
                    //send meaasge to MusicPlayActivity
                    MusicPlayActivity.handler.sendMessage(mMessage);
                }
            },1,1000);
        }
    }

    //Inner class MusicController class to instantiate MusicPlayInterface interface
    class MusicController extends Binder implements MusicPlayInterface{

        //Instantiate four interface function
        @Override
        public void play(String music) {
            MusicPlayService.this.play(music);
        }

        @Override
        public void pause() {
            MusicPlayService.this.pause();

        }

        @Override
        public void continuePlay() {
            MusicPlayService.this.continuePlay();

        }

        @Override
        public void seek(int time) {
            MusicPlayService.this.seek(time);

        }
    }
}
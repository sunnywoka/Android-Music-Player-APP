package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Random;

public class MusicPlayActivity extends AppCompatActivity {

    MusicPlayInterface musicPlayInterface;
    Intent mIntent;
    MyServiceConnection myServiceConnection;

    //four button
    private Button mButtonPlay, mButtonBack, mButtonNext, mButtonLast, mButtonRandom;

    //music titles, paths and album cover
    String[] titles, paths;
    int pos, count;
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    Bitmap cover;
    boolean randomOn;

    Random rand = new Random();

    //display the music playing progress
    private static SeekBar mSeekBar;

    private static TextView mTextView_time;
    private static TextView mTextView_duration;
    private static TextView mTextView_title;
    private static ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        mTextView_time = (TextView) findViewById(R.id.textViewPlayTime);
        mTextView_duration = (TextView) findViewById(R.id.textViewPlayDuration);
        mTextView_title = (TextView) findViewById(R.id.textViewPlayingMusicTitle);
        mImageView = findViewById(R.id.imageViewCover);

        //instantiate an intent object and get music title and path
        mIntent = new Intent(this, MusicPlayService.class);
        titles = getIntent().getStringArrayExtra("Titles");
        paths = getIntent().getStringArrayExtra("Paths");
        pos = getIntent().getIntExtra("Position", 0);
        count = getIntent().getIntExtra("MusicCount", 0);


        randomOn = false;

        //start service
        startService(mIntent);
        //create service connection object
        myServiceConnection = new MyServiceConnection();
        //binding service
        bindService(mIntent, myServiceConnection, BIND_AUTO_CREATE);

        //give button listener
        mButtonPlay = findViewById(R.id.buttonPlay);
        mButtonPlay.setOnClickListener(View -> buttonClick(mButtonPlay));
        mButtonBack = findViewById(R.id.buttonBack);
        mButtonBack.setOnClickListener(View -> buttonClick(mButtonBack));
        mButtonNext = findViewById(R.id.buttonNext);
        mButtonNext.setOnClickListener(View -> buttonClick(mButtonNext));
        mButtonLast = findViewById(R.id.buttonLast);
        mButtonLast.setOnClickListener(View -> buttonClick(mButtonLast));
        mButtonRandom = findViewById(R.id.buttonRandom);
        mButtonRandom.setOnClickListener(View -> buttonClick(mButtonRandom));
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        //give seek bar a change listener
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int mChange = seekBar.getProgress();
                //change music playing progress
                musicPlayInterface.seek(mChange);
            }
        });
    }

    //button click function to start other function
    private void buttonClick(Button b) {
        if (b.getText().equals("Play")) {//play music
            this.play(paths[pos], titles[pos]);
        } else if (b.getText().equals("Pause")) {//pause music
            this.pause();
        } else if (b.getText().equals("Continue")) {//continue play music
            this.continuePlay();
        } else if (b.getText().equals("Back")) {//back main menu
            this.back();
        } else if (b.getText().equals("Next")) {//next music
            if ((pos < count - 1) && (!randomOn)) {
                pos++;
                this.play(paths[pos], titles[pos]);
            } else if (randomOn) {
                int int_random = rand.nextInt(count);
                while (int_random == pos) {
                    int_random = rand.nextInt(count);
                }
                pos = int_random;
                this.play(paths[pos], titles[pos]);
            }
        } else if (b.getText().equals("Last")) {
            if (pos > 0) {
                pos--;
                this.play(paths[pos], titles[pos]);
            }
        } else if (b.getText().equals("Random")) {
            mButtonRandom.setText("Off");
            randomOn = true;
        } else if (b.getText().equals("Off")) {
            mButtonRandom.setText("Random");
            randomOn = false;
        }

    }


    //Message handler object
    public static Handler handler = new Handler(){

        //dealing the message from child thread
        public void handleMessage(Message m){
            //get message of music play progress from child thread
            Bundle bundle = m.getData();
            //music duration
            int duration = bundle.getInt("Duration");
            //music current playing position
            int currentPosition = bundle.getInt("CurrentPosition");
            //renew the seek bar length
            mSeekBar.setMax(duration);
            mSeekBar.setProgress(currentPosition);
            //convert the duration to minute and second
            int minute = duration / 1000 / 60;
            int second = duration / 1000 % 60;
            //minute and second string
            String strMinute = null;
            String strSecond = null;
            //if minute is smaller than 10
            if(minute < 10){
                strMinute = "0" + minute;
            } else{
                strMinute = minute + "";
            }
            //if second is smaller than 10
            if(second < 10){
                strSecond = "0" + second;
            } else{
                strSecond = second + "";
            }
            //display the music duration in text view
            mTextView_duration.setText(strMinute + ":" + strSecond);


            //convert the duration to minute and second
            int currentMinute = currentPosition / 1000 / 60;
            int currentSecond = currentPosition / 1000 % 60;
            //current minute and second string
            String strCurrentMinute = null;
            String strCurrentSecond = null;
            //if current minute is smaller than 10
            if(currentMinute < 10){
                strCurrentMinute = "0" + currentMinute;
            } else{
                strCurrentMinute = currentMinute + "";
            }
            //if current second is smaller than 10
            if(currentSecond < 10){
                strCurrentSecond = "0" + currentSecond;
            } else{
                strCurrentSecond = currentSecond + "";
            }
            //display the music current playing progress
            mTextView_time.setText(strCurrentMinute + ":" + strCurrentSecond);
        }
    };

    //play function for play button
    public void play(String path, String title){
        //play music with given path
        musicPlayInterface.play(path);
        //display music title
        mTextView_title.setText(title);
        //change button text
        mButtonPlay.setText("Pause");
        //get music album cover from music file
        mmr.setDataSource(paths[pos]);
        byte[] tempPicture = mmr.getEmbeddedPicture();
        //check if there is not album cover in music file
        if(tempPicture != null) {
            cover = BitmapFactory.decodeByteArray(tempPicture, 0, tempPicture.length);
        }
        //display album cover
        mImageView.setImageBitmap(cover);
    }

    //pause function for pause button
    public void pause(){
        //pause music
        musicPlayInterface.pause();
        //change button text
        mButtonPlay.setText("Continue");
    }

    //contunurePlay function for continue button
    public void continuePlay(){
        //continue play music
        musicPlayInterface.continuePlay();
        //change button text
        mButtonPlay.setText("Pause");
    }

    //back function for back button
    public void back(){
        //close current window
        finish();
    }

    //inner class MyServiceConnection instantiate
    class MyServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //get middleman object
            musicPlayInterface = (MusicPlayInterface) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }


}
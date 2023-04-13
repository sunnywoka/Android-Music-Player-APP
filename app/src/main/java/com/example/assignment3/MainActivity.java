package com.example.assignment3;

//Wenquan Zhang
//Yuekai Wu
//We develop a simple music player application for Android to play the music which are stored in the device.
//It has a main menu to display the list of music files. The user can click on the music name, then the application will start the music play activity to play music
//The music playing layout can display the music's name and its album picture. Also, there are three buttons: play/pause, last and next.

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView mView;
    //RecyclerView.LayoutManager mLayoutManager;
    Cursor mCursor;
    MusicPlayerAdapter mAdapter;
    //Some index to find the path of music files, their title and duration
    int musicPathIndex, musicTitleIndex, musicDurationIndex, musicIdIndex;
    //BitmapFactory.Options bopts;
    String[] paths = new String[1000];
    String[] titles = new String[1000];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("1", "on");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("1", "on");
        mView = findViewById(R.id.myView);
       //set each row to display one music title and its duration
        mView.setLayoutManager(new GridLayoutManager(this, 1));
        //check premission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        //get contact with the music files stored in the device
        mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                //MediaStore.Audio.Media.IS_MUSIC + "=1",
                null,
                null,
                null
        );
        //instantiate adapter
        mAdapter = new MusicPlayerAdapter(mCursor);
        //set adapter
        mView.setAdapter(mAdapter);
        Log.i("1", "on");
    }

    //Inner class MusicPlayerAdapter
    public class MusicPlayerAdapter extends RecyclerView.Adapter<MusicPlayerAdapter.ViewHolder> {
        Cursor mCursor;
        String title, duration;

        //Inner class ViewHolder
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView textViewTitle;
            TextView textViewDuration;
            public ViewHolder(View v) {
                super(v);
                textViewTitle = v.findViewById(R.id.textViewTitle);
                textViewDuration = v.findViewById(R.id.textViewDuration);
            }
        }

        //Constructor
        public MusicPlayerAdapter(Cursor c) {
            mCursor = c;
            musicPathIndex = mCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            musicTitleIndex = mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            musicIdIndex = mCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            musicDurationIndex = mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            for (mCursor.moveToFirst();!mCursor.isAfterLast();mCursor.moveToNext()) {
                int i = mCursor.getPosition();
                paths[i] = mCursor.getString(musicPathIndex);
                titles[i] = mCursor.getString(musicTitleIndex);
                Log.i("1111", "Load " + i + "   " + titles[i]);
            }
        }

        //Create new views
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            //If the user clicks one music title, the music play activity will starts
            v.setOnClickListener((view)-> {
                int pos = vh.getAdapterPosition();
                mCursor.moveToPosition(pos);
                Log.i("Click Listener", "Clicked" + mCursor.getPosition());
                //Build an Intent to start the music play acticity
                Intent intent = new Intent(MainActivity.this, MusicPlayActivity.class);
                //Add extended data to the intent
                intent.putExtra("path", mCursor.getString(musicPathIndex));
                intent.putExtra("id", mCursor.getFloat(musicIdIndex));
                intent.putExtra("duration", mCursor.getFloat(musicDurationIndex));
                //intent.putExtra("title", mCursor.getInt(musicTitleIndex));
                intent.putExtra("Title", mCursor.getString(musicTitleIndex));
                intent.putExtra("Paths", paths);
                intent.putExtra("Titles", titles);
                intent.putExtra("Position", pos);
                intent.putExtra("MusicCount", mCursor.getCount());
                startActivity(intent);
            });
            return vh;
        }

        // Replace the contents of a view
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //Background thread to speed up loading the music files
            new AsyncTask<ViewHolder,Void,String[]>() {
                private ViewHolder holder;
                protected String[] doInBackground(ViewHolder... params) {
                    String[] tempS = new String[2];
                    holder =params[0];
                    if (mCursor.moveToPosition(holder.getAdapterPosition())) {
                        //get music title
                        title = mCursor.getString(musicTitleIndex);
                        //get music duration
                        duration = mCursor.getString(musicDurationIndex);
                        tempS[0] = title;
                        //convert duration to minute and second
                        tempS[1] = millisecondConverter(duration);
                    }
                    return tempS;
                }
                protected void onPostExecute(String[] s) {
                    //display music title and duration
                    holder.textViewTitle.setText(s[0]);
                    holder.textViewDuration.setText(s[1]);
                }
            }.execute(holder);
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    //A function to convert the duration of music to minute and second
    public String millisecondConverter(String ms) {
        int ims = Integer.parseInt(ms);
        int minute = ims / 1000 / 60;
        int second = ims / 1000 % 60;
        String result = minute + ":" + second;
        return result;
    }
}


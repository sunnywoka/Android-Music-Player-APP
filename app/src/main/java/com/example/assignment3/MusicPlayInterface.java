package com.example.assignment3;

//music play interface
public interface MusicPlayInterface {
    //play function
    void play(String music);
    //pause function
    void pause();
    // continue play function
    void continuePlay();
    //modify the position of playing time
    void seek(int time);
}

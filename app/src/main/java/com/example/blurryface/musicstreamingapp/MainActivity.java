package com.example.blurryface.musicstreamingapp;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    TextView startTime,endTime,songTitle,artistTitle;
    ImageView play,rewind,forward,albumCover;
    MediaPlayer mediaPlayer;
    Uri musicUri,imageUri;
    StorageReference musicStorage;
    DatabaseReference musicInfo;
    SeekBar musicProgress;
    //variables for the length of the song
    private int durationTime,currentTime;
    public boolean isNotPlaying=true;
    String duration;
    //variables for the seconds of rewind and forward
    private int forwardSeekTime =5*1000;
    private int rewindSeekTime = 5*1000;

    private SpotsDialog spotsDialog;
    private ProgressBar songBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize the widgets
        play = findViewById(R.id.playImage);
        rewind = findViewById(R.id.reverseImage);
        forward = findViewById(R.id.fowardImage);
        albumCover = findViewById(R.id.albumimageView);
        songTitle = findViewById(R.id.songTextView);
        startTime = findViewById(R.id.startText);
        endTime = findViewById(R.id.endText);
        artistTitle = findViewById(R.id.artistTextView);
        musicProgress = findViewById(R.id.MusicseekBar);
        songBar = findViewById(R.id.songprogressBar);
        //initialise our mediaPlayer
        mediaPlayer = new MediaPlayer();
        //database
        musicStorage = FirebaseStorage.getInstance().getReference();
        musicInfo = FirebaseDatabase.getInstance().getReference().child("Music");
        songBar.setVisibility(View.GONE);


        try {
            //gets the music file on firebase
            musicUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/musicapp-9cc45.appspot.com/o/Music%2FAnne-Maria%2FThen.mp3?alt=media&token=76006f4e-b12c-4472-b365-b0e56dc5ccef");
            mediaPlayer.setDataSource(MainActivity.this,musicUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
        //gets the image file from firebase database
        imageUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/musicapp-9cc45.appspot.com/o/Music%2FAnne-Maria%2FCover.jpg?alt=media&token=ad764b39-a58c-4251-b198-e0f2d54c0613");
        Picasso.with(this).load(imageUri).placeholder(R.drawable.defaultimage).into(albumCover);



        //play music and pausing music
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPause();
            }
        });
        //rewind when button clicked
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rewind();
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foward();
            }
        });

        //get the length of the song
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                durationTime = mediaPlayer.getDuration()/1000;
                //convert duration to min and sec
                duration = String.format("%02d:%02d",durationTime/60,durationTime%60);
                endTime.setText(duration);
            }
        });
        //set the maximum progress at 100%
        musicProgress.setMax(mediaPlayer.getDuration()/1000);



        musicProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(mediaPlayer!=null && b) {
                    mediaPlayer.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //get music Information
        musicInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String artist = dataSnapshot.child("01").child("artists").getValue().toString();
                String title = dataSnapshot.child("01").child("title").getValue().toString();
                artistTitle.setText(artist);
                songTitle.setText(title);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,databaseError.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void playPause(){
        //check if music is playing
        if(isNotPlaying){
            mediaPlayer.start();
            //start the progress bar to move and counts down the seconds
            new MusicProgress().execute();
            isNotPlaying = false;
            play.setImageResource(R.drawable.ic_action_pause);
        }else {
            mediaPlayer.pause();
            isNotPlaying=true;
            play.setImageResource(R.drawable.ic_action_play);
        }
    }
    public void foward(){
        if(mediaPlayer!=null){
            int songTime = mediaPlayer.getCurrentPosition();
            //make sure the song cannot foward past the song total time
            if(songTime+forwardSeekTime<=mediaPlayer.getDuration()){
                mediaPlayer.seekTo(songTime+forwardSeekTime);
            }else {
                mediaPlayer.seekTo(mediaPlayer.getDuration());
            }
        }
    }
    public void rewind(){
        if(mediaPlayer!=null){
            int songTime = mediaPlayer.getCurrentPosition();
            //make sure that the song has played for more than five seconds
            if(songTime-rewindSeekTime>=0){
                mediaPlayer.seekTo(songTime-rewindSeekTime);
            }else {
                mediaPlayer.seekTo(0);
            }
        }
    }

    public void onBuy(View view){

    }
    /*
    @Override
    protected void onStop() {
        super.onStop();
        //makes sure the background task stops
        isNotPlaying = true;
    }
    */
    //class to run in the background
    public class  MusicProgress extends AsyncTask<Void, Integer,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            do{
                //get Current Time milliseconds
                currentTime = mediaPlayer.getCurrentPosition()/1000;
                publishProgress(currentTime);
            }while (musicProgress.getProgress()<=mediaPlayer.getDuration() / 1000);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            try {
                //update the seek bar to the current time
                musicProgress.setProgress(values[0]);
                //get the current time of the music in minute and seconds
                String currentString = String.format("%02d:%02d",values[0]/60,values[0]%60);
                startTime.setText(currentString);
            }
            catch (Exception e){
                Toast.makeText(MainActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }

        }
    }



}

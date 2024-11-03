package com.example.accentrecognition;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AccentsExamples extends BaseActivity {

    // MediaPlayer instance to handle audio playback
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accents_examples);

        // Buttons for different accents
        Button buttonIndia = findViewById(R.id.button_india);
        Button buttonUS = findViewById(R.id.button_us);
        Button buttonEngland = findViewById(R.id.button_england);
        Button buttonStop = findViewById(R.id.button_stop);

        // Set onClickListeners for each button to play corresponding audio
        buttonIndia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(R.raw.india);
            }
        });

        buttonUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(R.raw.us);
            }
        });

        buttonEngland.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(R.raw.england);
            }
        });

        // Set onClickListener for the stop button to stop audio playback
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudio();
            }
        });
    }

    // Plays the specified audio file by resource ID
    private void playAudio(int audioResId) {
        // Release existing MediaPlayer instance if it exists
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        // Create a new MediaPlayer with the selected audio and start playback
        mediaPlayer = MediaPlayer.create(this, audioResId);
        mediaPlayer.start();
    }

    // Stops audio playback if it's currently playing
    private void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Releases MediaPlayer resources when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

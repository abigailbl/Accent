package com.example.accentrecognition;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryActivity extends BaseActivity {

    private SharedPreferences sharedPreferences;
    private LinearLayout recordingsLayout;
    private List<String> recordingsList;
    private MediaPlayer mediaPlayer;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sharedPreferences = getSharedPreferences("recordings", Context.MODE_PRIVATE);
        recordingsLayout = findViewById(R.id.recordingsLayout);
        btnStop = findViewById(R.id.btnStop);

        cleanUpRecordings();
        loadRecordings();
        displayRecordings();

        btnStop.setOnClickListener(view -> stopPlayback());
    }

    private void cleanUpRecordings() {
        Set<String> recordings = sharedPreferences.getStringSet("recordingsList", new HashSet<>());
        Set<String> updatedRecordings = new HashSet<>();

        for (String filePath : recordings) {
            File file = new File(filePath);
            if (file.exists()) {
                if (filePath.contains("AUDIORECORDING")) { // שם המזהה הקלטות חסרות שם מדינה
                    if (file.delete()) {
                        Log.d("CleanUp", "Deleted recording: " + filePath);
                    } else {
                        Log.e("CleanUp", "Failed to delete recording: " + filePath);
                    }
                } else {
                    updatedRecordings.add(filePath);
                }
            } else {
                Log.e("CleanUp", "File does not exist: " + filePath);
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("recordingsList", updatedRecordings);
        editor.apply();
    }


    private void loadRecordings() {
        Set<String> recordings = sharedPreferences.getStringSet("recordingsList", new HashSet<>());
        recordingsList = new ArrayList<>(recordings);
        Log.d("LoadRecordings", "Loaded recordings: " + recordingsList.toString());
    }

    private void displayRecordings() {
        recordingsLayout.removeAllViews();
        for (int i = 0; i < recordingsList.size(); i++) {
            String filePath = recordingsList.get(i);
            final int index = i; // Create a final copy of the index
            LinearLayout recordingLayout = new LinearLayout(this);
            recordingLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button btnRecording = new Button(this);
            String fileName = new File(filePath).getName(); // Obtain just the file name
            btnRecording.setText(fileName);
            btnRecording.setOnClickListener(view -> playRecording(filePath));

            ImageButton btnDelete = new ImageButton(this);
            btnDelete.setImageResource(android.R.drawable.ic_delete);
            btnDelete.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btnDelete.setOnClickListener(view -> confirmDeleteRecording(index)); // Use the final variable

            recordingLayout.addView(btnRecording);
            recordingLayout.addView(btnDelete);

            recordingsLayout.addView(recordingLayout);
        }
    }


    private void playRecording(String filePath) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Playing recording", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(this, "Playback stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteRecording(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Recording")
                .setMessage("Are you sure you want to delete this recording?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRecording(position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteRecording(int position) {
        String filePath = recordingsList.get(position);
        File file = new File(filePath);

        if (file.exists() && file.delete()) {
            recordingsList.remove(position);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> recordingsSet = new HashSet<>(recordingsList);
            editor.putStringSet("recordingsList", recordingsSet);
            editor.apply();

            displayRecordings();
            Toast.makeText(this, "Recording deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete recording", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}

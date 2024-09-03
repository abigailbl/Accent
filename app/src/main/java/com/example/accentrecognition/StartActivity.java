package com.example.accentrecognition;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class StartActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;
    private Animation pulseAnimation;
    private SharedPreferences sharedPreferences;
    private int recordingCounter = 1; // Start from 1
    private String[] sentences = {
            "The sun was shining brightly in the sky. Birds were chirping as they flew between the trees." +
                    " A soft breeze carried the scent of " +
                    "flowers through the air. It was a perfect day to spend outside in nature.",
            "John had always wanted to travel the world. He dreamed of visiting different countries and learning about" +
                    " their cultures. Finally, he saved enough money to begin his journey. His first stop was Paris, where he admired the beautiful architecture.",
            "The cat sat quietly on the windowsill, watching the world go by. It enjoyed the warmth of the sunlight streaming through the glass. Occasionally, it would twitch its tail in response to something outside. But mostly, it was content just to relax in its cozy spot.",
            "Sarah worked hard every day to achieve her goals. She studied long hours and remained focused on her future. Despite the challenges, she never gave up. Eventually, her dedication paid off, and she succeeded beyond her expectations.",
            "The storm raged outside, with lightning flashing across the sky. Thunder boomed, shaking the house with each loud clap. Inside, the family huddled together for warmth and comfort. They waited patiently for the storm to pass, hoping for a clear morning.",
            "Technology has changed the way people live and work. With the rise of smartphones and computers, information is now at our fingertips. Communication has become instant, connecting people from different parts of the world. This shift has made the world feel smaller and more interconnected."
    };

    private TextView tvSentence;
    private Button btnChangeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        sharedPreferences = getSharedPreferences("recordings", Context.MODE_PRIVATE);

        tvSentence = findViewById(R.id.tvSentence);
        btnChangeText = findViewById(R.id.btnChangeText);

        String randomSentence = getRandomSentence();
        tvSentence.setText(randomSentence);

        btnChangeText.setOnClickListener(view -> {
            String newSentence = getRandomSentence();
            tvSentence.setText(newSentence);
        });

        ImageButton btnMicrophone = findViewById(R.id.btnMicrophone);
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);

        btnMicrophone.setOnClickListener(view -> {
            if (isRecording) {
                stopRecording();
                isRecording = false;
                btnMicrophone.clearAnimation();
                Log.d("Recording", "Recording stopped and file saved.");

            } else {
                Log.d("Recording", "before check permission.");

                if (checkPermissions()) {
                    startRecording();
                    isRecording = true;
                    btnMicrophone.startAnimation(pulseAnimation);
                    Log.d("Recording", "Recording started successfully.");
                }
            }
        });
    }

    private String getRandomSentence() {
        Random random = new Random();
        int index = random.nextInt(sentences.length);
        return sentences[index];
    }


    private boolean checkPermissions() {
        boolean audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        if ( !storagePermission) {
//            Log.d("Recording", "storage ermission.");
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 200);
//            return false;
//        }
        if (!audioPermission ) {
            Log.d("Recording", "audio ermission.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            return false;
        }
        return true;
    }

    private void startRecording() {
        audioFilePath = getFilesDir().getAbsolutePath() + "/audioRecording_" + System.currentTimeMillis() + ".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            saveRecording(audioFilePath);
            String serverUrl = "http://10.100.102.7:5000/predict";

            TextView responseTextView = findViewById(R.id.tvResponse);
//            new UploadAudioTask(responseTextView).execute(audioFilePath, serverUrl);
            new UploadAudioTask(sharedPreferences,this,responseTextView, audioFilePath).execute(audioFilePath,serverUrl);

            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecording(String filePath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> recordings = sharedPreferences.getStringSet("recordingsList", new HashSet<>());
        recordings.add(filePath);
        editor.putStringSet("recordingsList", recordings);
        editor.apply();
        Toast.makeText(this, "Recording saved: " + filePath, Toast.LENGTH_SHORT).show();

        Log.d("SaveRecording", "Saved recording at path: " + filePath);
    }

//    private void saveRecording(String filePath, String country) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Set<String> recordings = sharedPreferences.getStringSet("recordingsList", new HashSet<>());
//
//        // יצירת שם קובץ ממוספר עם המדינה
//        String numberedRecording = recordingCounter + " " + country;
//        recordings.add(numberedRecording + " - " + filePath);
//
//        // עדכון SharedPreferences
//        editor.putStringSet("recordingsList", recordings);
//        editor.apply();
//
//        // הגדלת המספר
//        recordingCounter++;
//
//        Toast.makeText(this, "Recording saved: " + numberedRecording, Toast.LENGTH_SHORT).show();
//        Log.d("SaveRecording", "Saved recording with country: " + numberedRecording);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 200) {
            boolean audioPermissionGranted = false;
            boolean storagePermissionGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                    audioPermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    storagePermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }

            if (audioPermissionGranted && storagePermissionGranted) {
                startRecording();
                isRecording = true;
                ImageButton btnMicrophone = findViewById(R.id.btnMicrophone);
                btnMicrophone.startAnimation(pulseAnimation);
            } else {
                Toast.makeText(this, "Permissions are required to record audio and save recordings.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

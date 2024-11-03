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

public class StartActivity extends BaseActivity {

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;
    private Animation pulseAnimation;
    private SharedPreferences sharedPreferences;
    private int recordingCounter = 1; // Start from 1
    private String[] sentences = {
            // List of sentences to display randomly on the screen for recording practice
            "The sun was shining brightly in the sky. Birds were chirping as they flew between the trees. A soft breeze carried the scent of flowers through the air.",
            "The cat sat quietly on the windowsill, watching the world go by. It enjoyed the warmth of the sunlight streaming through the glass.",
            "The café was warm and cozy, filled with the scent of freshly baked pastries. Rosie sipped her coffee, enjoying the momentary escape from the cold outside.",
            "The garden was in full bloom, with vibrant flowers swaying in the breeze. Rebecca knelt down to admire a particularly beautiful rose, careful not to disturb its delicate petals.",
            "Please call Stella and ask her to bring these things from the store: six spoons of fresh snow peas, five thick slabs of blue cheese, and maybe a snack for her brother Bob. We also need a small plastic snake and a big toy frog for the kids.",
            "The mountains stood tall and majestic against the clear blue sky. Johnny took a deep breath, grateful for the beauty and peace that surrounded him."
    };

    private TextView tvSentence;
    private Button btnChangeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Initialize SharedPreferences to save recordings locally
        sharedPreferences = getSharedPreferences("recordings", Context.MODE_PRIVATE);

        // Initialize sentence TextView and change text button
        tvSentence = findViewById(R.id.tvSentence);
        btnChangeText = findViewById(R.id.btnChangeText);

        // Display a random sentence on screen initially
        String randomSentence = getRandomSentence();
        tvSentence.setText(randomSentence);

        // Set listener for changing the sentence displayed on button click
        btnChangeText.setOnClickListener(view -> {
            String newSentence = getRandomSentence();
            tvSentence.setText(newSentence);
        });

        // Initialize microphone button and load pulse animation
        ImageButton btnMicrophone = findViewById(R.id.btnMicrophone);
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);

        // Set listener for microphone button to start or stop recording
        btnMicrophone.setOnClickListener(view -> {
            if (isRecording) {
                // Stop recording if already recording
                stopRecording();
                isRecording = false;
                btnMicrophone.clearAnimation();
                Log.d("Recording", "Recording stopped and file saved.");
            } else {
                // Start recording if permissions are granted
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

    // Generate a random sentence from the array
    private String getRandomSentence() {
        Random random = new Random();
        int index = random.nextInt(sentences.length);
        return sentences[index];
    }

    // Check and request permissions for audio recording and storage
    private boolean checkPermissions() {
        boolean audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!audioPermission) {
            Log.d("Recording", "Audio permission missing.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            return false;
        }
        return true;
    }

    // Start recording audio and save file to internal storage
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

    // Stop recording and release MediaRecorder resources
    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            saveRecording(audioFilePath);

            // Define the server URL to send audio for processing
            String serverUrl = "http://10.100.102.8:5000/predict";

            // Initialize TextView to display server response
            TextView responseTextView = findViewById(R.id.tvResponse);
            new UploadAudioTask(sharedPreferences, this, responseTextView, audioFilePath).execute(audioFilePath, serverUrl);

            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    // Save the recording file path to SharedPreferences
    private void saveRecording(String filePath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> recordings = sharedPreferences.getStringSet("recordingsList", new HashSet<>());
        recordings.add(filePath);
        editor.putStringSet("recordingsList", recordings);
        editor.apply();
        Toast.makeText(this, "Recording saved: " + filePath, Toast.LENGTH_SHORT).show();

        Log.d("SaveRecording", "Saved recording at path: " + filePath);
    }

    // Handle permission results from the user
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

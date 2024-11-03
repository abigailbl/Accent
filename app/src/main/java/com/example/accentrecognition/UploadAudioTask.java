package com.example.accentrecognition;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class UploadAudioTask extends AsyncTask<String, Void, String> {

    private Context context;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "UploadAudioTask";
    private TextView responseTextView;
    private String audioFilePath;
    // Constructor to initialize class fields

    public UploadAudioTask(SharedPreferences sharedPreferences,Context context,TextView responseTextView, String audioFilePath) {
        this.responseTextView = responseTextView;
        this.audioFilePath = audioFilePath;
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }


    @Override
    protected String doInBackground(String... params) {
        Log.d("stop", "upload audio url " );

        String filePath = params[0];
        String serverUrl = params[1];

        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String boundary = "*****";
        String twoHyphens = "--";
        String lineEnd = "\r\n";

        try {
            Log.d("stop", "upload audio start try " );

            // Open file input stream for reading the audio file
            FileInputStream fileInputStream = new FileInputStream(new File(filePath));

            // Open a connection to the server URL
            URL url = new URL(serverUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            Log.d("stop", "upload audio in try after con 1 " );


            try {
                // Create output stream for uploading data
                outputStream = new DataOutputStream(connection.getOutputStream());
            } catch (IOException e) {
                Log.e(TAG, "Error creating DataOutputStream: " + e.getMessage());
                e.printStackTrace();
                return null; // החזר שגיאה אם לא מצליח לפתוח את הזרם
            }

//            outputStream = new DataOutputStream(connection.getOutputStream());//!!!!!!!
            Log.d("stop", "upload audio in try after con2 " );
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + filePath + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            // Prepare buffer to read the file data
            int bytesAvailable = fileInputStream.available();
            int maxBufferSize = 1 * 1024 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // Read and upload file data in chunks
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            Log.d("stop", "upload audio before while " );

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            Log.d("stop", "upload audio in try before connection " );

            // Get response from server
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            Log.d(TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            Log.d("stop", "upload audio in try " );

            // Close streams after upload is complete
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            // Get server response as InputStream and read it
            InputStream is = connection.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            return b.toString();

        } catch (Exception ex) {
            Log.e(TAG, "Upload file to server exception: " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.d(TAG, "Server Response: " + result);
            String responseText = "Your detected mother tongue is: " + result;
            responseTextView.setText(responseText);
            new Handler().postDelayed(() -> {
                responseTextView.setText("");
            }, 5000);
            renameRecording(result);
        } else {
            Log.e(TAG, "Failed to upload file to server.");
            responseTextView.setText("Failed to upload file to server.");
            new Handler().postDelayed(() -> {
                responseTextView.setText("");
            }, 5000);
        }
    }

    // Rename the recording based on the detected country
    private void renameRecording(String country) {
        File originalFile = new File(audioFilePath);

        String uniqueId = String.valueOf(System.currentTimeMillis());
        String newFileName = context.getFilesDir().getAbsolutePath() + "/recording_" + uniqueId + "_" + country;
        File renamedFile = new File(newFileName);

        boolean renamed = originalFile.renameTo(renamedFile);

        if (renamed) {
            Log.d("RenameRecording", "Recording renamed successfully to: " + renamedFile.getName());

            updateRecordingInPreferences(renamedFile.getAbsolutePath());
        } else {
            Log.e("RenameRecording", "Failed to rename recording.");
        }
    }

    // Update the recording file path in SharedPreferences
    private void updateRecordingInPreferences(String newFilePath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> recordings = sharedPreferences.getStringSet("recordingsList", new HashSet<>());

        recordings.removeIf(filePath -> filePath.contains("The resulting accent is "));

        recordings.add(newFilePath);

        editor.putStringSet("recordingsList", recordings);
        editor.apply();

        Log.d("UpdatePreferences", "Updated recording list in SharedPreferences.");
    }

}

package com.example.accentrecognition;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

    public UploadAudioTask(SharedPreferences sharedPreferences,Context context,TextView responseTextView, String audioFilePath) {
        this.responseTextView = responseTextView;
        this.audioFilePath = audioFilePath;
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }
//    public UploadAudioTask(TextView responseTextView) {
//        this.responseTextView = responseTextView;
//    }

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

            FileInputStream fileInputStream = new FileInputStream(new File(filePath));

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

            int bytesAvailable = fileInputStream.available();
            int maxBufferSize = 1 * 1024 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

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

            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            Log.d(TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            Log.d("stop", "upload audio in try " );

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

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
            responseTextView.setText(responseText); // עדכון TextView עם התשובה
            renameRecording(result);
        } else {
            Log.e(TAG, "Failed to upload file to server.");
            responseTextView.setText("Failed to upload file to server.");
        }
    }
    private void renameRecording(String country) {
        // הנתיב של הקובץ הנוכחי
        File originalFile = new File(audioFilePath);

        // בניית שם הקובץ החדש המבוסס על שם המדינה ונתון ייחודי
        String uniqueId = String.valueOf(System.currentTimeMillis()); // ניתן גם להשתמש ב-ID ייחודי אחר
        String newFileName = context.getFilesDir().getAbsolutePath() + "/recording_" + uniqueId + "_" + country;
        File renamedFile = new File(newFileName);

        // שינוי שם הקובץ
        boolean renamed = originalFile.renameTo(renamedFile);

        if (renamed) {
            Log.d("RenameRecording", "Recording renamed successfully to: " + renamedFile.getName());

            // עדכון של ההקלטה ברשימת ההקלטות ב- SharedPreferences
            updateRecordingInPreferences(renamedFile.getAbsolutePath());
        } else {
            Log.e("RenameRecording", "Failed to rename recording.");
        }
    }



    private void updateRecordingInPreferences(String newFilePath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> recordings = sharedPreferences.getStringSet("recordingsList", new HashSet<>());

        // מחיקת ההקלטה הישנה מהרשימה אם קיימת
        recordings.removeIf(filePath -> filePath.contains("The resulting accent is "));

        // הוספת שם הקובץ החדש לרשימה
        recordings.add(newFilePath);

        // שמירה מחדש של הרשימה המעודכנת
        editor.putStringSet("recordingsList", recordings);
        editor.apply();

        Log.d("UpdatePreferences", "Updated recording list in SharedPreferences.");
    }

//    private void saveRecording(String country) {
//        // קריאה למתודה מחוץ ל-AsyncTask כדי לשמור את ההקלטה
//        ((StartActivity) responseTextView.getContext()).saveRecording(audioFilePath, country);
//    }

}

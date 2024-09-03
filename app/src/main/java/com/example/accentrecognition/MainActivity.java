package com.example.accentrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStart, btnHistory, btnAccentsExamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        btnStart = findViewById(R.id.btn_start);
        btnHistory = findViewById(R.id.btn_history);
        btnAccentsExamples = findViewById(R.id.btn_accents_examples);

        // Set onClickListeners for each button
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Start Activity (to be implemented)
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to History Activity (to be implemented)
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        btnAccentsExamples.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Play Recordings Activity (to be implemented)
                Intent intent = new Intent(MainActivity.this, AccentsExamples.class);
                startActivity(intent);
            }
        });
    }
}

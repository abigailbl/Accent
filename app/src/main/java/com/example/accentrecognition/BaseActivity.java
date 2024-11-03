package com.example.accentrecognition;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Set up the toolbar component
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set this toolbar as the default action bar for this activity
    }
}

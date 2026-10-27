package com.example.tripmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FrontPageActivity extends AppCompatActivity {

    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);

        btnGetStarted = findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(FrontPageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
package com.example.charger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_splash);

        SharedPreferences prefs = getSharedPreferences("AppUsagePrefs", MODE_PRIVATE);
        int launchCount = prefs.getInt("launch_count", 0);
        launchCount++;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("launch_count", launchCount);
        editor.apply();

        AppUsageTracker usageTracker = new AppUsageTracker(this);

        getWindow().setStatusBarColor(getColor(R.color.background_gray));
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 700);
    }
}
package com.example.water_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class WelcomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WaterLoggerPrefs";
    private static final String KEY_USER_ID = "userId";

    private ImageView splashLogo;
    private LinearLayout splashContainer;
    private TextView appTitle;
    private FloatingActionButton fabNext;
    private boolean isAnimationComplete = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);

        if (userId != -1) {
            // User is logged in, go directly to Dashboard
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return; // Skip the rest of the welcome screen logic
        }

        // User is not logged in, show the welcome screen
        setContentView(R.layout.activity_welcome);

        // Initialize views
        splashLogo = findViewById(R.id.splashLogo);
        splashContainer = findViewById(R.id.splashContainer);
        appTitle = findViewById(R.id.appTitle);
        fabNext = findViewById(R.id.fabNext);

        // Start splash screen animation
        startSplashAnimation();

        // FAB click listener
        fabNext.setOnClickListener(v -> {
            if (isAnimationComplete) {
                navigateToLogin();
            }
        });
    }

    private void startSplashAnimation() {
        // Load animations
        Animation zoomInAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        Animation slideInUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Set animation durations
        zoomInAnimation.setDuration(900);
        slideInUpAnimation.setDuration(800);
        slideInUpAnimation.setStartOffset(300);
        fadeInAnimation.setDuration(1000);

        // Apply animations to views
        if (splashLogo != null) {
            splashLogo.startAnimation(zoomInAnimation);
        }

        if (appTitle != null) {
            appTitle.startAnimation(slideInUpAnimation);
        }

        if (splashContainer != null) {
            splashContainer.startAnimation(fadeInAnimation);
        }

        // Set animation complete flag and enable FAB after animations finish
        zoomInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                fabNext.setEnabled(false);
                fabNext.setAlpha(0.5f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimationComplete = true;
                fabNext.setEnabled(true);
                fabNext.setAlpha(1.0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void navigateToLogin() {
        // Create transition animation
        Animation slideOutDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_down);
        slideOutDownAnimation.setDuration(500);

        splashContainer.startAnimation(slideOutDownAnimation);

        // Navigate after animation
        slideOutDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
}
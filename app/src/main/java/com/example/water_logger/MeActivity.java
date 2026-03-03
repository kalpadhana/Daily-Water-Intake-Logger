package com.example.water_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.water_logger.TargetUpdateBus;

public class MeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WaterLoggerPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "userId"; // saved at login

    private DatabaseHelper db;
    private int userId = -1;
    private TextView tvBigWater;
    private TextView tvWaterTarget;

    private final TargetUpdateBus.Listener targetUpdateListener = new TargetUpdateBus.Listener() {
        @Override
        public void onTargetUpdated(int updatedUserId, int newTarget) {
            if (updatedUserId == userId && newTarget > 0) {
                runOnUiThread(() -> {
                    if (tvWaterTarget != null) tvWaterTarget.setText(getString(R.string.today_target_format, newTarget));
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "Username");
        String email = prefs.getString(KEY_EMAIL, "user@example.com");
        userId = prefs.getInt(KEY_USER_ID, -1);

        TextView usernameText = findViewById(R.id.username_text);
        TextView emailText = findViewById(R.id.email_text);

        usernameText.setText(username);
        emailText.setText(email);

        // DB helper
        db = new DatabaseHelper(this);

        // water views
        tvBigWater = findViewById(R.id.tvBigWater);
        tvWaterTarget = findViewById(R.id.tvWaterTarget);

        // initialize display
        updateWaterDisplay();

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            // Show confirmation dialog before logging out
            new MaterialAlertDialogBuilder(MeActivity.this)
                    .setTitle("Confirm logout")
                    .setMessage("Are you sure you want to log out?")
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        // Clear user session
                        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                        editor.clear();
                        editor.apply();

                        // Navigate to LoginActivity
                        Intent intent = new Intent(MeActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_me);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_today) {
                startActivity(new Intent(MeActivity.this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.navigation_history) {
                startActivity(new Intent(MeActivity.this, HistoryActivity.class));
                return true;
            }
            // already on `me`
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh water values in case they changed elsewhere
        updateWaterDisplay();
        // register in-app bus listener
        TargetUpdateBus.register(targetUpdateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TargetUpdateBus.unregister(targetUpdateListener);
    }

    private void updateWaterDisplay() {
        if (tvBigWater == null) return;

        if (userId != -1 && db != null) {
            int currentMl = db.getTodayTotalIntake(userId);
            tvBigWater.setText(getString(R.string.today_current_format, currentMl));

            // update target from DB (not SharedPreferences) to stay consistent
            int target = db.getUserTarget(userId);
            if (tvWaterTarget != null) tvWaterTarget.setText(getString(R.string.today_target_format, target));
        } else {
            // fallback to static resource
            tvBigWater.setText(getString(R.string.today_current));
            if (tvWaterTarget != null) tvWaterTarget.setText(getString(R.string.today_target));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
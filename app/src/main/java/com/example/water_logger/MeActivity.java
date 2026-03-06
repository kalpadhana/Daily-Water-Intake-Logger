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
    private static final String KEY_TARGET_ML = "targetMl";

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
        userId = prefs.getInt(KEY_USER_ID, -1);
        String username = prefs.getString(KEY_USERNAME, "Username");
        String email = prefs.getString(KEY_EMAIL, "user@example.com");

        TextView usernameText = findViewById(R.id.username_text);
        TextView emailText = findViewById(R.id.email_text);
        TextView tvTodayProgress = findViewById(R.id.tvTodayProgress);

        usernameText.setText(username);
        emailText.setText(email);

        if (userId != -1) {
            DatabaseHelper db = new DatabaseHelper(this);
            int currentMl = db.getTodayTotalIntake(userId);
            int targetMl = prefs.getInt(KEY_TARGET_ML + "_" + userId, 2000);
            tvTodayProgress.setText(getString(R.string.today_water_value_format, currentMl, targetMl));
        }

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());

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
            } else if (itemId == R.id.navigation_me) {
                return true;
            }
            return false;
        });
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(MeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
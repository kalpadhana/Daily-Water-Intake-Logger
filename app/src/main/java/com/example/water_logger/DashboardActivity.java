package com.example.water_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private WaterLevelView waterView;
    private TextView tvRemaining, tvTargetMl, tvTargetPercent;
    private int targetMl;
    private int currentMl = 0;
    private DatabaseHelper db;

    private static final String PREFS_NAME = "WaterLoggerPrefs";
    private static final String KEY_TARGET_ML = "targetMl";

    private final ActivityResultLauncher<Intent> drinkActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    int amount = result.getData().getIntExtra("amount", 0);
                    if (amount > 0) {
                        addMl(amount);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        targetMl = prefs.getInt(KEY_TARGET_ML, 2000); // Load the saved goal, default to 2000

        db = new DatabaseHelper(this);

        waterView = findViewById(R.id.waterView);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvTargetMl = findViewById(R.id.tvTargetMl);
        tvTargetPercent = findViewById(R.id.tvTargetPercent);

        View targetCard = findViewById(R.id.target_card);
        View reminderCard = findViewById(R.id.reminder_card);

        targetCard.setOnClickListener(v -> showSetGoalDialog());

        reminderCard.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ReminderActivity.class);
            startActivity(intent);
        });

        Button btn150 = findViewById(R.id.btn150);
        Button btn250 = findViewById(R.id.btn250);
        Button btn300 = findViewById(R.id.btn300);
        Button btnDrink = findViewById(R.id.btnDrink);

        btn150.setOnClickListener(v -> addMl(150));
        btn250.setOnClickListener(v -> addMl(250));
        btn300.setOnClickListener(v -> addMl(300));

        btnDrink.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, DrinkActivity.class);
            drinkActivityLauncher.launch(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_today) {
                // Already on the dashboard, do nothing
                return true;
            } else if (itemId == R.id.navigation_history) {
                startActivity(new Intent(DashboardActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.navigation_me) {
                startActivity(new Intent(DashboardActivity.this, MeActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        targetMl = prefs.getInt(KEY_TARGET_ML, 2000); // Reload goal in case it was changed elsewhere
        currentMl = db.getTodayTotalIntake();
        updateUI();
    }

    private void addMl(int add) {
        db.addWaterRecord(add); // Save the record
        currentMl = db.getTodayTotalIntake(); // Reload from database
        updateUI();
        checkIfGoalCompleted();
    }

    private void updateUI() {
        waterView.setTargetMl(targetMl);
        waterView.setCurrentMl(currentMl);

        int remaining = Math.max(targetMl - currentMl, 0);
        tvRemaining.setText(getString(R.string.remaining_ml, remaining));

        tvTargetMl.setText(getString(R.string.target_ml_format, targetMl));

        int percent = 0;
        if (targetMl > 0) {
            percent = (int) ((currentMl * 100.0f) / targetMl);
        }
        tvTargetPercent.setText(getString(R.string.target_percent_format, percent));
    }

    private void showSetGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_target, null);
        builder.setView(dialogView);

        EditText etGoal = dialogView.findViewById(R.id.etGoal);
        etGoal.setText(String.valueOf(targetMl));
        etGoal.requestFocus();

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String goalStr = etGoal.getText().toString();
            if (!goalStr.isEmpty()) {
                try {
                    int newGoal = Integer.parseInt(goalStr);
                    if (newGoal > 0) {
                        targetMl = newGoal;
                        // Save the new goal
                        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putInt(KEY_TARGET_ML, newGoal);
                        editor.apply();

                        updateUI();
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void checkIfGoalCompleted() {
        if (currentMl >= targetMl && !db.isDayCompleted()) {
            db.markDayAsCompleted();
            showGoalCompletedDialog();
        }
    }

    private void showGoalCompletedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Goal Achieved!")
                .setMessage("Congratulations! You\'ve reached your daily water intake goal.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
package com.example.water_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private WaterLevelView waterView;
    private TextView tvRemaining, tvTargetMl, tvTargetPercent, tvGreeting, tvReminderTime, tvReminderLeft;
    private int targetMl;
    private int currentMl = 0;
    private DatabaseHelper db;
    private int userId;
    private String userName = "";

    private static final String PREFS_NAME = "WaterLoggerPrefs";
    private static final String KEY_USER_ID = "userId";

    private Handler reminderHandler = new Handler(Looper.getMainLooper());
    private Runnable reminderRunnable;

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
        userId = prefs.getInt(KEY_USER_ID, -1);

        if (userId == -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        db = new DatabaseHelper(this);
        db.archiveYesterdayIfMissing(userId);
        targetMl = db.getUserTarget(userId);

        // Fetch User Name
        Cursor cursor = db.getUserDetails(userId);
        if (cursor != null && cursor.moveToFirst()) {
            userName = cursor.getString(0);
            cursor.close();
        }

        int prefKeyTarget = prefs.getInt("targetMl_" + userId, -1);
        if (prefKeyTarget > 0 && prefKeyTarget != targetMl) {
            db.setUserTarget(userId, prefKeyTarget);
            targetMl = prefKeyTarget;
        }

        waterView = findViewById(R.id.waterView);
        tvRemaining = findViewById(R.id.tvRemainingLabel);
        tvTargetMl = findViewById(R.id.tvTargetMl);
        tvTargetPercent = findViewById(R.id.tvTargetPercent);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        tvReminderLeft = findViewById(R.id.tvReminderLeft);

        View targetCard = findViewById(R.id.target_card);
        View reminderCard = findViewById(R.id.reminder_card);

        if (targetCard != null) targetCard.setOnClickListener(v -> showSetGoalDialog());

        if (reminderCard != null) {
            reminderCard.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, ReminderActivity.class);
                startActivity(intent);
            });
        }

        View btn150 = findViewById(R.id.btn150);
        View btn250 = findViewById(R.id.btn250);
        View btn300 = findViewById(R.id.btn300);
        Button btnDrink = findViewById(R.id.btnDrink);

        if (btn150 != null) btn150.setOnClickListener(v -> addMl(150));
        if (btn250 != null) btn250.setOnClickListener(v -> addMl(250));
        if (btn300 != null) btn300.setOnClickListener(v -> addMl(300));

        if (btnDrink != null) {
            btnDrink.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, DrinkActivity.class);
                drinkActivityLauncher.launch(intent);
            });
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_today) {
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

        updateNextReminder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != -1) {
            db.archiveYesterdayIfMissing(userId);
            targetMl = db.getUserTarget(userId);

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int prefKeyTarget = prefs.getInt("targetMl_" + userId, -1);
            if (prefKeyTarget > 0 && prefKeyTarget != targetMl) {
                db.setUserTarget(userId, prefKeyTarget);
                targetMl = prefKeyTarget;
            }

            currentMl = db.getTodayTotalIntake(userId);
            updateUI();
            startReminderUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReminderUpdates();
    }

    private void startReminderUpdates() {
        reminderRunnable = new Runnable() {
            @Override
            public void run() {
                updateNextReminder();
                reminderHandler.postDelayed(this, 60000); // Update every minute
            }
        };
        reminderHandler.post(reminderRunnable);
    }

    private void stopReminderUpdates() {
        if (reminderRunnable != null) {
            reminderHandler.removeCallbacks(reminderRunnable);
        }
    }

    private void updateNextReminder() {
        List<Reminder> reminders = db.getUserReminders(userId);
        if (reminders.isEmpty()) {
            if (tvReminderTime != null) tvReminderTime.setText("--:--");
            if (tvReminderLeft != null) tvReminderLeft.setText("No active reminders");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String nowTimeStr = sdf.format(new Date());
        
        Reminder nextReminder = null;

        // Find next active reminder today
        for (Reminder reminder : reminders) {
            if (reminder.isEnabled() && reminder.getTime().compareTo(nowTimeStr) > 0) {
                if (nextReminder == null || reminder.getTime().compareTo(nextReminder.getTime()) < 0) {
                    nextReminder = reminder;
                }
            }
        }

        // If none left today, find the first active one from tomorrow
        if (nextReminder == null) {
            for (Reminder reminder : reminders) {
                if (reminder.isEnabled()) {
                    if (nextReminder == null || reminder.getTime().compareTo(nextReminder.getTime()) < 0) {
                        nextReminder = reminder;
                    }
                }
            }
        }

        if (nextReminder != null && tvReminderTime != null) {
            tvReminderTime.setText(nextReminder.getTime());
            
            try {
                Date now = new Date();
                Date targetDate = sdf.parse(nextReminder.getTime());
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(targetDate);
                Calendar nowCal = Calendar.getInstance();
                cal.set(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.MONTH), nowCal.get(Calendar.DAY_OF_MONTH));
                
                if (cal.getTime().before(now)) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                
                long diffMillis = cal.getTimeInMillis() - now.getTime();
                long hours = diffMillis / (1000 * 60 * 60);
                long minutes = (diffMillis / (1000 * 60)) % 60;
                
                String leftText = (hours > 0 ? hours + "h " : "") + minutes + "m left";
                if (tvReminderLeft != null) tvReminderLeft.setText(leftText);
                
            } catch (Exception e) {
                if (tvReminderLeft != null) tvReminderLeft.setText("Upcoming");
            }
        } else if (tvReminderTime != null) {
            tvReminderTime.setText("--:--");
            if (tvReminderLeft != null) tvReminderLeft.setText("No active reminders");
        }
    }

    private void addMl(int add) {
        if (db.isDayCompleted(userId)) {
            Toast.makeText(this, "Daily goal already completed — additional entries ignored for today.", Toast.LENGTH_SHORT).show();
            currentMl = db.getTodayTotalIntake(userId);
            updateUI();
            return;
        }

        db.addWaterRecord(userId, add);
        currentMl = db.getTodayTotalIntake(userId);
        updateUI();
        checkIfGoalCompleted();
    }

    private void updateUI() {
        if (waterView != null) {
            waterView.setTargetMl(targetMl);
            waterView.setCurrentMl(currentMl);
        }

        int remaining = Math.max(targetMl - currentMl, 0);
        if (tvRemaining != null) tvRemaining.setText(getString(R.string.remaining_ml, remaining));
        if (tvTargetMl != null) tvTargetMl.setText(targetMl + " ml");

        int percent = 0;
        if (targetMl > 0) {
            percent = (int) ((currentMl * 100.0f) / targetMl);
        }
        if (tvTargetPercent != null) tvTargetPercent.setText(percent + "% reached");

        updateGreeting();
    }

    private void updateGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning ☀️";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon ☀️";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening 🌙";
        } else {
            greeting = "Good Night 🌙";
        }

        if (!userName.isEmpty()) {
            greeting += " " + userName;
        }
        if (tvGreeting != null) tvGreeting.setText(greeting);
    }

    private void showSetGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_target, null);
        builder.setView(dialogView);

        EditText etGoal = dialogView.findViewById(R.id.etGoal);
        if (etGoal != null) {
            etGoal.setText(String.valueOf(targetMl));
            etGoal.requestFocus();
        }

        AlertDialog dialog = builder.create();

        View btnSave = dialogView.findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String goalStr = etGoal.getText().toString();
                if (!goalStr.isEmpty()) {
                    try {
                        int newGoal = Integer.parseInt(goalStr);
                        if (newGoal > 0) {
                            targetMl = newGoal;
                            db.setUserTarget(userId, newGoal);

                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putInt("targetMl_" + userId, newGoal);
                            editor.apply();

                            TargetUpdateBus.notifyTargetUpdated(userId, newGoal);

                            updateUI();
                            dialog.dismiss();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        View btnCancel = dialogView.findViewById(R.id.btnCancel);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void checkIfGoalCompleted() {
        if (currentMl >= targetMl && !db.isDayCompleted(userId)) {
            db.markDayAsCompleted(userId);
            showGoalCompletedDialog();
        }
    }

    private void showGoalCompletedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Goal Achieved!")
                .setMessage("Congratulations! You've reached your daily water intake goal.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
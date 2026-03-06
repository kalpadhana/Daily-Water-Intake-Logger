package com.example.water_logger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ReminderActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WaterLoggerPrefs";
    private static final String KEY_USER_ID = "userId";
    private ReminderAdapter adapter;
    private DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getInt(KEY_USER_ID, -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            }
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewReminders);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            List<Reminder> reminders = loadReminders();
            adapter = new ReminderAdapter(reminders);
            recyclerView.setAdapter(adapter);
        }

        Button btnSave = findViewById(R.id.btnSaveReminder);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                if (userId != -1) {
                    db.saveUserReminders(userId, adapter.getReminders());
                    Toast.makeText(this, "Reminder settings saved!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ReminderActivity.this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "User session error. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private List<Reminder> loadReminders() {
        List<Reminder> reminders = db.getUserReminders(userId);
        if (reminders.isEmpty()) {
            // Default reminders if none exist in DB
            reminders.add(new Reminder("After Wake-up", "08:00", true));
            reminders.add(new Reminder("Before Breakfast", "08:20", true));
            reminders.add(new Reminder("After Breakfast", "09:30", true));
            reminders.add(new Reminder("Before Lunch", "11:00", true));
            reminders.add(new Reminder("After Lunch", "13:00", true));
            reminders.add(new Reminder("Before Dinner", "18:00", true));
            reminders.add(new Reminder("After Dinner", "20:00", true));
            reminders.add(new Reminder("Before Sleep", "22:40", true));
        }
        return reminders;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
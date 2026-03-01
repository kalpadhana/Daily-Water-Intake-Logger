package com.example.water_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ReminderActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ReminderPrefs";
    private ReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Reminder> reminders = loadReminders();

        adapter = new ReminderAdapter(reminders);
        recyclerView.setAdapter(adapter);

        Button btnSave = findViewById(R.id.btnSaveReminder);
        btnSave.setOnClickListener(v -> {
            saveReminders();
            Toast.makeText(this, "Reminder settings saved!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ReminderActivity.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // To prevent the user from coming back to this screen on back press
        });
    }

    private List<Reminder> loadReminders() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(new Reminder("After Wake-up", "08:00", prefs.getBoolean("reminder_0", true)));
        reminders.add(new Reminder("Before Breakfast", "08:20", prefs.getBoolean("reminder_1", true)));
        reminders.add(new Reminder("After Breakfast", "09:30", prefs.getBoolean("reminder_2", true)));
        reminders.add(new Reminder("Before Lunch", "11:00", prefs.getBoolean("reminder_3", true)));
        reminders.add(new Reminder("After Lunch", "13:00", prefs.getBoolean("reminder_4", true)));
        reminders.add(new Reminder("Before Dinner", "18:00", prefs.getBoolean("reminder_5", true)));
        reminders.add(new Reminder("After Dinner", "20:00", prefs.getBoolean("reminder_6", true)));
        reminders.add(new Reminder("Before Sleep", "22:40", prefs.getBoolean("reminder_7", true)));
        return reminders;
    }

    private void saveReminders() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        List<Reminder> reminders = adapter.getReminders();
        for (int i = 0; i < reminders.size(); i++) {
            editor.putBoolean("reminder_" + i, reminders.get(i).isEnabled());
        }
        editor.apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
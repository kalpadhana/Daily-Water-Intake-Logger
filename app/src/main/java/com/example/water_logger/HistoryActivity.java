package com.example.water_logger;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_history);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_today) {
                startActivity(new Intent(HistoryActivity.this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.navigation_history) {
                // Already on this screen, do nothing
                return true;
            } else if (itemId == R.id.navigation_insights) {
                startActivity(new Intent(HistoryActivity.this, InsightsActivity.class));
                return true;
            } else if (itemId == R.id.navigation_me) {
                startActivity(new Intent(HistoryActivity.this, MeActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
package com.example.water_logger;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {

    private WaterLevelView waterView;
    private TextView tvRemaining, tvTargetMl, tvTargetPercent;
    private int targetMl = 2000;
    private int currentMl = 750;

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

        waterView = findViewById(R.id.waterView);
        tvRemaining = findViewById(R.id.tvRemainingLabel);
        tvTargetMl = findViewById(R.id.tvTargetMl);
        tvTargetPercent = findViewById(R.id.tvTargetPercent);

        findViewById(R.id.target_card).setOnClickListener(v -> showSetGoalDialog());
        findViewById(R.id.reminder_card).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
            startActivity(intent);
        });

        Button btn150 = findViewById(R.id.btn150);
        Button btn250 = findViewById(R.id.btn250);
        Button btn300 = findViewById(R.id.btn300);
        Button btnDrink = findViewById(R.id.btnDrink);

        updateUI();

        btn150.setOnClickListener(v -> addMl(150));
        btn250.setOnClickListener(v -> addMl(250));
        btn300.setOnClickListener(v -> addMl(300));

        btnDrink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DrinkActivity.class);
            drinkActivityLauncher.launch(intent);
        });
    }

    private void addMl(int add) {
        currentMl += add;
        updateUI();
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
                        updateUI();
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    // You could show a Toast here to inform the user of invalid input
                }
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
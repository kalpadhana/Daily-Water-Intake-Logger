package com.example.water_logger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DrinkActivity extends AppCompatActivity {

    private EditText etWaterAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_drink);

            ImageView ivClose = findViewById(R.id.ivClose);
            etWaterAmount = findViewById(R.id.etWaterAmount);
            Button btnDrink = findViewById(R.id.btnDrink);

            ivClose.setOnClickListener(v -> finish());

            btnDrink.setOnClickListener(v -> {
                String amountStr = etWaterAmount.getText().toString().trim();
                if (!amountStr.isEmpty()) {
                    try {
                        int amount = Integer.parseInt(amountStr);
                        if (amount > 0) {
                            Intent result = new Intent();
                            result.putExtra("amount", amount);
                            setResult(RESULT_OK, result);
                            finish();
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            });

        } catch (Exception e) {
            Log.e("DrinkActivity", "Error inflating layout", e);
            Toast.makeText(this, "Error loading layout: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

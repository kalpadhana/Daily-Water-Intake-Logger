package com.example.water_logger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPhoneNumber, etPassword, etConfirmPassword;
    Button btnRegister;
    ImageButton btnBack;
    CheckBox cbTerms;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
        cbTerms = findViewById(R.id.cbTerms);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {

            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhoneNumber.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.isEmpty() && phone.length() != 10) {
                Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "You must accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = db.insertUser(name, email, phone, password);

            if (inserted) {
                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
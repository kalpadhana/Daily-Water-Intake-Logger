package com.example.water_logger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegisterNow;
    DatabaseHelper db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);

        tvRegisterNow.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean validUser = db.checkUser(email, password);

            if (validUser) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(LoginActivity.this, GetStartedActivity.class);
                startActivity(i);
                finish(); // prevents going back to login when pressing back
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
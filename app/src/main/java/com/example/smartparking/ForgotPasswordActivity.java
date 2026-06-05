package com.example.smartparking;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnReset;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etForgotEmail);
        btnReset = findViewById(R.id.btnSendResetLink);
        progressBar = findViewById(R.id.pbForgot);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("يرجى إدخال البريد الإلكتروني");
                etEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("يرجى إدخال بريد إلكتروني صحيح");
                etEmail.requestFocus();
                return;
            }

            sendResetEmail(email);
        });
    }

    private void sendResetEmail(String email) {
        btnReset.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnReset.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "تم إرسال الرابط! تفقد بريدك الإلكتروني.", Toast.LENGTH_LONG).show();
                        finish(); // العودة لشاشة تسجيل الدخول
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "حدث خطأ غير متوقع";
                        Toast.makeText(ForgotPasswordActivity.this,
                                "فشل الإرسال: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
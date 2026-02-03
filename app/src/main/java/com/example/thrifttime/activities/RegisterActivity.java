package com.example.thrifttime.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.thrifttime.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etNickname, etUsername;
    private Button btnDoRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.registerEmail);
        etPassword = findViewById(R.id.registerPassword);
        etNickname = findViewById(R.id.editTextNickname);
        etUsername = findViewById(R.id.editTextUsername);
        btnDoRegister = findViewById(R.id.btnDoRegister);
        progressBar = findViewById(R.id.progressBarRegister);

        btnDoRegister.setOnClickListener(v -> checkUsernameAndRegister());
    }

    private void checkUsernameAndRegister() {
        // Industrial Practice: Usernames are stored in lowercase to prevent duplicates like "User" and "user"
        String rawUsername = etUsername.getText().toString().trim().toLowerCase();
        String finalUsername = rawUsername.startsWith("@") ? rawUsername : "@" + rawUsername;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();

        // 1. Basic Empty Check
        if (nickname.isEmpty() || rawUsername.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Username Format Check (No spaces)
        if (rawUsername.contains(" ")) {
            etUsername.setError("Username cannot contain spaces");
            etUsername.requestFocus();
            return;
        }

        // --- PASSWORD SECURITY CHECKS ---
        if (password.length() < 8) {
            etPassword.setError("Must be at least 8 characters");
            etPassword.requestFocus();
            return;
        }

        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            etPassword.setError("Must contain at least 1 Uppercase letter");
            etPassword.requestFocus();
            return;
        }

        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            etPassword.setError("Must contain at least 1 Number (0-9)");
            etPassword.requestFocus();
            return;
        }

        if (!Pattern.compile("[!@#$%^&*+=?_~-]").matcher(password).find()) {
            etPassword.setError("Must contain at least 1 Symbol (!@#$%)");
            etPassword.requestFocus();
            return;
        }

        // Start Loading UI
        setLoading(true);

        // --- UNIQUE USERNAME CHECK ---
        db.collection("users")
                .whereEqualTo("username", finalUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Username is available, proceed to Auth
                        performRegistration(email, password, nickname, finalUsername);
                    } else {
                        // Username taken
                        setLoading(false);
                        etUsername.setError("Username already taken!");
                        etUsername.requestFocus();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void performRegistration(String email, String password, String nickname, String finalUsername) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // Prepare User Data for Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("displayName", nickname);
                    user.put("username", finalUsername);
                    user.put("email", email);
                    user.put("userId", uid);
                    user.put("photoUrl", "");
                    user.put("points", 0);
                    user.put("userType", "User");

                    db.collection("users").document(uid).set(user)
                            .addOnSuccessListener(aVoid -> {
                                setLoading(false);
                                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                Toast.makeText(this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            btnDoRegister.setEnabled(false);
            btnDoRegister.setText("Creating...");
        } else {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            btnDoRegister.setEnabled(true);
            btnDoRegister.setText("Sign Up");
        }
    }
}
package com.example.thrifttime.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.thrifttime.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etUsername, etEmail, etOldPass, etNewPass;
    private Button btnSave;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        etName = findViewById(R.id.etEditName);
        etUsername = findViewById(R.id.etEditUsername);
        etEmail = findViewById(R.id.etEditEmail);
        etOldPass = findViewById(R.id.etOldPassword);
        etNewPass = findViewById(R.id.etNewPassword);
        btnSave = findViewById(R.id.btnSaveChanges);

        loadCurrentData();

        btnSave.setOnClickListener(v -> attemptUpdate());
    }

    private void loadCurrentData() {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etName.setText(doc.getString("displayName"));
                        etUsername.setText(doc.getString("username"));
                        etEmail.setText(user.getEmail()); // Get email from Auth, not Firestore
                    }
                });
    }

    private void attemptUpdate() {
        String oldPass = etOldPass.getText().toString();

        // 1. Force User to Enter Old Password
        if (oldPass.isEmpty()) {
            etOldPass.setError("Current password is required to save changes");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Verifying...");

        // 2. Re-Authenticate (Verify Password)
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

        user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
            // Password Verified! Now update data.
            updateDetails();
        }).addOnFailureListener(e -> {
            btnSave.setEnabled(true);
            btnSave.setText("Save Changes");
            Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateDetails() {
        String newName = etName.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();

        // 3. Update Email (if changed)
        if (!newEmail.equals(user.getEmail())) {
            user.updateEmail(newEmail);
        }

        // 4. Update Password (if entered)
        if (!newPass.isEmpty()) {
            if (newPass.length() < 8) {
                Toast.makeText(this, "New password too short!", Toast.LENGTH_SHORT).show();
                return;
            }
            user.updatePassword(newPass);
        }

        // 5. Update Firestore Data
        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", newName);
        updates.put("username", newUsername);
        updates.put("email", newEmail);

        db.collection("users").document(user.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
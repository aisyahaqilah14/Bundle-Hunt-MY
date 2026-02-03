package com.example.thrifttime.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat; // Import for Notifications

import com.example.thrifttime.R;
import com.example.thrifttime.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddPostActivity extends AppCompatActivity {

    private EditText etDescription;
    private ImageView ivPreview;
    private TextView tvLocationPreview;
    private Button btnPhoto, btnLocation, btnPost;
    private ProgressBar progressBar;

    private Uri imageUri;
    private String locationString = "";
    private FirebaseFirestore db;

    // 1. Camera Launcher
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String uriStr = result.getData().getStringExtra("imageUri");
                    if (uriStr != null) {
                        imageUri = Uri.parse(uriStr);
                        ivPreview.setImageURI(imageUri);
                        ivPreview.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    // 2. Gallery Launcher
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivPreview.setImageURI(imageUri);
                    ivPreview.setVisibility(View.VISIBLE);
                }
            }
    );

    // 3. Map Launcher
    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    locationString = result.getData().getStringExtra("selectedAddress");
                    tvLocationPreview.setText("📍 " + locationString);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        db = FirebaseFirestore.getInstance();

        etDescription = findViewById(R.id.etDescription);
        ivPreview = findViewById(R.id.ivPreview);
        tvLocationPreview = findViewById(R.id.tvLocationPreview);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnLocation = findViewById(R.id.btnLocation);
        btnPost = findViewById(R.id.btnPost);
        progressBar = findViewById(R.id.progressBar);

        // Create Notification Channel (Required for Android 8+)
        createNotificationChannel();

        btnPhoto.setOnClickListener(v -> {
            String[] options = {"Take Photo", "Choose from Gallery"};
            new AlertDialog.Builder(this)
                    .setTitle("Select Image")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            cameraLauncher.launch(new Intent(this, CameraActivity.class));
                        } else {
                            galleryLauncher.launch("image/*");
                        }
                    }).show();
        });

        btnLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapLauncher.launch(intent);
        });

        btnPost.setOnClickListener(v -> uploadPost());
    }

    private void uploadPost() {
        String desc = etDescription.getText().toString().trim();
        if (imageUri == null) {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnPost.setEnabled(false);

        StorageReference ref = FirebaseStorage.getInstance().getReference("posts/" + UUID.randomUUID().toString());
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    savePostToFirestore(desc, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnPost.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void savePostToFirestore(String description, String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            String name = doc.getString("displayName");
            String handle = doc.getString("username");
            String pfp = doc.getString("photoUrl");

            if (name == null || name.isEmpty()) {
                name = (handle != null && !handle.isEmpty()) ? handle : "Thrifter";
            }
            if (handle == null) handle = "";

            Post post = new Post(userId, name, pfp, description, imageUrl, locationString, System.currentTimeMillis());
            post.setHandle(handle);

            db.collection("posts").add(post).addOnSuccessListener(ref -> {
                ref.update("postId", ref.getId());

                // 1. TRIGGER NOTIFICATION HERE
                sendUploadSuccessNotification();

                Toast.makeText(this, "Posted!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnPost.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // --- NEW NOTIFICATION LOGIC ---

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Upload Status";
            String description = "Notifications for post uploads";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("upload_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendUploadSuccessNotification() {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "upload_channel")
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done) // Standard System Icon
                    .setContentTitle("Upload Complete")
                    .setContentText("Your thrift find is now live! 👕")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(100, builder.build());
            }
        } catch (SecurityException e) {
            // If permission is missing on Android 13+, it just won't show. No crash.
            e.printStackTrace();
        }
    }
}
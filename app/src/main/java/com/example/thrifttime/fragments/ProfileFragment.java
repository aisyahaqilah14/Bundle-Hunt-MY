package com.example.thrifttime.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.thrifttime.R;
import com.example.thrifttime.activities.EditProfileActivity;
import com.example.thrifttime.activities.MainActivity; // Or your LoginActivity
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private ImageView imageViewProfile;
    private TextView textViewName, textViewEmail;
    private Button buttonEditProfile, buttonLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String currentUserId;

    // 1. Gallery Launcher
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Update UI immediately for better UX
                    imageViewProfile.setImageURI(uri);
                    // Start Upload
                    uploadImageToFirebase(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Bind Views (Ensure IDs match your fragment_profile.xml)
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        textViewName = view.findViewById(R.id.textViewName); // Check your XML for this ID
        textViewEmail = view.findViewById(R.id.textViewEmail);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // Load current data
        loadUserProfile();

        // 2. Click Image to Upload
        imageViewProfile.setOnClickListener(v -> openGallery());

        // 3. Edit Button (Can be used for text edit, but letting it change photo for now too)
        buttonEditProfile.setOnClickListener(v -> {
            // Open the new Edit Activity
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // Navigate back to Login
            // Intent intent = new Intent(getActivity(), LoginActivity.class);
            // startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void loadUserProfile() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("displayName");
                        String email = documentSnapshot.getString("email");
                        String photoUrl = documentSnapshot.getString("photoUrl");

                        if (name != null) textViewName.setText(name);
                        if (email != null) textViewEmail.setText(email);

                        // Load existing pfp
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_profile) // Make sure this drawable exists
                                    .into(imageViewProfile);
                        }
                    }
                });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();

        // Save to: profile_images/USER_ID.jpg
        StorageReference fileRef = storageRef.child("profile_images/" + currentUserId + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    updateFirestore(downloadUrl);
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateFirestore(String downloadUrl) {
        db.collection("users").document(currentUserId)
                .update("photoUrl", downloadUrl)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
                );
    }
}
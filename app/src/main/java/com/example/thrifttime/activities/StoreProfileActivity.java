package com.example.thrifttime.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.thrifttime.R;
import com.example.thrifttime.adapters.PhotoAdapter;
import com.example.thrifttime.models.Store;

import java.util.ArrayList;
import java.util.List;

public class StoreProfileActivity extends AppCompatActivity {

    private ImageView imageViewStore;
    private TextView textViewStoreName, textViewAddress, textViewHours, textViewDescription;
    private RatingBar ratingBar;
    private Button buttonTakePhoto, buttonGetDirections, buttonWriteReview;
    private RecyclerView recyclerViewPhotos;

    private String storeId;
    private Store store;
    private FirebaseFirestore db;
    private PhotoAdapter photoAdapter;
    private List<String> photoUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_profile);

        db = FirebaseFirestore.getInstance();

        // Get store ID from intent
        storeId = getIntent().getStringExtra("store_id");
        if (storeId == null) {
            Toast.makeText(this, "Store not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadStoreData();
    }

    private void initViews() {
        imageViewStore = findViewById(R.id.imageViewStore);
        textViewStoreName = findViewById(R.id.textViewStoreName);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewHours = findViewById(R.id.textViewHours);
        textViewDescription = findViewById(R.id.textViewDescription);
        ratingBar = findViewById(R.id.ratingBar);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        buttonGetDirections = findViewById(R.id.buttonGetDirections);
        buttonWriteReview = findViewById(R.id.buttonWriteReview);
        recyclerViewPhotos = findViewById(R.id.recyclerViewPhotos);

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreProfileActivity.this, CameraActivity.class);
                intent.putExtra("store_id", storeId);
                startActivity(intent);
            }
        });

        buttonGetDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (store != null) {
                    // Open Google Maps with directions
                    String uri = "http://maps.google.com/maps?daddr=" +
                            store.getLatitude() + "," + store.getLongitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });

        buttonWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StoreProfileActivity.this,
                        "Review feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoAdapter(this, photoUrls);
        recyclerViewPhotos.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewPhotos.setAdapter(photoAdapter);
    }

    private void loadStoreData() {
        db.collection("stores").document(storeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        store = task.getResult().toObject(Store.class);
                        if (store != null) {
                            updateUI();
                            loadStorePhotos();
                        }
                    } else {
                        Toast.makeText(this, "Error loading store", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        textViewStoreName.setText(store.getName());
        textViewAddress.setText(store.getAddress());
        textViewHours.setText("Hours: " + store.getOpeningHours());
        textViewDescription.setText(store.getDescription());
        ratingBar.setRating(store.getRating());

        // Load store image if available
        if (store.getPhotoUrls() != null && !store.getPhotoUrls().isEmpty()) {
            Glide.with(this)
                    .load(store.getPhotoUrls().get(0))
                    .placeholder(R.drawable.ic_store)
                    .into(imageViewStore);
        }
    }

    private void loadStorePhotos() {
        db.collection("photos")
                .whereEqualTo("storeId", storeId)
                .limit(12)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        photoUrls.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String photoUrl = document.getString("photoUrl");
                            if (photoUrl != null) {
                                photoUrls.add(photoUrl);
                            }
                        }
                        photoAdapter.notifyDataSetChanged();
                    }
                });
    }
}
package com.example.thrifttime.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.thrifttime.R;

public class FullScreenImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ImageView fullImage = findViewById(R.id.fullImageView);

        // 1. Get string extra using the SAME key as Adapter
        String url = getIntent().getStringExtra("imageUrl");

        // 2. Check if valid
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_photo)
                    .into(fullImage);
        } else {
            Toast.makeText(this, "Error: Image not found", Toast.LENGTH_SHORT).show();
            finish(); // Close if no image
        }

        // 3. Close on click
        fullImage.setOnClickListener(v -> finish());
    }
}
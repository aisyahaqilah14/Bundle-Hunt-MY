package com.example.thrifttime.adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thrifttime.R;
import com.example.thrifttime.activities.FullScreenImageActivity;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private Context context;
    private List<String> photoUrls;

    public PhotoAdapter(Context context, List<String> photoUrls) {
        this.context = context;
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);

        Glide.with(context)
                .load(photoUrl)
                .placeholder(R.drawable.ic_photo)
                .centerCrop()
                .into(holder.imageViewPhoto);

        // Add this click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenImageActivity.class);
            intent.putExtra("imageUrl", photoUrl);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
        }
    }
}
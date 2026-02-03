package com.example.thrifttime.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.thrifttime.R;
import com.example.thrifttime.activities.CommentActivity;
import com.example.thrifttime.activities.FullScreenImageActivity;
import com.example.thrifttime.databinding.ItemPostBinding;
import com.example.thrifttime.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private String currentUserId;
    private FirebaseFirestore db;

    public FeedAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(context), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // --- STRICT USERNAME LOGIC (Instagram Style) ---
        String handle = post.getHandle();

        if (handle != null && !handle.isEmpty()) {
            if (!handle.startsWith("@")) {
                holder.binding.tvUserName.setText("@" + handle);
            } else {
                holder.binding.tvUserName.setText(handle);
            }
        } else {
            holder.binding.tvUserName.setText("@loading...");
            db.collection("users").document(post.getUserId()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String realHandle = documentSnapshot.getString("username");
                            if (realHandle != null && !realHandle.isEmpty()) {
                                holder.binding.tvUserName.setText(realHandle.startsWith("@") ? realHandle : "@" + realHandle);
                            } else {
                                String realName = documentSnapshot.getString("displayName");
                                holder.binding.tvUserName.setText(realName != null ? realName : "@unknown");
                            }
                            String realPfp = documentSnapshot.getString("photoUrl");
                            if (realPfp != null && !realPfp.isEmpty()) {
                                Glide.with(context).load(realPfp).placeholder(R.drawable.ic_profile).into(holder.binding.ivUserProfile);
                            }
                        }
                    });
        }

        holder.binding.tvDescription.setText(post.getDescription());
        holder.binding.tvLikeCount.setText(String.valueOf(post.getLikesCount()));

        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            holder.binding.tvLocation.setVisibility(View.VISIBLE);
            holder.binding.tvLocation.setText("📍 " + post.getLocation());
        } else {
            holder.binding.tvLocation.setVisibility(View.GONE);
        }

        Glide.with(context).load(post.getUserProfileUrl()).placeholder(R.drawable.ic_profile).into(holder.binding.ivUserProfile);
        Glide.with(context).load(post.getImageUrl()).placeholder(R.drawable.ic_launcher_background).into(holder.binding.ivPostImage);

        holder.binding.ivPostImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenImageActivity.class);
            intent.putExtra("imageUrl", post.getImageUrl());
            context.startActivity(intent);
        });

        boolean isLiked = post.getLikedBy().contains(currentUserId);
        holder.binding.btnLike.setImageResource(isLiked ? R.drawable.ic_liked : R.drawable.ic_like_border);

        holder.binding.btnLike.setOnClickListener(v -> {
            if (post.getLikedBy().contains(currentUserId)) {
                holder.binding.btnLike.setImageResource(R.drawable.ic_like_border);
                db.collection("posts").document(post.getPostId()).update("likesCount", FieldValue.increment(-1), "likedBy", FieldValue.arrayRemove(currentUserId));
                post.getLikedBy().remove(currentUserId);
                post.setLikesCount(post.getLikesCount() - 1);
            } else {
                holder.binding.btnLike.setImageResource(R.drawable.ic_liked);
                db.collection("posts").document(post.getPostId()).update("likesCount", FieldValue.increment(1), "likedBy", FieldValue.arrayUnion(currentUserId));
                post.getLikedBy().add(currentUserId);
                post.setLikesCount(post.getLikesCount() + 1);
            }
            holder.binding.tvLikeCount.setText(String.valueOf(post.getLikesCount()));
        });

        holder.binding.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.getPostId());
            context.startActivity(intent);
        });

        holder.binding.btnShare.setOnClickListener(v -> {
            String shareBody = "Check out this post by " + holder.binding.tvUserName.getText() + ":\n" + post.getDescription();
            shareImageAndText(context, post.getImageUrl(), shareBody);
        });

        // --- DELETE POST TRIGGER ---
        holder.itemView.setOnLongClickListener(v -> {
            if (post.getUserId().equals(currentUserId)) {
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to remove this find?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deletePost(post, position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            return true;
        });
    }

    // --- NEW: THE MISSING DELETE METHOD ---
    private void deletePost(Post post, int position) {
        // 1. Delete from Firestore
        db.collection("posts").document(post.getPostId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. Delete Image from Storage to save space
                    if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(post.getImageUrl())
                                .delete()
                                .addOnFailureListener(e -> Log.e("Delete", "Image cleanup failed"));
                    }

                    // 3. Update UI instantly
                    postList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void shareImageAndText(Context context, String imageUrl, String caption) {
        Glide.with(context).asBitmap().load(imageUrl).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                try {
                    File cachePath = new File(context.getCacheDir(), "images");
                    cachePath.mkdirs();
                    File newFile = new File(cachePath, "share_image.png");
                    FileOutputStream stream = new FileOutputStream(newFile);
                    resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();
                    Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", newFile);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/*");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, caption);
                    context.startActivity(Intent.createChooser(shareIntent, "Share Post"));
                } catch (Exception e) {
                    Toast.makeText(context, "Error sharing", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {}
        });
    }

    @Override
    public int getItemCount() { return postList.size(); }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ItemPostBinding binding;
        public PostViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
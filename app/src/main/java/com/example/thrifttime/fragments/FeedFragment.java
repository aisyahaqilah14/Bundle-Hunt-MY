package com.example.thrifttime.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thrifttime.R;
import com.example.thrifttime.activities.AddPostActivity;
import com.example.thrifttime.adapters.FeedAdapter; // CHANGED
import com.example.thrifttime.models.Post;         // CHANGED
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerViewFeed;
    private TextView textViewEmpty;
    private FeedAdapter feedAdapter; // Using the new Adapter
    private List<Post> postList;     // Using the new Model
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();

        // Initialize Views
        // Note: Make sure your XML ID is 'recyclerViewFeed' (or change this line to 'recyclerViewStores')
        recyclerViewFeed = view.findViewById(R.id.recyclerViewFeed);
        textViewEmpty = view.findViewById(R.id.textViewEmpty); // Ensure this exists in XML or remove this line

        // Setup RecyclerView
        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        feedAdapter = new FeedAdapter(getContext(), postList);
        recyclerViewFeed.setAdapter(feedAdapter);

        // Load the Social Feed
        loadPosts();

        // FAB Logic
        // Inside FeedFragment.java
        FloatingActionButton fabAddPost = view.findViewById(R.id.fabAddPost);
        fabAddPost.setOnClickListener(v -> {
            // Ensure this opens AddPostActivity
            Intent intent = new Intent(getActivity(), AddPostActivity.class);
            startActivity(intent);
        });

        return view;
    }

    // Refresh feed when user comes back from posting
    @Override
    public void onResume() {
        super.onResume();
        loadPosts();
    }

    private void loadPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Show newest first!
                .limit(50) // Load last 50 posts
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Post post = document.toObject(Post.class);
                                postList.add(post);
                            } catch (Exception e) {
                                // Skip posts that might have bad data formats
                            }
                        }

                        feedAdapter.notifyDataSetChanged();

                        // Handle Empty State
                        if (postList.isEmpty()) {
                            if (textViewEmpty != null) textViewEmpty.setVisibility(View.VISIBLE);
                            recyclerViewFeed.setVisibility(View.GONE);
                        } else {
                            if (textViewEmpty != null) textViewEmpty.setVisibility(View.GONE);
                            recyclerViewFeed.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(),
                                    "Error loading feed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
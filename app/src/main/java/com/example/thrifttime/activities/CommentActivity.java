package com.example.thrifttime.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.thrifttime.R;
import com.example.thrifttime.adapters.CommentAdapter;
import com.example.thrifttime.models.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private String postId;
    private EditText etComment;
    private ImageButton btnPostComment;
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        etComment = findViewById(R.id.etComment);
        btnPostComment = findViewById(R.id.btnPostComment);
        rvComments = findViewById(R.id.rvComments);

        commentList = new ArrayList<>();
        adapter = new CommentAdapter(commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);

        loadComments();
        btnPostComment.setOnClickListener(v -> postComment());
    }

    private void loadComments() {
    db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                if (value != null) {
                    commentList.clear();
                    commentList.addAll(value.toObjects(Comment.class));
                    adapter.notifyDataSetChanged();
                }
            });
}

private void postComment() {
    String text = etComment.getText().toString().trim();
    if (text.isEmpty()) return;

    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
        String freshName = doc.getString("displayName");
        String commentId = db.collection("posts").document(postId).collection("comments").document().getId();

        Comment comment = new Comment(commentId, userId, freshName, text, System.currentTimeMillis());

        db.collection("posts").document(postId).collection("comments")
                .document(commentId).set(comment)
                .addOnSuccessListener(aVoid -> {
                    // This increments the 'commentCount' field in the Post document
                    db.collection("posts").document(postId)
                            .update("commentCount", FieldValue.increment(1));
                    etComment.setText("");
                    Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show();
                });
    });
}
}
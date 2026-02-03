package com.example.thrifttime.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.thrifttime.models.Comment;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the system layout but we will bind it safely
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Safety check to prevent crashes if data is null
        String user = (comment.getUserName() != null) ? comment.getUserName() : "Anonymous";
        String text = (comment.getCommentText() != null) ? comment.getCommentText() : "";

        holder.tvUser.setText(user);
        holder.tvText.setText(text);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvText;
        CommentViewHolder(View itemView) {
            super(itemView);
            // the standard IDs for simple_list_item_2
            tvUser = itemView.findViewById(android.R.id.text1);
            tvText = itemView.findViewById(android.R.id.text2);
        }
    }
}
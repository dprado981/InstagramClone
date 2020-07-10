package com.codepath.parsetagram;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.parsetagram.data.model.Comment;
import com.codepath.parsetagram.fragments.DetailFragment;
import com.codepath.parsetagram.fragments.ProfileFragment;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final Context context;
    private final List<Comment> comments;

    public static final String TAG = CommentsAdapter.class.getSimpleName();

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment post = comments.get(position);
        holder.bind(post);
    }

    // Clean all elements of the recycler
    public void clear() {
        comments.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Comment> list) {
        comments.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView ivProfileImage;
        private final TextView tvDescriptionUsername;
        private final TextView tvDescription;

        private Comment comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvDescriptionUsername = itemView.findViewById(R.id.tvDescriptionUsername);
            tvDescription = itemView.findViewById(R.id.tvDescription);

            itemView.setOnClickListener(this);
            ivProfileImage.setOnClickListener(this);
            tvDescriptionUsername.setOnClickListener(this);
        }

        @SuppressLint("ResourceType")
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            if (v == ivProfileImage || v == tvDescriptionUsername) {
                Fragment fragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("username", comment.getUser().getUsername());
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            }
        }

        // Bind the Post data to the view elements
        public void bind(Comment comment) {
            this.comment = comment;
            ParseUser user = comment.getUser();

            tvDescriptionUsername.setText(user.getUsername());
            tvDescription.setText(comment.getText());

            ParseFile profileImage = user.getParseFile("profileImage");
            if (profileImage != null) {
                Glide.with(context)
                        .load(profileImage.getUrl())
                        .placeholder(R.drawable.ufi_heart_active)
                        .into(ivProfileImage);
            }

        }

    }
}

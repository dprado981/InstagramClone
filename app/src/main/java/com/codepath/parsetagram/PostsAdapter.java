package com.codepath.parsetagram;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.parsetagram.fragments.ProfileFragment;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;
    private boolean forProfileView;

    public static final String TAG = PostsAdapter.class.getSimpleName();

    public PostsAdapter(Context context, List<Post> posts, boolean forProfileView) {
        this.context = context;
        this.posts = posts;
        this.forProfileView = forProfileView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view, forProfileView);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout llHeader;
        private TextView tvUsername;
        private ImageView ivProfileImage;
        private ImageView ivImage;
        private TextView tvDescription;
        private Post post;

        public ViewHolder(@NonNull View itemView, boolean forProfileView) {
            super(itemView);
            llHeader = itemView.findViewById(R.id.llHeader);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            itemView.setOnClickListener(this);
            ivProfileImage.setOnClickListener(this);
            tvUsername.setOnClickListener(this);
            if(forProfileView) {
                llHeader.setVisibility(View.GONE);
                tvDescription.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            if (v == tvUsername || v == ivProfileImage) {
                Fragment fragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                Log.d(TAG, post.getUser().getObjectId());
                bundle.putString("userId", post.getUser().getObjectId());
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            }

            if (v == itemView) {
                Intent intent = new Intent(context, PostDetail.class);
                intent.putExtra("objectId", post.getObjectId());
                context.startActivity(intent);
            }
        }

        // Bind the Post data to the view elements
        public void bind(Post post) {
            this.post = post;
            ParseUser user = post.getUser();
            tvUsername.setText(post.getUser().getUsername());
            tvDescription.setText(post.getDescription());
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .placeholder(R.drawable.ufi_heart_active)
                        .into(ivImage);
            }
            image = user.getParseFile("profileImage");
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .placeholder(R.drawable.ufi_heart_active)
                        .into(ivProfileImage);
            }
        }
    }
}

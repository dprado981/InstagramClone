package com.codepath.parsetagram;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

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

        private TextView tvUsername;
        private ImageView ivImage;
        private TextView tvDescription;
        private Post post;

        public ViewHolder(@NonNull View itemView, boolean forProfileView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            itemView.setOnClickListener(this);
            if(forProfileView) {
                tvUsername.setVisibility(View.GONE);
                tvDescription.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, PostDetail.class);
            intent.putExtra("objectId", post.getObjectId());
            context.startActivity(intent);
        }

        // Bind the Post data to the view elements
        public void bind(Post post) {
            this.post = post;
            tvDescription.setText(post.getUser().getUsername());
            tvDescription.setText(post.getDescription());
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .placeholder(R.drawable.ufi_heart_active)
                        .into(ivImage);
            }
        }
    }
}

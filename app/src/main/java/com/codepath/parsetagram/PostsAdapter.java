package com.codepath.parsetagram;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.parsetagram.fragments.ProfileFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private final Context context;
    private final List<Post> posts;
    private final boolean forProfileView;

    public static final String TAG = PostsAdapter.class.getSimpleName();

    public PostsAdapter(Context context, List<Post> posts, boolean forProfileView) {
        this.context = context;
        this.posts = posts;
        this.forProfileView = forProfileView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
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

        // TODO: comments

        private final LinearLayout llHeader;
        private final TextView tvHeaderUsername;
        private final ImageView ivProfileImage;
        private final ImageView ivImage;
        private final LinearLayout llButtons;
        private final ImageView ivLike;
        private final ImageView ivComment;
        private final LinearLayout llDescription;
        private final TextView tvDescriptionUsername;
        private final TextView tvDescription;
        private final TextView tvLikeCount;
        private final TextView tvTimestamp;

        private Post post;

        public ViewHolder(@NonNull View itemView, boolean forProfileView) {
            super(itemView);
            llHeader = itemView.findViewById(R.id.llHeader);
            tvHeaderUsername = itemView.findViewById(R.id.tvHeaderUsername);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivImage = itemView.findViewById(R.id.ivImage);
            llButtons = itemView.findViewById(R.id.llButtons);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivComment = itemView.findViewById(R.id.ivComment);
            llDescription = itemView.findViewById(R.id.llDescription);
            tvDescriptionUsername = itemView.findViewById(R.id.tvDescriptionUsername);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);

            itemView.setOnClickListener(this);
            ivProfileImage.setOnClickListener(this);
            tvHeaderUsername.setOnClickListener(this);
            ivLike.setOnClickListener(this);
            ivComment.setOnClickListener(this);
            tvDescriptionUsername.setOnClickListener(this);

            if(forProfileView) {
                llHeader.setVisibility(View.GONE);
                llButtons.setVisibility(View.GONE);
                llDescription.setVisibility(View.GONE);
                tvLikeCount.setVisibility(View.GONE);
                tvTimestamp.setVisibility(View.GONE);
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            if (v == tvHeaderUsername || v == ivProfileImage || v == tvDescriptionUsername) {
                Fragment fragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                Log.d(TAG, post.getUser().getObjectId());
                bundle.putString("userId", post.getUser().getObjectId());
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            }

            if (v == ivLike) {
                ParseUser user = ParseUser.getCurrentUser();
                List<String> likesList = post.getLikesList();
                if (likesList.contains(user.getUsername())) {
                    post.deleteLike(user);
                    Glide.with(context)
                            .load(R.drawable.ufi_heart)
                            .placeholder(R.drawable.ufi_heart)
                            .into(ivLike);
                } else {
                    post.addLike(user);
                    Glide.with(context)
                            .load(R.drawable.ufi_heart_active)
                            .placeholder(R.drawable.ufi_heart_active)
                            .into(ivLike);
                }
                tvLikeCount.setText(likesList.size() +  " likes");
                post.saveInBackground();
            }

            if (v == itemView) {
                Intent intent = new Intent(context, PostDetail.class);
                intent.putExtra("objectId", post.getObjectId());
                context.startActivity(intent);
            }
        }

        // Bind the Post data to the view elements
        @SuppressLint("SetTextI18n")
        public void bind(final Post post) {
            this.post = post;
            ParseUser user = post.getUser();

            tvHeaderUsername.setText(post.getUser().getUsername());
            tvDescriptionUsername.setText(post.getUser().getUsername());
            tvDescription.setText(post.getDescription());
            tvLikeCount.setText(post.getLikesList().size() +  " likes");

            long createdAt = post.getCreatedAt().getTime();
            String relativeTime = DateUtils.getRelativeTimeSpanString(createdAt,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            tvTimestamp.setText(relativeTime);

            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .placeholder(R.drawable.ufi_heart_active)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.e(TAG, "Glide failed to load image");
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                // Make all images square
                                ivImage.getLayoutParams().height = ((View) ivImage.getParent()).getWidth();
                                return false;
                            }
                        })
                        .into(ivImage);
            }

            if (!forProfileView) {
                ParseFile profileImage = user.getParseFile("profileImage");
                if (profileImage != null) {
                    Glide.with(context)
                            .load(profileImage.getUrl())
                            .placeholder(R.drawable.ufi_heart_active)
                            .into(ivProfileImage);
                }
            }

            if (post.getLikesList().contains(ParseUser.getCurrentUser().getUsername())) {
                Glide.with(context)
                        .load(R.drawable.ufi_heart_active)
                        .placeholder(R.drawable.ufi_heart_active)
                        .into(ivLike);
            }

        }

    }
}

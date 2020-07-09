package com.codepath.parsetagram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class PostDetail extends AppCompatActivity {

    private ImageView ivProfileImage;
    private TextView tvHeaderUsername;
    private ImageView ivImage;
    private ImageView ivLike;
    private TextView tvLikeCount;
    private TextView tvDescriptionUsername;
    private TextView tvDescription;
    private TextView tvTimestamp;
    private Context context;

    private Post post;
    private ParseUser user;
    private ParseUser currentUser;

    public static final String TAG = AppCompatActivity.class.getSimpleName();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);


        tvHeaderUsername = findViewById(R.id.tvHeaderUsername);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivImage = findViewById(R.id.ivImage);
        ivLike = findViewById(R.id.ivLike);
        tvLikeCount = findViewById(R.id.tvLikeCount);
        tvDescriptionUsername = findViewById(R.id.tvDescriptionUsername);
        tvDescription = findViewById(R.id.tvDescription);
        tvTimestamp = findViewById(R.id.tvTimestamp);

        context = this;
        currentUser = ParseUser.getCurrentUser();

        Intent intent = getIntent();
        String objectId = intent.getStringExtra("objectId");
        queryPost(objectId);

        ivLike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                List<String> likesList = post.getLikesList();
                boolean alreadyLiked = false;
                for (String s : likesList) {
                    if (s.equals(user.getUsername())) {
                        alreadyLiked = true;
                        break;
                    }
                }
                if (alreadyLiked) {
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
                tvLikeCount.setText(post.getLikesList().size() +  " likes");
                post.saveInBackground();
            }
        });
    }

    private void queryPost(final String objectId) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find posts matching objectId
        query.whereEqualTo(Post.KEY_OBJECT_ID, objectId);
        query.findInBackground(new FindCallback<Post>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting post: " + objectId , e);
                    return;
                }

                post = posts.get(0);
                user = post.getUser();

                // Set ImageViews
                ParseFile image = post.getImage();
                ParseFile profileImage = null;
                try {
                    profileImage = user.fetchIfNeeded().getParseFile("profileImage");
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
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
                if (profileImage != null) {
                    Glide.with(context)
                            .load(profileImage.getUrl())
                            .placeholder(R.drawable.ufi_heart_active)
                            .into(ivProfileImage);
                }

                tvHeaderUsername.setText(user.getUsername());
                tvLikeCount.setText(post.getLikesList().size() +  " likes");

                if (post.getLikesList().contains(ParseUser.getCurrentUser().getUsername())) {
                    Glide.with(context)
                            .load(R.drawable.ufi_heart_active)
                            .placeholder(R.drawable.ufi_heart_active)
                            .into(ivLike);
                }

                tvDescriptionUsername.setText(user.getUsername());
                tvDescription.setText(post.getDescription());

                long createdAt = post.getCreatedAt().getTime();
                String relativeTime = DateUtils.getRelativeTimeSpanString(createdAt,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
                tvTimestamp.setText(relativeTime);
            }
        });
    }
}
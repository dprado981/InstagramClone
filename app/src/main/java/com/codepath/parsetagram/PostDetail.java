package com.codepath.parsetagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

public class PostDetail extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvUsername;
    private TextView tvCaption;
    private TextView tvTimestamp;
    private Context context;

    private Post post;

    public static final String TAG = AppCompatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ivImage = findViewById(R.id.ivImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvCaption = findViewById(R.id.tvCaption);
        tvTimestamp = findViewById(R.id.tvTimestamp);

        context = this;

        Intent intent = getIntent();
        String objectId = intent.getStringExtra("objectId");
        queryPost(objectId);
    }

    private void queryPost(final String objectId) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find posts matching objectId
        query.whereEqualTo(Post.KEY_OBJECT_ID, objectId);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting post: " + objectId , e);
                    return;
                }
                post = posts.get(0);
                try {
                   tvUsername.setText(post.getUser().fetchIfNeeded().getUsername());
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                tvCaption.setText(post.getDescription());
                ParseFile image = post.getImage();
                if (image != null) {
                    Glide.with(context)
                            .load(image.getUrl())
                            .placeholder(R.drawable.ufi_heart_active)
                            .into(ivImage);
                }
                long createdAt = post.getCreatedAt().getTime();
                String relativeTime = DateUtils.getRelativeTimeSpanString(createdAt,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
                tvTimestamp.setText(relativeTime);
            }
        });
    }
}
package com.codepath.parsetagram.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.parsetagram.CommentsAdapter;
import com.codepath.parsetagram.EndlessRecyclerViewScrollListener;
import com.codepath.parsetagram.R;
import com.codepath.parsetagram.data.models.Comment;
import com.codepath.parsetagram.data.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements View.OnClickListener {

    private Context context;

    private ImageView ivLike;
    private TextView tvLikeCount;
    private TextView tvDescriptionUsername;
    private TextView tvDescription;
    private TextView tvTimestamp;

    private RecyclerView rvComments;
    private CommentsAdapter adapter;
    protected SwipeRefreshLayout swipeContainer;
    protected EndlessRecyclerViewScrollListener scrollListener;
    private List<Comment> allComments;

    private EditText etComment;
    private Button btnPost;

    private Post post;
    private ParseUser user;
    private ParseUser currentUser;

    public static final String TAG = DetailFragment.class.getSimpleName();

    public DetailFragment() {
        // Required empty public constructor
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        View postNoImage = view.findViewById(R.id.postNoImage);

        ivLike = postNoImage.findViewById(R.id.ivLike);
        tvLikeCount = postNoImage.findViewById(R.id.tvLikeCount);
        tvDescriptionUsername = postNoImage.findViewById(R.id.tvDescriptionUsername);
        tvDescription = postNoImage.findViewById(R.id.tvDescription);
        tvTimestamp = postNoImage.findViewById(R.id.tvTimestamp);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        rvComments = view.findViewById(R.id.rvComments);
        etComment = view.findViewById(R.id.etComment);
        btnPost = view.findViewById(R.id.btnPost);

        currentUser = ParseUser.getCurrentUser();

        Bundle bundle = getArguments();
        String objectId = null;
        boolean autoComment = false;
        if (bundle != null) {
            objectId = bundle.getString("objectId");
            autoComment = bundle.getBoolean("autoComment");
        }

        if (autoComment) {
            etComment.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        queryPost(objectId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        allComments = new ArrayList<>();
        adapter = new CommentsAdapter(context, allComments);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(layoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                queryComments(true);
            }
        };

        // Adds the scroll listener to RecyclerView
        rvComments.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                queryComments(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        tvDescriptionUsername.setOnClickListener(this);
        ivLike.setOnClickListener(this);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = etComment.getText().toString();
                if (text.isEmpty()) {
                    return;
                }
                etComment.setText("");
                Comment comment = new Comment();
                comment.setText(text);
                comment.setUser(ParseUser.getCurrentUser());
                comment.setPost(post);
                comment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error while saving comment", e);
                            Toast.makeText(context, "Error while saving comment", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(TAG, "Comment was saved successfully");
                        Toast.makeText(context, "Posted!", Toast.LENGTH_SHORT).show();
                    }
                });
                addCommentToScreen(comment);
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { checkButton(); }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { checkButton(); }

            @Override
            public void afterTextChanged(Editable editable) { checkButton(); }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        if (v == tvDescriptionUsername) {
            Fragment fragment = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username", post.getUser().getUsername());
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
        }

        if (v == ivLike) {
            ParseUser user = ParseUser.getCurrentUser();
            List<String> likesList = post.getLikesList();
            if (likesList.contains(user.getUsername())) {
                post.deleteLike(user);
                Glide.with(context)
                        .load(R.drawable.ic_heart)
                        .placeholder(R.drawable.ic_heart)
                        .into(ivLike);
            } else {
                post.addLike(user);
                Glide.with(context)
                        .load(R.drawable.ic_heart_active)
                        .placeholder(R.drawable.ic_heart_active)
                        .into(ivLike);
            }
            tvLikeCount.setText(post.getLikesList().size() +  getString(R.string.space_likes));
            post.saveInBackground();
        }
    }

    private void addCommentToScreen(Comment comment) {
        // add to list, update recyclerview
        allComments.add(0, comment);
        adapter.notifyItemInserted(0);
        rvComments.smoothScrollToPosition(0);
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

                tvLikeCount.setText(post.getLikesList().size() +  " likes");

                if (post.getLikesList().contains(currentUser.getUsername())) {
                    Glide.with(context)
                            .load(R.drawable.ic_heart_active)
                            .placeholder(R.drawable.ic_heart_active)
                            .into(ivLike);
                }

                try {
                    tvDescriptionUsername.setText(user.fetchIfNeeded().getUsername());
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                tvDescription.setText(post.getDescription());

                long createdAt = post.getCreatedAt().getTime();
                String relativeTime = DateUtils.getRelativeTimeSpanString(createdAt,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
                tvTimestamp.setText(relativeTime);

                queryComments(false);
            }
        });
    }

    protected void queryComments(final boolean loadNext) {
        // Specify which class to query
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        // Find all posts
        Log.d(TAG, "comments from: " + post.getObjectId());
        query.whereEqualTo(Comment.KEY_POST, post.getObjectId());
        query.include(Comment.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder(Comment.KEY_CREATED_AT);
        if (loadNext) {
            Date olderThanDate = allComments.get(allComments.size()-1).getCreatedAt();
            Log.i(TAG, "Loading comments older than " + olderThanDate);
            query.whereLessThan(Comment.KEY_CREATED_AT, olderThanDate);
        }
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> comments, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments", e);
                    return;
                }
                if (!loadNext) {
                    adapter.clear();
                    scrollListener.resetState();
                    swipeContainer.setRefreshing(false);
                }
                adapter.addAll(comments);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void checkButton() {
        if (!etComment.getText().toString().isEmpty()) {
            btnPost.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondary));
        } else {
            btnPost.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondaryMuted));
        }
    }
}
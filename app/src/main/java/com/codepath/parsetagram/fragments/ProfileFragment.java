package com.codepath.parsetagram.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.parsetagram.EndlessRecyclerViewScrollListener;
import com.codepath.parsetagram.LoginActivity;
import com.codepath.parsetagram.Post;
import com.codepath.parsetagram.PostsAdapter;
import com.codepath.parsetagram.R;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    // TODO: make different fragment that extends this one for other profile view
    // TODO: make this fragment be like oh: ur logged in show personal pic

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private Context context;
    private LinearLayout llHeader;
    private ImageView ivProfile;
    private ImageView ivUpdateProfilePicture;
    private TextView tvLogOut;
    private RecyclerView rvPosts;
    private SwipeRefreshLayout swipeContainer;

    private ParseUser user;
    private ParseUser currentUser;
    private List<Post> allPosts;
    private PostsAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private File photoFile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        llHeader = view.findViewById(R.id.llHeader);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivUpdateProfilePicture = view.findViewById(R.id.ivUpdateProfilePicture);
        tvLogOut = view.findViewById(R.id.tvLogOut);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        rvPosts = view.findViewById(R.id.rvPosts);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(context, allPosts, true);

        rvPosts.setAdapter(adapter);

        currentUser = ParseUser.getCurrentUser();

        Bundle bundle = getArguments();
        String userId = null;
        if (bundle != null) {
            userId = getArguments().getString("userId");
        }

        setProfile(userId);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
        rvPosts.setLayoutManager(gridLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi();
            }
        };

        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                queryPosts();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        tvLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOutUser();
            }
        });

        ivUpdateProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });
    }

    private void loadNextDataFromApi() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.whereEqualTo(Post.KEY_USER, user);
        Date olderThanDate = allPosts.get(allPosts.size()-1).getCreatedAt();
        Log.d(TAG, "Getting posts older than: " + olderThanDate);
        query.whereLessThan(Post.KEY_CREATED_AT,
                allPosts.get(allPosts.size()-1).getCreatedAt());
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for(Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", Username: " + post.getUser().getUsername());
                }
                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void queryPosts() {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts from user
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, user);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for(Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", Username: " + post.getUser().getUsername());
                }
                adapter.clear();
                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
                scrollListener.resetState();
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void setProfile(final String userId) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("objectId", userId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting user", e);
                    return;
                }
                user = users.get(0);
                ParseFile image = user.getParseFile("profileImage");
                Log.i(TAG, "Showing profile of: " + user.getUsername());
                if (image != null) {
                    Glide.with(context)
                            .load(image.getUrl())
                            .placeholder(R.drawable.instagram_user_outline_24)
                            .into(ivProfile);
                }

                if (!currentUser.getUsername().equals(user.getUsername())) {
                    ivUpdateProfilePicture.setVisibility(View.GONE);
                    tvLogOut.setVisibility(View.GONE);
                }

                queryPosts();
            }
        });
    }

    private void logOutUser() {
        Log.i(TAG, "Attempting to log out user");
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with logout:", e);
                    return;
                }
                goToLogin();
                Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Returns to Login page and removes activity from backstack */
    private void goToLogin() {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
        Activity activity = getActivity();
        if (activity != null ){
            activity.finishAffinity();
        }
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = ComposeFragment.getPhotoFileUri(ComposeFragment.PHOTO_FILE_NAME, context);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(context, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, ComposeFragment.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ComposeFragment.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                takenImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapBytes = stream.toByteArray();
                ivProfile.setImageBitmap(takenImage);
                user.put("profileImage", new ParseFile(ComposeFragment.PHOTO_FILE_NAME, bitmapBytes));
                Log.d(TAG, "saving....");
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error while saving", e);
                            Toast.makeText(context, "Error while saving", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(TAG, "Profile Image was saved successfully");
                    }
                });
            } else { // Result was a failure
                Toast.makeText(context, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
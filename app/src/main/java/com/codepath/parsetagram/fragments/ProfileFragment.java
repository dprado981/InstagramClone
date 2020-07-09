package com.codepath.parsetagram.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.parsetagram.Post;
import com.codepath.parsetagram.PostsAdapter;
import com.codepath.parsetagram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends PostsFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    protected ImageView ivProfile;

    protected ParseUser user;

    protected int getLayout() {
        return R.layout.fragment_profile;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfile = view.findViewById(R.id.ivProfile);

        Bundle bundle = getArguments();
        String userId = null;
        if (bundle != null) {
            userId = getArguments().getString("userId");
        }

        setProfile(userId);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager(Context context) {
        return new GridLayoutManager(context, 3);
    }

    @Override
    protected void setupRecyclerView(RecyclerView.LayoutManager layoutManager) {
        adapter = new PostsAdapter(context, allPosts, true);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);
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

                queryPosts(false);
            }
        });
    }


    @Override
    protected void addExtraPostQueryOptions(ParseQuery<Post> query) {
        query.whereEqualTo(Post.KEY_USER, user);
    }
}
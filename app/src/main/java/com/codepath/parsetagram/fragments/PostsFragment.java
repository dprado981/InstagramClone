package com.codepath.parsetagram.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.parsetagram.EndlessRecyclerViewScrollListener;
import com.codepath.parsetagram.Post;
import com.codepath.parsetagram.PostsAdapter;
import com.codepath.parsetagram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {

    public static final String TAG = PostsFragment.class.getSimpleName();

    protected Context context;
    protected PostsAdapter adapter;
    protected SwipeRefreshLayout swipeContainer;
    protected EndlessRecyclerViewScrollListener scrollListener;

    protected List<Post> allPosts;

    protected RecyclerView rvPosts;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(getLayout(), container, false);
    }

    protected int getLayout() {
        return R.layout.fragment_posts;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        swipeContainer = view.findViewById(R.id.swipeContainer);
        rvPosts = view.findViewById(R.id.rvPosts);

        RecyclerView.LayoutManager layoutManager = getLayoutManager(context);
        allPosts = new ArrayList<>();
        setupRecyclerView(layoutManager);

        queryPosts(false);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                queryPosts(true);
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
                queryPosts(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    protected RecyclerView.LayoutManager getLayoutManager(Context context) {
        return new LinearLayoutManager(context);
    }

    protected void setupRecyclerView(RecyclerView.LayoutManager layoutManager) {
        adapter = new PostsAdapter(context, allPosts, false);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(layoutManager);
    }


    protected void queryPosts(final boolean loadNext) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        if (loadNext) {
            Date olderThanDate = allPosts.get(allPosts.size()-1).getCreatedAt();
            Log.i(TAG, "Loading posts older than " + olderThanDate);
            query.whereLessThan(Post.KEY_CREATED_AT, olderThanDate);
        }
        addExtraPostQueryOptions(query);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (!loadNext) {
                    adapter.clear();
                    scrollListener.resetState();
                    swipeContainer.setRefreshing(false);
                }
                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    protected void addExtraPostQueryOptions(ParseQuery<Post> query) { query.setLimit(20); }

}
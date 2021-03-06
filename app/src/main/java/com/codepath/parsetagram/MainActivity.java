package com.codepath.parsetagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.codepath.parsetagram.fragments.ComposeFragment;
import com.codepath.parsetagram.fragments.CurrentProfileFragment;
import com.codepath.parsetagram.fragments.PostsFragment;
import com.codepath.parsetagram.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private BottomNavigationView bottomNavigation;

    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_logo);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }


        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            String username = intent.getStringExtra("username");
            Fragment fragment = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();

        }

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.actionHome:
                        fragment = new PostsFragment();
                        break;
                    case R.id.actionCompose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.actionProfile:
                    default:
                        fragment = new CurrentProfileFragment();
                        break;
                }
                Bundle bundle = new Bundle();
                bundle.putString("username", ParseUser.getCurrentUser().getUsername());
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                return true;
            }
        });

        bottomNavigation.setSelectedItemId(R.id.actionHome);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "back pressed");
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            super.onBackPressed();
            Fragment currentFragment = manager.findFragmentById(R.id.flContainer);
            if (currentFragment instanceof CurrentProfileFragment) {
                bottomNavigation.setSelectedItemId(R.id.actionProfile);
            } else if (currentFragment instanceof ComposeFragment) {
                bottomNavigation.setSelectedItemId(R.id.actionCompose);
            } else {
                bottomNavigation.setSelectedItemId(R.id.actionHome);
            }
        }

    }
}
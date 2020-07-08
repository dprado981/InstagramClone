package com.codepath.parsetagram.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.codepath.parsetagram.LoginActivity;
import com.codepath.parsetagram.R;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static android.app.Activity.RESULT_OK;

public class CurrentProfileFragment extends ProfileFragment {

    private CardView cvUpdateProfilePicture;
    private ImageView ivUpdateProfilePicture;
    private TextView tvLogOut;

    private File photoFile;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cvUpdateProfilePicture = view.findViewById(R.id.cvUpdateProfilePicture);
        ivUpdateProfilePicture = view.findViewById(R.id.ivUpdateProfilePicture);
        tvLogOut = view.findViewById(R.id.tvLogOut);

        cvUpdateProfilePicture.setVisibility(View.VISIBLE);
        tvLogOut.setVisibility(View.VISIBLE);

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

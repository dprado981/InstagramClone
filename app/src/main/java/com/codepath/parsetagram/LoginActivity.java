package com.codepath.parsetagram;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/** A login screen that offers login via username/password */
public class LoginActivity extends AppCompatActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvConfirmation;
    private Button btnLogin;
    private Button btnRegister;
    private ProgressBar pbLogin;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        if (ParseUser.getCurrentUser() != null) {
            goToMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvConfirmation = findViewById(R.id.tvConfirmation);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        pbLogin = findViewById(R.id.pbLogin);

        etUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    btnLogin.performClick();
                }
                return false;
            }
        });

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    btnLogin.performClick();
                }
                return false;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick login button");
                pbLogin.setVisibility(View.VISIBLE);
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick register button");
                pbLogin.setVisibility(View.VISIBLE);
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                registerUser(username, password);
            }
        });

        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { checkButton(); }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { checkButton(); }

            @Override
            public void afterTextChanged(Editable editable) { checkButton(); }

        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { checkButton(); }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { checkButton(); }

            @Override
            public void afterTextChanged(Editable editable) { checkButton(); }

        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to log in user " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login:", e);
                    Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show();
                    pbLogin.setVisibility(View.INVISIBLE);
                    return;
                }
                goToMainActivity();
                Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String username, String password) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                pbLogin.setVisibility(View.INVISIBLE);
                if (e != null) {
                    Log.e(TAG, "Issue with registration:", e);
                    tvConfirmation.setText(R.string.fail_registration);
                    fadeIn(tvConfirmation, 1000);
                    fadeOut(tvConfirmation, 1000);
                    return;
                }
                Log.i(TAG, "Registration successful!");
                etUsername.setText("");
                etPassword.setText("");
                tvConfirmation.setText(R.string.success_registration);
                fadeIn(tvConfirmation, 1000);
                fadeOut(tvConfirmation, 1000);
                ParseUser.logOutInBackground();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public static void fadeIn(final View view, long animationDuration) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(animationDuration);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeIn);
    }

    public static void fadeOut(final View view, long animationDuration) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(animationDuration);
        fadeOut.setDuration(animationDuration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }

    private void checkButton() {
        if (!etUsername.getText().toString().isEmpty() && !etUsername.getText().toString().isEmpty()) {
            btnLogin.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondary));
            btnRegister.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondary));
        } else {
            btnLogin.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondaryMuted));
            btnRegister.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondaryMuted));
        }
    }

}
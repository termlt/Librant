package com.librant.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.librant.R;
import com.librant.activities.HomeActivity;

public class MainActivity extends AppCompatActivity {
    private Button loginButton, signUpButton, guestButton;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.signup_button);
        guestButton = findViewById(R.id.guest_button);

        loginButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        signUpButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        });

        guestButton.setOnClickListener(view -> {
            String guestEmail = "kikejo8607@lucvu.com";
            String guestPassword = "12345678$";

            mAuth.signInWithEmailAndPassword(guestEmail, guestPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                finish();
                            }
                        } else {
                            String errorMessage = "Authentication failed.";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Invalid password.";
                            } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = "No account found with this email.";
                            } else if (task.getException() instanceof FirebaseAuthException) {
                                errorMessage = "Authentication error: " + task.getException().getMessage();
                            }
                            Snackbar.make(view, errorMessage, Snackbar.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

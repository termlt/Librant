package com.librant.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.librant.R;
import com.librant.activities.HomeActivity;

public class MainActivity extends AppCompatActivity {
    Button loginButton, signUpButton;
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

        loginButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        signUpButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        });
    }
}
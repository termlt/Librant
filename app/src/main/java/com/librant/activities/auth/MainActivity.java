package com.librant.activities.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.librant.activities.HomeActivity;
import com.librant.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        binding.loginButton.setOnClickListener(view ->{
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                intent.putExtra("isSignup", false);
                startActivity(intent);
                new Intent(MainActivity.this, AuthActivity.class);
            });


        binding.signupButton.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                intent.putExtra("isSignup", true);
                startActivity(intent);
                new Intent(MainActivity.this, AuthActivity.class);
        });
    }
}

package com.librant.activities.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.librant.R;
import com.librant.activities.HomeActivity;
import com.librant.fragments.auth.LoginFragment;
import com.librant.fragments.auth.SignupFragment;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(AuthActivity.this, HomeActivity.class));
            finish();
        } else {
            Intent intent = getIntent();
            Boolean isSignup = intent.getBooleanExtra("isSignup", false);

            if (isSignup) {
                startFragment(new SignupFragment());
            } else {
                startFragment(new LoginFragment());
            }
        }
    }

    private void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}

package com.librant.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.librant.R;
import com.librant.activities.auth.LoginActivity;
import com.librant.databinding.ActivityHomeBinding;
import com.librant.fragments.HomeFragment;
import com.librant.fragments.SavedFragment;
import com.librant.fragments.SearchFragment;
import com.librant.fragments.profile.ProfileFragment;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class HomeActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private SavedFragment savedFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;  // Track the currently active fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        savedFragment = new SavedFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "HOME")
                .commit();
        activeFragment = homeFragment;

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, searchFragment, "SEARCH").hide(searchFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, savedFragment, "SAVED").hide(savedFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment).commit();

        binding.bottomBar.setOnItemSelectedListener((OnItemSelectedListener) i -> {
            Fragment selectedFragment = null;
            switch (i) {
                case 0:
                    selectedFragment = homeFragment;
                    break;
                case 1:
                    selectedFragment = searchFragment;
                    break;
                case 2:
                    selectedFragment = savedFragment;
                    break;
                case 3:
                    selectedFragment = profileFragment;
                    break;
            }
            switchFragment(selectedFragment);
            return true;
        });
    }

    private void switchFragment(Fragment fragment) {
        if (fragment != activeFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(fragment)
                    .commit();
            activeFragment = fragment;
        }
    }
}

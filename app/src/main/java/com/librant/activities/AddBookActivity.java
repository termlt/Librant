package com.librant.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.librant.R;
import com.librant.fragments.book.AddBookTitleFragment;

public class AddBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        if (savedInstanceState == null) {
            Fragment mainContainer = getSupportFragmentManager().findFragmentById(R.id.fragment_container_add_book);
            if (mainContainer == null) {
                AddBookTitleFragment addBookTitleFragment = new AddBookTitleFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container_add_book, addBookTitleFragment)
                        .commit();
            }
        }
    }
}

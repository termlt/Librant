package com.librant.fragments.book;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.librant.R;
import com.librant.activities.HomeActivity;
import com.librant.models.Book;

public class AddBookTitleFragment extends Fragment {
    private TextInputEditText editTextTitle;
    private AppCompatButton backButton;
    private MaterialButton continueButton;
    private Book book;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_book_title, container, false);

        backButton = root.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        LinearProgressIndicator progressIndicator = root.findViewById(R.id.toolbarProgress);
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressIndicator, "progress", 0, 15);
        progressAnimator.setDuration(400);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();

        continueButton = root.findViewById(R.id.continueButton);
        continueButton.setEnabled(false);

        editTextTitle = root.findViewById(R.id.editTextTitle);
        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    continueButton.setBackgroundColor(getResources().getColor(R.color.light_green));
                    continueButton.setEnabled(true);
                } else {
                    continueButton.setBackgroundColor(getResources().getColor(R.color.button_gray));
                    continueButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s){}
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextTitle.getText() != null) {
                    if (!editTextTitle.getText().toString().isEmpty()) {
                        book = new Book();
                        book.setTitle(editTextTitle.getText().toString());

                        AddBookAuthorFragment addBookAuthorFragment = new AddBookAuthorFragment();
                        Bundle args = new Bundle();
                        args.putSerializable("book", book);
                        addBookAuthorFragment.setArguments(args);

                        FragmentManager fragmentManager = getParentFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment_container_add_book, addBookAuthorFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
        });

        return root;
    }
}

package com.librant.fragments.book;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.librant.R;
import com.librant.activities.HomeActivity;
import com.librant.models.Book;

public class AddBookDescriptionFragment extends Fragment {
    private TextInputEditText editTextDescription;
    private MaterialButton continueButton;
    private Book book;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_book_description, container, false);

        Bundle args = getArguments();
        if (args != null) {
            book = (Book) args.getSerializable("book");
        }

        root.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
        });

        continueButton = root.findViewById(R.id.continueButton);
        continueButton.setEnabled(false);

        editTextDescription = root.findViewById(R.id.editTextDescription);
        editTextDescription.addTextChangedListener(new TextWatcher() {
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
                if (editTextDescription.getText() != null) {
                    if (!editTextDescription.getText().toString().isEmpty()) {
                        book.setDescription(editTextDescription.getText().toString());

                        AddBookGenresFragment addBookGenresFragment = new AddBookGenresFragment();
                        args.putSerializable("book", book);
                        addBookGenresFragment.setArguments(args);

                        FragmentManager fragmentManager = getParentFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment_container_add_book, addBookGenresFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
        });

        return root;
    }
}

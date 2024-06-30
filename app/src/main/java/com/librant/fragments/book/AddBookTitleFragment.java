package com.librant.fragments.book;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.librant.R;
import com.librant.models.Book;

public class AddBookTitleFragment extends Fragment {
    private TextInputEditText editTextTitle;
    private ImageButton backButton;
    private MaterialButton continueButton;
    private Book book;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_book_title, container, false);

        backButton = root.findViewById(R.id.btn_back);
        backButton.setOnClickListener(view -> getActivity().finish());

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

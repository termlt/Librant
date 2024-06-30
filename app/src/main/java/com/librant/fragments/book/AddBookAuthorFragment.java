package com.librant.fragments.book;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.librant.R;
import com.librant.activities.HomeActivity;
import com.librant.models.Book;

public class AddBookAuthorFragment extends Fragment {

    private TextInputEditText editTextAuthorName;
    private TextInputEditText editTextAuthorSurname;
    private AppCompatButton continueButton;
    private Book book;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_book_author, container, false);

        Bundle args = getArguments();
        if (args != null) {
            book = (Book) args.getSerializable("book");
        }

        ImageButton backButton = root.findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
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

        editTextAuthorName = root.findViewById(R.id.editTextAuthorName);
        editTextAuthorSurname = root.findViewById(R.id.editTextAuthorSurname);

        continueButton = root.findViewById(R.id.continueButton);
        continueButton.setEnabled(false);

        editTextAuthorName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        editTextAuthorSurname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String authorName = editTextAuthorName.getText().toString().trim();
                String authorSurname = editTextAuthorSurname.getText().toString().trim();

                if (!TextUtils.isEmpty(authorName) && !TextUtils.isEmpty(authorSurname)) {
                    book.setAuthorName(authorName);
                    book.setAuthorSurname(authorSurname);

                    AddBookDescriptionFragment addBookDescriptionFragment = new AddBookDescriptionFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("book", book);
                    addBookDescriptionFragment.setArguments(args);

                    FragmentManager fragmentManager = getParentFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container_add_book, addBookDescriptionFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        return root;
    }

    private void validateFields() {
        String authorName = editTextAuthorName.getText().toString().trim();
        String authorSurname = editTextAuthorSurname.getText().toString().trim();

        if (!TextUtils.isEmpty(authorName) && !TextUtils.isEmpty(authorSurname)) {
            continueButton.setBackgroundColor(getResources().getColor(R.color.light_green));
            continueButton.setEnabled(true);
        } else {
            continueButton.setBackgroundColor(getResources().getColor(R.color.button_gray));
            continueButton.setEnabled(false);
        }
    }
}

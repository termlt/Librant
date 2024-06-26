package com.librant.fragments.book;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.librant.R;
import com.librant.activities.HomeActivity;
import com.librant.models.Book;

public class AddBookInfoFragment extends Fragment {
    private TextInputEditText editTextLanguage;
    private TextInputEditText editTextPageCount;
    private Spinner spinnerAgeLimit;
    private MaterialButton continueButton;
    private Book book;

    private boolean isLanguageFilled = false;
    private boolean isPageCountFilled = false;
    private boolean isAgeLimitSelected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_book_info, container, false);

        if (getArguments() != null) {
            book = (Book) getArguments().getSerializable("book");
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

        editTextLanguage = root.findViewById(R.id.editTextLanguage);
        editTextPageCount = root.findViewById(R.id.editTextPageCount);
        spinnerAgeLimit = root.findViewById(R.id.spinnerAgeLimit);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.age_limits, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeLimit.setAdapter(adapter);
        spinnerAgeLimit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isAgeLimitSelected = true;
                checkAllFields();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                isAgeLimitSelected = false;
                checkAllFields();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isLanguageFilled = !editTextLanguage.getText().toString().isEmpty();
                isPageCountFilled = !editTextPageCount.getText().toString().isEmpty();
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextLanguage.addTextChangedListener(textWatcher);
        editTextPageCount.addTextChangedListener(textWatcher);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLanguageFilled && isPageCountFilled && isAgeLimitSelected) {
                    book.setLanguage(editTextLanguage.getText().toString());
                    book.setPageCount(Integer.parseInt(editTextPageCount.getText().toString()));
                    book.setAgeLimit(Integer.parseInt(spinnerAgeLimit.getSelectedItem().toString().replace("+", "")));

                    UploadImageFragment nextFragment = new UploadImageFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("book", book);
                    nextFragment.setArguments(args);

                    FragmentManager fragmentManager = getParentFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container_add_book, nextFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        return root;
    }

    private void checkAllFields() {
        boolean allFieldsFilled = isLanguageFilled && isPageCountFilled && isAgeLimitSelected;
        continueButton.setEnabled(allFieldsFilled);
        continueButton.setBackgroundColor(getResources().getColor(allFieldsFilled ? R.color.light_green : R.color.button_gray));
    }
}
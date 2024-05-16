package com.librant.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.librant.R;
import com.librant.models.Book;

import java.util.ArrayList;
import java.util.Date;

public class EditBookActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextAuthorName;
    private TextInputEditText editTextAuthorSurname;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextLanguage;
    private TextInputEditText editTextPageCount;
    private Spinner spinnerAgeLimit;
    private Spinner spinnerAvailability;
    private MaterialButton saveButton;
    private Book book;
    private Date availabilityDate;

    private boolean isTitleFilled = false;
    private boolean isAuthorNameFilled = false;
    private boolean isAuthorSurnameFilled = false;
    private boolean isDescriptionFilled = false;
    private boolean isLanguageFilled = false;
    private boolean isPageCountFilled = false;
    private boolean isAgeLimitSelected = false;
    private boolean isAvailabilitySelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("book")) {
            book = (Book) getIntent().getSerializableExtra("book");
        }

        LinearProgressIndicator progressIndicator = findViewById(R.id.toolbarProgress);
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressIndicator, "progress", 15, 30);
        progressAnimator.setDuration(400);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();

        saveButton = findViewById(R.id.saveButton);
        saveButton.setEnabled(false);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextAuthorName = findViewById(R.id.editTextAuthorName);
        editTextAuthorSurname = findViewById(R.id.editTextAuthorSurname);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextLanguage = findViewById(R.id.editTextLanguage);
        editTextPageCount = findViewById(R.id.editTextPageCount);
        spinnerAgeLimit = findViewById(R.id.spinnerAgeLimit);
        spinnerAvailability = findViewById(R.id.spinnerAvailability);

        ArrayAdapter<CharSequence> ageLimitAdapter = ArrayAdapter.createFromResource(this,
                R.array.age_limits, android.R.layout.simple_spinner_item);
        ageLimitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeLimit.setAdapter(ageLimitAdapter);
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

        ArrayAdapter<CharSequence> availabilityAdapter = ArrayAdapter.createFromResource(this,
                R.array.availability_options, android.R.layout.simple_spinner_item);
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(availabilityAdapter);
        spinnerAvailability.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    showUnavailableDialog();
                } else {
                    isAvailabilitySelected = true;
                    checkAllFields();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                isAvailabilitySelected = false;
                checkAllFields();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isTitleFilled = !editTextTitle.getText().toString().isEmpty();
                isAuthorNameFilled = !editTextAuthorName.getText().toString().isEmpty();
                isAuthorSurnameFilled = !editTextAuthorSurname.getText().toString().isEmpty();
                isDescriptionFilled = !editTextDescription.getText().toString().isEmpty();
                isLanguageFilled = !editTextLanguage.getText().toString().isEmpty();
                isPageCountFilled = !editTextPageCount.getText().toString().isEmpty();
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextTitle.addTextChangedListener(textWatcher);
        editTextAuthorName.addTextChangedListener(textWatcher);
        editTextAuthorSurname.addTextChangedListener(textWatcher);
        editTextDescription.addTextChangedListener(textWatcher);
        editTextLanguage.addTextChangedListener(textWatcher);
        editTextPageCount.addTextChangedListener(textWatcher);

        saveButton.setOnClickListener(view -> {
            if (allFieldsFilled()) {
                book.setTitle(editTextTitle.getText().toString());
                book.setAuthorName(editTextAuthorName.getText().toString());
                book.setAuthorSurname(editTextAuthorSurname.getText().toString());
                book.setDescription(editTextDescription.getText().toString());
                book.setLanguage(editTextLanguage.getText().toString());
                book.setPageCount(Integer.parseInt(editTextPageCount.getText().toString()));
                book.setAgeLimit(Integer.parseInt(spinnerAgeLimit.getSelectedItem().toString().replace("+", "")));
                book.setAvailability(spinnerAvailability.getSelectedItem().toString());
                if (availabilityDate != null) {
                    book.setAvailabilityDate(availabilityDate);
                }

                db.collection("books").document(book.getBookId())
                        .set(book)
                        .addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(EditBookActivity.this, BookDetailsActivity.class);
                            intent.putExtra("book", (Parcelable) book);
                            startActivity(intent);
                            finish();
                        });
            }
        });

        loadBookData();
    }

    private void loadBookData() {
        if (book != null) {
            editTextTitle.setText(book.getTitle());
            editTextAuthorName.setText(book.getAuthorName());
            editTextAuthorSurname.setText(book.getAuthorSurname());
            editTextDescription.setText(book.getDescription());
            editTextLanguage.setText(book.getLanguage());
            editTextPageCount.setText(String.valueOf(book.getPageCount()));
            spinnerAgeLimit.setSelection(getSpinnerIndex(spinnerAgeLimit, book.getAgeLimit() + "+"));
            spinnerAvailability.setSelection(getSpinnerIndex(spinnerAvailability, book.getAvailability()));
        }
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void checkAllFields() {
        saveButton.setEnabled(allFieldsFilled());
        saveButton.setBackgroundColor(getResources().getColor(allFieldsFilled() ? R.color.light_green : R.color.button_gray));
    }

    private boolean allFieldsFilled() {
        return isTitleFilled && isAuthorNameFilled && isAuthorSurnameFilled && isDescriptionFilled && isLanguageFilled && isPageCountFilled && isAgeLimitSelected && isAvailabilitySelected;
    }

    private void showUnavailableDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_borrower_info, null);
        TextInputEditText borrowerName = dialogView.findViewById(R.id.borrowerName);
        TextInputEditText borrowerSurname = dialogView.findViewById(R.id.borrowerSurname);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
                .setTitle("Borrower Information")
                .setView(dialogView)
                .setPositiveButton("Choose Date", null)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    spinnerAvailability.setSelection(0);
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (borrowerName.getText().toString().isEmpty()) {
                borrowerName.setError("Name is required");
            } else if (borrowerSurname.getText().toString().isEmpty()) {
                borrowerSurname.setError("Surname is required");
            } else {
                dialog.dismiss();
                showDatePicker(borrowerName.getText().toString(), borrowerSurname.getText().toString());
            }
        });
    }

    private void showDatePicker(String borrowerName, String borrowerSurname) {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Availability Date")
                .setCalendarConstraints(constraints)
                .build();

        datePicker.show(getSupportFragmentManager(), datePicker.toString());

        datePicker.addOnPositiveButtonClickListener(selection -> {
            availabilityDate = new Date(selection);
            if (book.getBorrowers() == null) {
                book.setBorrowers(new ArrayList<>());
            }
            book.getBorrowers().add(borrowerName + " " + borrowerSurname);
            isAvailabilitySelected = true;
            checkAllFields();
        });

        datePicker.addOnNegativeButtonClickListener(dialog -> {
            spinnerAvailability.setSelection(0);
        });
    }
}

package com.librant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.librant.R;
import com.librant.models.Book;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditBookActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextAuthorName;
    private TextInputEditText editTextAuthorSurname;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextLanguage;
    private TextInputEditText editTextPageCount;
    private TextInputEditText editTextBorrowerName;
    private TextInputEditText editTextBorrowerSurname;
    private TextInputLayout outlinedTextFieldBorrowerName;
    private TextInputLayout outlinedTextFieldBorrowerSurname;

    private ImageButton backButton;

    private Spinner spinnerAgeLimit;
    private Spinner spinnerAvailability;
    private MaterialButton saveButton;
    private TextView borrowerNameText;
    private TextView borrowerSurnameText;
    private MaterialCardView availabilityDateCard;
    private TextView availabilityDateText;
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
    private boolean isBorrowerNameFilled = false;
    private boolean isBorrowerSurnameFilled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("book")) {
            book = (Book) getIntent().getSerializableExtra("book");
        }

        initViews();

        backButton.setOnClickListener(v -> finish());

        saveButton.setEnabled(false);

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
                    showBorrowerInfo();
                } else {
                    hideBorrowerInfo();
                }
                isAvailabilitySelected = true;
                checkAllFields();
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
                isBorrowerNameFilled = !editTextBorrowerName.getText().toString().isEmpty();
                isBorrowerSurnameFilled = !editTextBorrowerSurname.getText().toString().isEmpty();
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
        editTextBorrowerName.addTextChangedListener(textWatcher);
        editTextBorrowerSurname.addTextChangedListener(textWatcher);

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

                if (spinnerAvailability.getSelectedItem().toString().equals("Unavailable")) {
                    book.setBorrowerName(editTextBorrowerName.getText().toString());
                    book.setBorrowerSurname(editTextBorrowerSurname.getText().toString());

                    if (availabilityDate != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(availabilityDate);
                        String newBorrowerInfo = book.getBorrowerName() + " " + book.getBorrowerSurname() + " " + formattedDate;

                        List<String> borrowers = book.getBorrowers();
                        if (borrowers == null) {
                            borrowers = new ArrayList<>();
                            book.setBorrowers(borrowers);
                        }

                        borrowers.add(newBorrowerInfo);
                        book.setAvailabilityDate(availabilityDate);
                    }
                } else {
                    book.setBorrowerName(null);
                    book.setBorrowerSurname(null);
                    book.setAvailabilityDate(null);
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

    private void initViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextAuthorName = findViewById(R.id.editTextAuthorName);
        editTextAuthorSurname = findViewById(R.id.editTextAuthorSurname);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextLanguage = findViewById(R.id.editTextLanguage);
        editTextPageCount = findViewById(R.id.editTextPageCount);
        editTextBorrowerName = findViewById(R.id.editTextBorrowerName);
        editTextBorrowerSurname = findViewById(R.id.editTextBorrowerSurname);
        spinnerAgeLimit = findViewById(R.id.spinnerAgeLimit);
        spinnerAvailability = findViewById(R.id.spinnerAvailability);
        saveButton = findViewById(R.id.saveButton);
        borrowerNameText = findViewById(R.id.borrower_name_text);
        borrowerSurnameText = findViewById(R.id.borrower_surname_text);
        availabilityDateCard = findViewById(R.id.availabilityDateCard);
        availabilityDateText = findViewById(R.id.availabilityDateText);

        backButton = findViewById(R.id.btn_back);

        outlinedTextFieldBorrowerName = findViewById(R.id.outlinedTextFieldBorrowerName);
        outlinedTextFieldBorrowerSurname = findViewById(R.id.outlinedTextFieldBorrowerSurname);

        availabilityDateCard.setOnClickListener(v -> showDatePicker());
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

            if (book.getAvailability().equals("Unavailable")) {
                showBorrowerInfo();
                editTextBorrowerName.setText(book.getBorrowerName());
                editTextBorrowerSurname.setText(book.getBorrowerSurname());
                isBorrowerNameFilled = true;
                isBorrowerSurnameFilled = true;
                if (book.getAvailabilityDate() != null) {
                    availabilityDate = book.getAvailabilityDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    availabilityDateText.setText(sdf.format(availabilityDate));
                }
                checkAllFields();
            } else {
                hideBorrowerInfo();
            }

            if (book.getBorrowers() == null) {
                book.setBorrowers(new ArrayList<>());
            }
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
        if (spinnerAvailability.getSelectedItem().toString().equals("Unavailable")) {
            return isTitleFilled && isAuthorNameFilled && isAuthorSurnameFilled && isDescriptionFilled && isLanguageFilled && isPageCountFilled && isAgeLimitSelected && isAvailabilitySelected && isBorrowerNameFilled && isBorrowerSurnameFilled;
        } else {
            return isTitleFilled && isAuthorNameFilled && isAuthorSurnameFilled && isDescriptionFilled && isLanguageFilled && isPageCountFilled && isAgeLimitSelected && isAvailabilitySelected;
        }
    }

    private void showBorrowerInfo() {
        editTextBorrowerName.setVisibility(View.VISIBLE);
        editTextBorrowerSurname.setVisibility(View.VISIBLE);
        borrowerNameText.setVisibility(View.VISIBLE);
        borrowerSurnameText.setVisibility(View.VISIBLE);
        availabilityDateCard.setVisibility(View.VISIBLE);

        outlinedTextFieldBorrowerName.setVisibility(View.VISIBLE);
        outlinedTextFieldBorrowerSurname.setVisibility(View.VISIBLE);

        checkAllFields();
    }

    private void hideBorrowerInfo() {
        editTextBorrowerName.setVisibility(View.GONE);
        editTextBorrowerSurname.setVisibility(View.GONE);
        borrowerNameText.setVisibility(View.GONE);
        borrowerSurnameText.setVisibility(View.GONE);
        availabilityDateCard.setVisibility(View.GONE);

        outlinedTextFieldBorrowerName.setVisibility(View.GONE);
        outlinedTextFieldBorrowerSurname.setVisibility(View.GONE);

        isBorrowerNameFilled = false;
        isBorrowerSurnameFilled = false;
        checkAllFields();
    }

    private void showDatePicker() {
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
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            availabilityDateText.setText(sdf.format(availabilityDate));
            isAvailabilitySelected = true;
            checkAllFields();
        });

        datePicker.addOnNegativeButtonClickListener(dialog -> {
            if (book.getAvailability().equals("Unavailable") && availabilityDate == null) {
                spinnerAvailability.setSelection(0);
                hideBorrowerInfo();
            }
        });
    }
}

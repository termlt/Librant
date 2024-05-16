package com.librant.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.librant.R;
import com.librant.databinding.ActivityBookDetailsBinding;
import com.librant.db.UserCollection;
import com.librant.fragments.profile.BookOwnerHistoryFragment;
import com.librant.models.Book;
import com.librant.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookDetailsActivity extends AppCompatActivity {
    private UserCollection userCollection;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private List<String> savedBooks;
    private Book book;
    private User bookOwner;
    private MaterialButton buttonDirection, buttonInformation, buttonContactOwner;
    private String bookOwnerLocation;
    private ImageView saveButton, historyButton, approveButton, disproveButton, backButton;
    private ActivityBookDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userCollection = new UserCollection();

        setupUI();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("book")) {
            book = intent.getParcelableExtra("book");
            updateRecentlyViewedBooks(book.getBookId());
            loadBookData(book);

            fetchBookOwner();
        } else {
            finish();
        }

        if (book.isApproved()) {
            disableButtons();
        } else {
            enableButtons();
        }
    }

    private void setupUI() {
        approveButton = binding.approveButton;
        historyButton = binding.historyButton;
        disproveButton = binding.disproveButton;
        saveButton = binding.saveButton;
        backButton = binding.backButton;
        buttonInformation = binding.buttonInformation;
        buttonDirection = binding.buttonDirection;
        buttonContactOwner = binding.buttonContactOwner;

        savedBooks = new ArrayList<>();

        backButton.setOnClickListener(view -> finish());
        saveButton.setOnClickListener(v -> handleSaveButtonClick());
        buttonDirection.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class).putExtra("ownerLocation", bookOwnerLocation)));
        buttonInformation.setOnClickListener(v -> displayOwnerInfo());
        buttonContactOwner.setOnClickListener(v -> displayOwnerInfo());

        fetchUserSavedBooks(saveButton);
    }

    private void fetchBookOwner() {
        userCollection.getUserById(book.getOwnerId(), user -> {
            bookOwner = user;
            if (bookOwner != null) {
                bookOwnerLocation = bookOwner.getAddress();
            }

            if (mAuth.getCurrentUser().getUid().equals(bookOwner.getId())) {
                historyButton.setVisibility(View.VISIBLE);

                historyButton.setOnClickListener(v -> {
                    BookOwnerHistoryFragment fragment = new BookOwnerHistoryFragment("What is Lorem Ipsum?\n" +
                            "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
                    fragment.show(getSupportFragmentManager(), fragment.getTag());
                });
            }
        });
    }

    private void displayOwnerInfo() {
        if (book.getAvailability().equals("Unavailable")) {
            showUnavailableDialog();
        } else if (bookOwner != null && bookOwner.getName() != null) {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_user_info, (ViewGroup) getCurrentFocus(), false);
            populateDialogWithUserData(dialogView);
            showUserInfoDialog(dialogView);
        }
    }

    private void showUnavailableDialog() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String availableAfter = book.getAvailabilityDate() != null ? sdf.format(book.getAvailabilityDate()) : "Unknown";
        new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
                .setTitle(book.getTitle())
                .setMessage(Html.fromHtml("This book is currently unavailable.<br/><b>Available after:</b> <b>" + availableAfter + "</b>"))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void populateDialogWithUserData(View dialogView) {
        MaterialTextView nameView = dialogView.findViewById(R.id.nameTextView);
        MaterialTextView phoneView = dialogView.findViewById(R.id.phoneTextView);
        MaterialTextView addressView = dialogView.findViewById(R.id.addressTextView);

        String nameText = (bookOwner.getName() != null && !bookOwner.getName().isEmpty()) ? bookOwner.getName() + " " + (bookOwner.getSurname() != null ? bookOwner.getSurname() : "") : "Unavailable";
        String phoneText = (bookOwner.getPhoneNumber() != null && !bookOwner.getPhoneNumber().isEmpty()) ? bookOwner.getPhoneNumber() : "Unavailable";
        String addressText = (bookOwner.getAddress() != null && !bookOwner.getAddress().isEmpty()) ? bookOwner.getAddress() : "Unavailable";

        nameView.setText(fromHtml("<b>Name:</b> " + nameText));
        phoneView.setText(fromHtml("<b>Phone Number:</b> " + phoneText));
        addressView.setText(fromHtml("<b>Address:</b> " + addressText));
    }

    @SuppressWarnings("deprecation")
    private Spanned fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    private void showUserInfoDialog(View dialogView) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .setTitle("Owner Information");

        if (bookOwner.getPhoneNumber() != null && !bookOwner.getPhoneNumber().isEmpty()) {
            builder.setPositiveButton("Copy Phone", (dialog, which) -> copyToClipboard("Phone number", bookOwner.getPhoneNumber()));
        }

        if (bookOwner.getAddress() != null && !bookOwner.getAddress().isEmpty()) {
            builder.setNegativeButton("Copy Address", (dialog, which) -> copyToClipboard("Address", bookOwner.getAddress()));
        }

        builder.show();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    private void handleSaveButtonClick() {
        if (savedBooks.contains(book.getBookId())) {
            removeBookFromUserModel(saveButton, book.getBookId());
        } else {
            updateBookToUserModel(saveButton, book);
        }
    }

    private void enableButtons() {
        approveButton.setVisibility(View.VISIBLE);
        disproveButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);

        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("pendingBooks").document(book.getBookId())
                        .update("approved", true)
                        .addOnSuccessListener(aVoid -> moveBookToBooksCollection())
                        .addOnFailureListener(e -> Snackbar.make(saveButton, "Something went wrong. " +
                                "Try Again later", Snackbar.LENGTH_SHORT).show());
            }
        });

        disproveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("pendingBooks").document(book.getBookId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            deleteBookImageFromStorage();
                            Snackbar.make(v, "Book disapproved and removed from pending list", Snackbar.LENGTH_SHORT).show();
                            new Handler().postDelayed(BookDetailsActivity.this::finish, 2000);
                        })
                        .addOnFailureListener(e -> Snackbar.make(saveButton, "Something went wrong. " +
                                "Try Again later", Snackbar.LENGTH_SHORT).show());
            }
        });
    }

    private void disableButtons() {
        approveButton.setVisibility(View.GONE);
        disproveButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
    }

    private void moveBookToBooksCollection() {
        db.collection("pendingBooks").document(book.getBookId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Book approvedBook = documentSnapshot.toObject(Book.class);
                        if (approvedBook != null) {
                            db.collection("books").document(approvedBook.getBookId())
                                    .set(approvedBook)
                                    .addOnSuccessListener(aVoid -> deleteBookFromPendingBooksCollection())
                                    .addOnFailureListener(e -> Snackbar.make(saveButton, "Something went wrong. " +
                                            "Try Again later", Snackbar.LENGTH_SHORT).show());
                        }
                    }
                });
    }

    private void deleteBookFromPendingBooksCollection() {
        db.collection("pendingBooks").document(book.getBookId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(saveButton, "Book approved!", Snackbar.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Snackbar.make(saveButton, "Something went wrong. " +
                        "Try Again later", Snackbar.LENGTH_SHORT).show());
    }

    private void loadBookData(Book book) {
        if (book != null) {
            ImageView bookCoverImageView = findViewById(R.id.book_cover_image);
            Glide.with(this)
                    .load(book.getImageUrl())
                    .into(bookCoverImageView);

            TextView bookTitleTextView = findViewById(R.id.bookTitle);
            bookTitleTextView.setText(book.getTitle());

            TextView bookAuthorTextView = findViewById(R.id.bookAuthor);
            bookAuthorTextView.setText(book.getAuthorName() + " " + book.getAuthorSurname());

            TextView bookDescriptionTextView = findViewById(R.id.bookDescription);
            bookDescriptionTextView.setText(book.getDescription());

            TextView ageButton = findViewById(R.id.ageButton);
            ageButton.setText(book.getAgeLimit() + "+");

            TextView languageButton = findViewById(R.id.languageButton);
            languageButton.setText(book.getLanguage());

            TextView pagesButton = findViewById(R.id.pagesButton);
            pagesButton.setText(book.getPageCount() + " Pages");

            MaterialButton buttonContactOwner = findViewById(R.id.buttonContactOwner);

            switch (book.getAvailability()) {
                case "Available":
                    buttonContactOwner.setBackgroundColor(getResources().getColor(R.color.light_green));
                    break;
                case "Unavailable":
                    buttonContactOwner.setBackgroundColor(getResources().getColor(R.color.yellow));
                    break;
                case "Hidden":
                    buttonContactOwner.setBackgroundColor(getResources().getColor(R.color.button_gray));
                    break;
                default:
                    buttonContactOwner.setBackgroundColor(getResources().getColor(R.color.light_green));
                    break;
            }
        }
    }

    private void updateRecentlyViewedBooks(String bookId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.update("viewedBooks", FieldValue.arrayUnion(bookId));

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> viewedBooks = (List<String>) documentSnapshot.get("viewedBooks");
                if (viewedBooks != null && viewedBooks.size() > 3) {
                    List<String> updatedViewedBooks = viewedBooks.subList(1, 4);
                    userDocRef.update("viewedBooks", updatedViewedBooks);
                }
            }
        });
    }

    private void updateBookToUserModel(View view, Book book) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("savedBooks", FieldValue.arrayUnion(book.getBookId()))
                .addOnSuccessListener(aVoid -> {
                    savedBooks.add(book.getBookId());
                    Snackbar.make(view, "Book saved successfully",
                            Snackbar.LENGTH_SHORT).show();
                    ((ImageView) view).setImageResource(R.drawable.saved_icon_filled);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(view, "Failed to save book",
                            Snackbar.LENGTH_SHORT).show();
                });
    }

    private void removeBookFromUserModel(View view, String bookId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("savedBooks", FieldValue.arrayRemove(book.getBookId()))
                .addOnSuccessListener(aVoid -> {
                    savedBooks.remove(book.getBookId());
                    Snackbar.make(view, "Book removed successfully",
                            Snackbar.LENGTH_SHORT).show();
                    ((ImageView) view).setImageResource(R.drawable.saved_icon_outlined);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(view, "Failed to remove book",
                            Snackbar.LENGTH_SHORT).show();
                });
    }

    private void deleteBookImageFromStorage() {
        if (book != null && book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            StorageReference imageRef = storage.getReferenceFromUrl(book.getImageUrl());
            imageRef.delete();
        }
    }

    private void fetchUserSavedBooks(ImageView saveButton) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userSavedBooks = (List<String>) documentSnapshot.get("savedBooks");
                        if (userSavedBooks != null && userSavedBooks.contains(book.getBookId())) {
                            savedBooks.addAll(userSavedBooks);
                            saveButton.setImageResource(R.drawable.saved_icon_filled);
                        } else {
                            saveButton.setImageResource(R.drawable.saved_icon_outlined);
                        }
                    }
                });
    }
}

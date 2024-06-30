package com.librant.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
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
import com.librant.activities.auth.MainActivity;
import com.librant.databinding.ActivityBookDetailsBinding;
import com.librant.db.UserCollection;
import com.librant.fragments.profile.BookOwnerHistoryFragment;
import com.librant.models.Book;
import com.librant.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
    private String bookOwnerLocation;
    private ImageView saveButton, historyButton, approveButton, disproveButton, bookCoverImage;
    private ActivityBookDetailsBinding binding;
    private DocumentReference userDocRef;
    private TextView bookTitleTextView, bookAuthorTextView, bookDescriptionTextView, ageButton, languageButton, pagesButton, genresButton;
    private MaterialButton directionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userCollection = new UserCollection();
        userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());

        setupUI();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("book")) {
            book = intent.getParcelableExtra("book");
            if (book != null) {
                updateRecentlyViewedBooks(book.getBookId());
                loadBookData(book);

                if (!book.getOwnerId().equals(mAuth.getCurrentUser().getUid()) && book.getAvailability().equals("Unavailable")) {
                    directionButton.setEnabled(false);
                }

                fetchBookOwner();

                if (book.isApproved()) {
                    disableButtons();
                } else {
                    enableButtons();
                }
            }
        } else {
            finish();
        }
    }

    private void setupUI() {
        approveButton = binding.approveButton;
        historyButton = binding.historyButton;
        disproveButton = binding.disproveButton;
        saveButton = binding.saveButton;

        directionButton = binding.buttonDirection;

        bookTitleTextView = binding.bookTitle;
        bookAuthorTextView = binding.bookAuthor;
        bookDescriptionTextView = binding.bookDescription;
        ageButton = binding.ageButton;
        languageButton = binding.languageButton;
        pagesButton = binding.pagesButton;
        genresButton = binding.genreButton;

        bookCoverImage = binding.bookCoverImage;

        savedBooks = new ArrayList<>();

        binding.backButton.setOnClickListener(view -> finish());

        saveButton.setOnClickListener(v -> handleSaveButtonClick());

        binding.buttonContactOwner.setOnClickListener(v -> displayOwnerInfo());

        fetchUserSavedBooks(saveButton);
    }

    private void fetchBookOwner() {
        userCollection.getUserById(book.getOwnerId(), user -> {
            bookOwner = user;
            if (bookOwner != null) {
                directionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (book.getOwnerId().equals(mAuth.getCurrentUser().getUid())) {
                            Snackbar.make(v, "This is your address", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        if (!bookOwner.isAddressVisible() || bookOwner.getAddress().isEmpty()) {
                            Snackbar.make(v, "This book owner does not have an address", Snackbar.LENGTH_SHORT).show();
                        } else {
                            userCollection.getUserById(mAuth.getCurrentUser().getUid(), user -> {
                                if (user != null) {
                                    String currentUserAddress = user.getAddress();
                                    if (currentUserAddress == null || currentUserAddress.isEmpty()) {
                                        Snackbar.make(v, "You don't have your address specified", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        startActivity(new Intent(BookDetailsActivity.this, MapActivity.class)
                                                .putExtra("ownerLocation", bookOwnerLocation));
                                    }
                                } else {
                                    Snackbar.make(v, "You don't have your address specified", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                bookOwnerLocation = bookOwner.getAddress();
            }

            if (mAuth.getCurrentUser().getUid().equals(bookOwner.getId())) {
                historyButton.setVisibility(View.VISIBLE);

                historyButton.setOnClickListener(v -> {
                    String borrowerHistory = formatBorrowerHistory(book.getBorrowers());
                    BookOwnerHistoryFragment fragment = BookOwnerHistoryFragment.newInstance(borrowerHistory);
                    fragment.show(getSupportFragmentManager(), fragment.getTag());
                });
            }
        });
    }


    private String formatBorrowerHistory(List<String> borrowers) {
        if (borrowers == null || borrowers.isEmpty()) return "No borrower history available.";

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        List<String[]> borrowerPartsList = new ArrayList<>();

        for (String borrower : borrowers) {
            String[] parts = borrower.split(" ");
            if (parts.length >= 3) {
                String name = parts[0];
                String surname = parts[1];
                String dateString = borrower.substring(name.length() + surname.length() + 2).trim();
                try {
                    Date date = sdf.parse(dateString);
                    borrowerPartsList.add(new String[]{name, surname, dateString, String.valueOf(date.getTime())});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Collections.sort(borrowerPartsList, (b1, b2) -> Long.compare(Long.parseLong(b2[3]), Long.parseLong(b1[3])));

        StringBuilder historyBuilder = new StringBuilder();
        for (String[] parts : borrowerPartsList) {
            historyBuilder.append("<b>Name:</b> ").append(parts[0]).append(" ").append(parts[1]).append("<br/>");
            historyBuilder.append("<b>Borrowed Until:</b> ").append(parts[2]).append("<br/><br/>");
        }

        return historyBuilder.toString();
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

        if (!bookOwner.isPhoneNumberVisible()) {
            phoneText = "Unavailable";
        }

        if (!bookOwner.isAddressVisible()) {
            addressText = "Unavailable";
        }

        nameView.setText(fromHtml("<b>Name:</b> " + nameText));
        phoneView.setText(fromHtml("<b>Phone Number:</b> " + phoneText));
        addressView.setText(fromHtml("<b>Address:</b> " + addressText));
    }

    private Spanned fromHtml(String html){
        return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
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
            Glide.with(this)
                    .load(book.getImageUrl())
                    .into(bookCoverImage);

            bookTitleTextView.setText(book.getTitle());
            bookAuthorTextView.setText(book.getAuthorName() + " " + book.getAuthorSurname());
            bookDescriptionTextView.setText(book.getDescription());
            ageButton.setText(book.getAgeLimit() + "+");
            languageButton.setText(book.getLanguage());
            pagesButton.setText(Integer.toString(book.getPageCount()));

            if (book.getGenres() != null && !book.getGenres().isEmpty()) {
                genresButton.setText(TextUtils.join(", ", book.getGenres()));
            } else {
                genresButton.setText("No genres available");
            }

            MaterialButton buttonContactOwner = findViewById(R.id.buttonContactOwner);

            if (book.getAvailability() != null) {
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
    }

    private void updateRecentlyViewedBooks(String bookId) {
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

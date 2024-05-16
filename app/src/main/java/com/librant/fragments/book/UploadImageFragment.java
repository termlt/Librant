package com.librant.fragments.book;

import static android.app.Activity.RESULT_OK;
import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG;
import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF;
import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;
import com.librant.R;
import com.librant.activities.HomeActivity;
import com.librant.models.Book;

public class UploadImageFragment extends Fragment {
    private static final String BOOK_COLLECTION = "pendingBooks";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private String uploadedImageDocumentId;
    private Book book;
    private ProgressBar progressBar;
    private MaterialButton addBookButton;
    private boolean bookAddedSuccessfully = false;
    private GmsDocumentScannerOptions options;
    private GmsDocumentScanner scanner;
    private ActivityResultLauncher<IntentSenderRequest> scannerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload_image, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        progressBar = root.findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.progress_bar_color), PorterDuff.Mode.SRC_IN);

        AppCompatButton backButton = root.findViewById(R.id.back_button);
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

        LinearProgressIndicator progressIndicator = root.findViewById(R.id.toolbarProgress);
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressIndicator, "progress", 70, 90);
        progressAnimator.setDuration(400);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();

        addBookButton = root.findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookAddedSuccessfully = true;

                Fragment mainContainer = getParentFragmentManager().findFragmentById(R.id.fragment_container_add_book);
                if (mainContainer != null) {
                    BookAddedFragment bookAddedFragment = new BookAddedFragment();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container_add_book, bookAddedFragment)
                            .commit();
                }
            }
        });

        MaterialCardView uploadBookBanner = root.findViewById(R.id.uploadBookBanner);
        uploadBookBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                options = new GmsDocumentScannerOptions.Builder()
                        .setGalleryImportAllowed(false)
                        .setPageLimit(1)
                        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
                        .setScannerMode(SCANNER_MODE_FULL)
                        .build();

                scanner = GmsDocumentScanning.getClient(options);

                scanner.getStartScanIntent(getActivity())
                        .addOnSuccessListener(intentSender -> {
                            IntentSenderRequest request = new IntentSenderRequest.Builder(intentSender).build();
                            scannerLauncher.launch(request);
                        });
            }
        });

        scannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        GmsDocumentScanningResult scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.getData());
                        for (GmsDocumentScanningResult.Page page : scanningResult.getPages()) {
                            selectedImageUri = page.getImageUri();
                        }

                        Bundle args = getArguments();
                        if (args != null) {
                            book = (Book) args.getSerializable("book");
                        }
                        if (book != null) uploadImageToStorage(root, selectedImageUri, book);
                    } else {
                        Snackbar.make(root, "Failed to upload image",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });

        return root;
    }


    private void uploadImageToStorage(View view, Uri imageUri, Book book) {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference imageRef = storageRef.child("book_covers/" + mAuth.getCurrentUser().getUid() + "/" + book.getTitle() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef = imageRef;
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                book.setImageUrl(uri.toString());
                                book.setOwnerId(mAuth.getCurrentUser().getUid());
                                book.setApproved(false);

                                db.collection(BOOK_COLLECTION)
                                        .add(book)
                                        .addOnSuccessListener(documentReference -> {
                                            uploadedImageDocumentId = documentReference.getId();
                                            book.setBookId(documentReference.getId());
                                            db.collection(BOOK_COLLECTION).document(documentReference.getId())
                                                    .set(book)
                                                    .addOnSuccessListener(aVoid -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        addBookButton.setBackgroundColor(getResources().getColor(R.color.light_green));
                                                        addBookButton.setEnabled(true);
                                                        Snackbar.make(view, "Image uploaded successfully",
                                                                Snackbar.LENGTH_SHORT).show();
                                                    });
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(view, "Failed to upload image",
                                        Snackbar.LENGTH_SHORT).show();
                            });
                });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!bookAddedSuccessfully) {
            addBookButton.setBackgroundColor(getResources().getColor(R.color.button_gray));

            if (storageRef != null) {
                storageRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            if (uploadedImageDocumentId != null) {
                                db.collection(BOOK_COLLECTION).document(uploadedImageDocumentId)
                                        .delete();
                            }
                        });
            } else if (selectedImageUri != null && uploadedImageDocumentId != null) {
                db.collection(BOOK_COLLECTION).document(uploadedImageDocumentId)
                        .delete();
            }
        }
    }
}
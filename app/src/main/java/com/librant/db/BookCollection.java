package com.librant.db;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.librant.models.Book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class BookCollection {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public interface OnBooksLoadedListener {
        void onBooksLoaded(List<Book> bookList);
        void onFailure(Exception e);
    }

    public interface OnPendingBooksCountListener {
        void onCountLoaded(int count);
        void onFailure(Exception e);
    }

    public void getPendingBooksCount(OnPendingBooksCountListener listener) {
        db.collection("pendingBooks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        listener.onCountLoaded(count);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void getUserBooks(String userId, OnBooksLoadedListener listener) {
        List<Book> bookList = new ArrayList<>();

        db.collection("books")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("approved", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            bookList.add(book);
                        }
                        listener.onBooksLoaded(bookList);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void fetchLatestBooks(OnBooksLoadedListener listener, ExecutorService executorService) {
        executorService.execute(() -> {
            db.collection("books")
                    .whereEqualTo("approved", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<String> allBookIds = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                String availability = book.getAvailability();
                                if (availability != null && (!book.getAvailability().equals("Hidden") || availability.equals("Unavailable"))) {
                                    allBookIds.add(book.getBookId());
                                }
                            }

                            Collections.shuffle(allBookIds);
                            List<String> randomBookIds = allBookIds.subList(0, Math.min(10, allBookIds.size()));

                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for (String bookId : randomBookIds) {
                                tasks.add(db.collection("books").document(bookId).get());
                            }

                            Tasks.whenAllSuccess(tasks).addOnCompleteListener(innerTask -> {
                                if (innerTask.isSuccessful()) {
                                    List<Book> randomBooks = new ArrayList<>();
                                    for (Object result : innerTask.getResult()) {
                                        DocumentSnapshot document = (DocumentSnapshot) result;
                                        if (document.exists()) {
                                            randomBooks.add(document.toObject(Book.class));
                                        }
                                    }
                                    listener.onBooksLoaded(randomBooks);
                                } else {
                                    listener.onFailure(innerTask.getException());
                                }
                            });
                        } else {
                            listener.onFailure(task.getException());
                        }
                    });
        });
    }

    public void fetchRecentlyViewedBooks(String userId, OnBooksLoadedListener listener) {
        List<Book> bookList = new ArrayList<>();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> recentlyViewedBookIds = (List<String>) documentSnapshot.get("viewedBooks");
                        if (recentlyViewedBookIds != null && !recentlyViewedBookIds.isEmpty()) {
                            db.collection("books")
                                    .whereIn("bookId", recentlyViewedBookIds)
                                    .whereEqualTo("approved", true)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Book book = document.toObject(Book.class);
                                                bookList.add(book);
                                            }
                                            listener.onBooksLoaded(bookList);
                                        } else {
                                            listener.onFailure(task.getException());
                                        }
                                    });
                        } else {
                            listener.onBooksLoaded(bookList);
                        }
                    } else {
                        listener.onBooksLoaded(bookList);
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void fetchBooksByGenre(String genre, OnBooksLoadedListener listener, ExecutorService executorService) {
        executorService.execute(() -> {
            db.collection("books")
                    .whereArrayContains("genres", genre)
                    .whereEqualTo("approved", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Book> bookList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                String availability = book.getAvailability();
                                if (availability != null && (availability.equals("Available") || availability.equals("Unavailable"))) {
                                    bookList.add(book);
                                }
                            }
                            listener.onBooksLoaded(bookList);
                        } else {
                            listener.onFailure(task.getException());
                        }
                    });
        });
    }

    public void fetchUserSavedBooks(String userId, OnBooksLoadedListener listener) {
        List<Book> bookList = new ArrayList<>();

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> bookIds = (List<String>) document.get("savedBooks");
                            if (bookIds == null || bookIds.isEmpty()) {
                                listener.onBooksLoaded(bookList);
                            } else {
                                for (String bookId : bookIds) {
                                    db.collection("books")
                                            .whereEqualTo("bookId", bookId)
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                                    Book book = documentSnapshot.toObject(Book.class);
                                                    bookList.add(book);
                                                }
                                                listener.onBooksLoaded(bookList);
                                            })
                                            .addOnFailureListener(e -> {
                                                listener.onFailure(e);
                                            });
                                }
                            }
                        } else {
                            listener.onBooksLoaded(bookList);
                        }
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void loadPendingBooks(OnBooksLoadedListener listener) {
        db.collection("pendingBooks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Book> books = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            books.add(book);
                        }
                        listener.onBooksLoaded(books);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void deleteBook(Book book, String userId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        String bookId = book.getBookId();
        String imageUrl = book.getImageUrl();

        if (book.getOwnerId().equals(userId)) {
            db.collection("books").document(bookId).delete()
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);

            deleteImageFromStorage(imageUrl, onSuccessListener, onFailureListener);
            removeFromUserCollections(bookId);
        } else {
            onFailureListener.onFailure(new Exception("You do not have permission to delete this book."));
        }
    }

    private void deleteImageFromStorage(String imageUrl, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference photoRef = storage.getReferenceFromUrl(imageUrl);
            photoRef.delete().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
        }
    }

    private void removeFromUserCollections(String bookId) {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<String> viewedBooks = (List<String>) document.get("viewedBooks");
                    List<String> savedBooks = (List<String>) document.get("savedBooks");

                    boolean needsUpdate = false;
                    if (viewedBooks != null && viewedBooks.contains(bookId)) {
                        viewedBooks.remove(bookId);
                        needsUpdate = true;
                    }
                    if (savedBooks != null && savedBooks.contains(bookId)) {
                        savedBooks.remove(bookId);
                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("viewedBooks", viewedBooks);
                        updates.put("savedBooks", savedBooks);
                        db.collection("users").document(document.getId()).update(updates);
                    }
                }
            }
        });
    }

}
package com.librant.db;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.librant.models.Book;

import java.util.ArrayList;
import java.util.List;

public class BookCollection {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnBooksLoadedListener {
        void onBooksLoaded(List<Book> bookList);
        void onFailure(Exception e);
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

    private void fetchBooksByGenre(String genre, OnBooksLoadedListener listener) {
        List<Book> bookList = new ArrayList<>();

        db.collection("books")
                .whereArrayContains("genres", genre)
                .whereEqualTo("approved", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
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


    //TODO: Modify the pending books fetching method
//    private void fetchPendingBooks() {
//        String currentUserId = user.getUid();
//
//        DocumentReference userRef = db.collection("users").document(currentUserId);
//        userRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document != null && document.exists()) {
//                    if (document.getBoolean("isAdmin") == null) {
//                        userRef.update("isAdmin", false);
//                    } else {
//                        boolean isAdmin = document.getBoolean("isAdmin");
//
//                        if (isAdmin) {
//                            db.collection("pendingBooks")
//                                    .get()
//                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                            if (task.isSuccessful()) {
//                                                pendingBookList.clear();
//                                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                                    Book book = document.toObject(Book.class);
//                                                    pendingBookList.add(book);
//                                                }
//
//                                                pendingBookAdapter.notifyDataSetChanged();
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                }
//            }
//        });
//    }
}
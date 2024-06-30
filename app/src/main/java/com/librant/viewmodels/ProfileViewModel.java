package com.librant.viewmodels;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.librant.db.BookCollection;
import com.librant.models.Book;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<List<Book>> userBooks = new MutableLiveData<>();
    private final MutableLiveData<List<Book>> recentlyViewedBooks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final BookCollection bookCollection = new BookCollection();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final long DEBOUNCE_DELAY = 500;

    public LiveData<List<Book>> getUserBooks() {
        return userBooks;
    }

    public LiveData<List<Book>> getRecentlyViewedBooks() {
        return recentlyViewedBooks;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchUserBooks() {
        debounceFetch(() -> {
            isLoading.postValue(true);
            String userId = mAuth.getCurrentUser().getUid();
            bookCollection.getUserBooks(userId, new BookCollection.OnBooksLoadedListener() {
                @Override
                public void onBooksLoaded(List<Book> books) {
                    isLoading.postValue(false);
                    userBooks.postValue(books);
                }

                @Override
                public void onFailure(Exception e) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Failed to load user books: " + e.getMessage());
                }
            });
        });
    }

    public void fetchRecentlyViewedBooks() {
        debounceFetch(() -> {
            isLoading.postValue(true);
            String userId = mAuth.getCurrentUser().getUid();
            bookCollection.fetchRecentlyViewedBooks(userId, new BookCollection.OnBooksLoadedListener() {
                @Override
                public void onBooksLoaded(List<Book> books) {
                    isLoading.postValue(false);
                    recentlyViewedBooks.postValue(books);
                }

                @Override
                public void onFailure(Exception e) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Failed to load recently viewed books: " + e.getMessage());
                }
            });
        });
    }

    private void debounceFetch(Runnable fetchDataRunnable) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(fetchDataRunnable, DEBOUNCE_DELAY);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacksAndMessages(null);
    }
}
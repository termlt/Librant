package com.librant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.librant.R;
import com.librant.activities.BookDetailsActivity;
import com.librant.adapters.BookAdapter;
import com.librant.db.BookCollection;
import com.librant.models.Book;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {
    private BookCollection bookCollection;
    private List<Book> savedBooks;
    private BookAdapter bookAdapter;
    private LinearLayout noSavedBooksLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        bookCollection = new BookCollection();
        noSavedBooksLayout = view.findViewById(R.id.noSavedBooksLayout);
        savedBooks = new ArrayList<>();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bookAdapter = new BookAdapter(savedBooks);
        recyclerView.setAdapter(bookAdapter);

        bookAdapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(getActivity(), BookDetailsActivity.class);
            intent.putExtra("book", (Parcelable) book);
            startActivity(intent);
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchUserSavedBooks);

        fetchUserSavedBooks();
        return view;
    }

    private void fetchUserSavedBooks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            bookCollection.fetchUserSavedBooks(user.getUid(), new BookCollection.OnBooksLoadedListener() {
                @Override
                public void onBooksLoaded(List<Book> bookList) {
                    savedBooks.clear();
                    savedBooks.addAll(bookList);
                    bookAdapter.notifyDataSetChanged();
                    noSavedBooksLayout.setVisibility(bookList.isEmpty() ? View.VISIBLE : View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Exception e) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            savedBooks.clear();
            bookAdapter.notifyDataSetChanged();
            noSavedBooksLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}

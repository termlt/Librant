package com.librant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.librant.activities.BookDetailsActivity;
import com.librant.adapters.BookAdapter;
import com.librant.databinding.FragmentSearchBinding;
import com.librant.models.Book;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private BookAdapter bookAdapter;
    private ArrayList<Book> bookList;
    private FirebaseFirestore db;
    private ConstraintLayout booksNotFoundLayout;
    private LinearLayout searchAnythingLayout;
    private FragmentSearchBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        SearchView searchView = binding.searchView;
        SearchBar searchBar = binding.searchBar;
        RecyclerView recyclerView = binding.bookRecyclerView;
        booksNotFoundLayout = binding.booksNotFoundLayout;
        searchAnythingLayout = binding.searchAnythingLayout;

        bookList = new ArrayList<>();

        bookAdapter = new BookAdapter(bookList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(bookAdapter);

        bookAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                Intent intent = new Intent(getActivity(), BookDetailsActivity.class);
                intent.putExtra("book", (Parcelable) book);
                startActivity(intent);
            }
        });

        db = FirebaseFirestore.getInstance();

        searchView.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchBar.setText(v.getText());
                searchBooks(searchView.getText().toString());
                searchView.handleBackInvoked();
                return true;
            }
        });

        return binding.getRoot();
    }

    private void searchBooks(String searchText) {
        if (searchText.isEmpty()) {
            searchAnythingLayout.setVisibility(View.VISIBLE);
            booksNotFoundLayout.setVisibility(View.GONE);

            bookList.clear();
            bookAdapter.notifyDataSetChanged();

            return;
        }

        searchAnythingLayout.setVisibility(View.GONE);
        db.collection("books")
                .whereEqualTo("approved", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            if (book.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                                if (book.getAvailability() != null && !book.getAvailability().equals("Hidden")) {
                                    bookList.add(book);
                                }
                            }
                        }

                        if (bookList.isEmpty()) {
                            booksNotFoundLayout.setVisibility(View.VISIBLE);
                        } else {
                            booksNotFoundLayout.setVisibility(View.GONE);
                        }
                        bookAdapter.notifyDataSetChanged();
                    }
                });
    }
}

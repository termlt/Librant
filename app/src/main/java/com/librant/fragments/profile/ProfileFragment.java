package com.librant.fragments.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.librant.R;
import com.librant.activities.BookDetailsActivity;
import com.librant.activities.EditBookActivity;
import com.librant.activities.EditProfileActivity;
import com.librant.activities.auth.MainActivity;
import com.librant.adapters.BookAdapter;
import com.librant.databinding.FragmentProfileBinding;
import com.librant.db.BookCollection;
import com.librant.fragments.EmptyBooksFragment;
import com.librant.models.Book;
import com.librant.viewmodels.ProfileViewModel;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private BookAdapter bookAdapter;
    private final String[] options = {"My Books", "Recently Viewed"};
    private TextView userNameTextView, contactInfoTextView, editBookTextView;
    private ImageView editProfileButton, logoutButton;
    private ProfileViewModel viewModel;
    private RecyclerView bookRecyclerView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FragmentProfileBinding binding;
    private BookCollection bookCollection;
    private LinearLayout noSavedBooksLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        bookCollection = new BookCollection();

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        bindViews();
        setupObservers();

        initializeTabs();
        updateUserInfo(userNameTextView, contactInfoTextView);

        if (user == null) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        }

        return view;
    }

    private void setupObservers() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                bookAdapter.setBooks(new ArrayList<>());
                bookAdapter.notifyDataSetChanged();

                if (tab.getPosition() == 0) {
                    viewModel.getUserBooks().observe(getViewLifecycleOwner(), books -> {
                        bookAdapter.setBooks(books);
                        bookAdapter.notifyDataSetChanged();
                        toggleNoBooksLayout(books.isEmpty());
                    });
                    viewModel.fetchUserBooks();
                    attachItemTouchHelper();
                } else {
                    editBookTextView.setVisibility(View.GONE);
                    viewModel.getRecentlyViewedBooks().observe(getViewLifecycleOwner(), books -> {
                        bookAdapter.setBooks(books);
                        bookAdapter.notifyDataSetChanged();
                        toggleNoBooksLayout(books.isEmpty());
                    });
                    viewModel.fetchRecentlyViewedBooks();
                    detachItemTouchHelper();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewModel.getUserBooks().removeObservers(getViewLifecycleOwner());
                viewModel.getRecentlyViewedBooks().removeObservers(getViewLifecycleOwner());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
    }

    private void toggleNoBooksLayout(boolean show) {
        if (show) {
            noSavedBooksLayout.setVisibility(View.VISIBLE);
            editBookTextView.setVisibility(View.GONE);
            bookRecyclerView.setVisibility(View.GONE);
        } else {
            noSavedBooksLayout.setVisibility(View.GONE);

            if (tabLayout.getSelectedTabPosition() == 0) {
                editBookTextView.setVisibility(View.VISIBLE);
            } else {
                editBookTextView.setVisibility(View.GONE);
            }
            bookRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void bindViews() {
        userNameTextView = binding.userNameTextView;
        contactInfoTextView = binding.contactInfoTextView;
        editBookTextView = binding.editTextView;
        bookRecyclerView = binding.bookRecyclerView;
        viewPager = binding.viewPager;
        tabLayout = binding.optionsLayout;
        editProfileButton = binding.editProfileButton;
        logoutButton = binding.logoutConfirmationButton;
        noSavedBooksLayout = binding.noSavedBooksLayout;

        editProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());

        bookAdapter = new BookAdapter(new ArrayList<>());
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        bookRecyclerView.setAdapter(bookAdapter);

        bookAdapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(getActivity(), BookDetailsActivity.class);
            intent.putExtra("book", (Parcelable) book);
            startActivity(intent);
        });
    }

    private void attachItemTouchHelper() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (tabLayout.getSelectedTabPosition() == 0 && viewModel.getUserBooks().getValue() != null &&
                        !viewModel.getUserBooks().getValue().isEmpty() &&
                        position < viewModel.getUserBooks().getValue().size()) {
                    showEditOrDeleteDialog(position);
                    bookAdapter.notifyItemChanged(position);
                } else {
                    bookAdapter.notifyItemChanged(position);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(bookRecyclerView);
    }

    private void detachItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        }).attachToRecyclerView(bookRecyclerView);
    }

    private void initializeTabs() {
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return new EmptyBooksFragment();
            }

            @Override
            public int getItemCount() {
                return options.length;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(options[position])).attach();
    }

    private void showEditOrDeleteDialog(int position) {
        Book book = bookAdapter.getBooks().get(position);

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
                .setTitle(book.getTitle())
                .setMessage("Do you want to edit or delete this book?")
                .setNegativeButton("Delete", (dialog, which) -> {
                    bookCollection.deleteBook(book, mAuth.getCurrentUser().getUid(),
                            aVoid -> {
                                Snackbar.make(requireView(), "Book deleted successfully.", Snackbar.LENGTH_LONG).show();
                                bookAdapter.removeBook(position);
                            },
                            e -> Snackbar.make(requireView(), "Something went wrong.", Snackbar.LENGTH_LONG).show());
                })
                .setPositiveButton("Edit", (dialog, which) -> {
                    Intent editBookIntent = new Intent(getActivity(), EditBookActivity.class);
                    editBookIntent.putExtra("book", (Parcelable) book);
                    startActivity(editBookIntent);
                })
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);
    }

    private void showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.logout_confirmation_dialog, null);
        AlertDialog alertDialog = builder.setView(dialogView).create();

        MaterialButton btnProceed = dialogView.findViewById(R.id.logoutButton);
        MaterialButton btnCancel = dialogView.findViewById(R.id.cancelButton);

        btnProceed.setOnClickListener(v -> {
            alertDialog.dismiss();
            mAuth.signOut();
            startActivity(new Intent(getActivity(), MainActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    private void updateUserInfo(TextView username, TextView contactInfoTextView) {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (!documentSnapshot.getString("name").isEmpty() || !documentSnapshot.getString("surname").isEmpty()) {
                            username.setText(documentSnapshot.getString("name") + " " + documentSnapshot.getString("surname"));
                        } else {
                            username.setText("Anonymous");
                        }
                        String address = documentSnapshot.getString("address");
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        if (address != null && !address.isEmpty()) {
                            contactInfoTextView.setText(address);
                        } else if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            contactInfoTextView.setText(phoneNumber);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        viewModel.getUserBooks().removeObservers(getViewLifecycleOwner());
        viewModel.getRecentlyViewedBooks().removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserInfo(userNameTextView, contactInfoTextView);
        viewModel.fetchUserBooks();
    }
}

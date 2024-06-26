package com.librant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.librant.R;
import com.librant.activities.AddBookActivity;
import com.librant.activities.BookDetailsActivity;
import com.librant.activities.auth.MainActivity;
import com.librant.adapters.BookAdapter;
import com.librant.databinding.FragmentHomeBinding;
import com.librant.db.BookCollection;
import com.librant.db.UserCollection;
import com.librant.models.Book;
import com.librant.util.FillDetailsBottomSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private BookAdapter bookAdapter;
    private final List<Book> bookList = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int tabPosition;
    private final String[] genres = {"For You", "Adventure", "Detective", "Fiction",
            "Psychology", "Philosophy", "Romance", "Horror", "Mystery", "Sci-Fi", "Other"};
    private ImageView addBookButton;
    private ImageView notificationsButton;
    private LinearLayout linearLayoutEmptyBooks;
    private FragmentHomeBinding binding;
    private UserCollection userCollection;
    private BookCollection bookCollection;
    private ExecutorService executorService;
    private Handler mainHandler;
    private boolean isAdmin = false;
    private RecyclerView pendingBooksRecyclerView;
    private BookAdapter latestBooksAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupViews();
        initializeTabs();
        checkUserDetails();

        if (user == null) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        }

        return view;
    }

    private void setupViews() {
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userCollection = new UserCollection();
        bookCollection = new BookCollection();
        swipeRefreshLayout = binding.swipeRefreshLayout;
        addBookButton = binding.addBookButton;
        notificationsButton = binding.notificationsButton;
        linearLayoutEmptyBooks = binding.emptyBooksLayout;
        drawerLayout = binding.drawerLayout;

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        checkUserContacts();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (tabPosition == 0) fetchLatestBooks();
            else {
                String selectedGenre = genres[tabPosition];
                fetchBooksByGenre(selectedGenre);
            }
        });

        addBookButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddBookActivity.class)));

        checkIfAdmin();
    }

    private void checkUserContacts() {
        userCollection.getUserById(user.getUid(), receivedUser -> {
            if (receivedUser != null && !receivedUser.getAddress().isEmpty() ||
                    receivedUser != null && !receivedUser.getPhoneNumber().isEmpty()) {
                addBookButton.setVisibility(View.VISIBLE);
            } else {
                addBookButton.setVisibility(View.GONE);
            }
        });
    }

    private void checkIfAdmin() {
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Boolean isAdmin = document.getBoolean("isAdmin");
                            if (isAdmin != null && isAdmin) {
                                this.isAdmin = true;
                                setupDrawerLayout();
                                notificationsButton.setVisibility(View.VISIBLE);
                                setNotificationsBadge();
                            } else {
                                notificationsButton.setVisibility(View.GONE);
                            }
                        } else {
                            notificationsButton.setVisibility(View.GONE);
                        }
                    } else {
                        notificationsButton.setVisibility(View.GONE);
                    }
                });
    }

    private void setNotificationsBadge() {
        bookCollection.getPendingBooksCount(new BookCollection.OnPendingBooksCountListener() {
            @OptIn(markerClass = ExperimentalBadgeUtils.class)
            @Override
            public void onCountLoaded(int count) {
                BadgeDrawable badgeDrawable = BadgeDrawable.create(requireContext());
                badgeDrawable.setNumber(count);
                badgeDrawable.setVisible(count > 0);

                BadgeUtils.attachBadgeDrawable(badgeDrawable, notificationsButton, null);
            }

            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void initializeTabs() {
        TabLayout tabLayout = binding.genresLayout;
        ViewPager2 viewPager = binding.viewPager;
        viewPager.setUserInputEnabled(false);
        RecyclerView bookRecyclerView = binding.bookRecyclerView;

        bookAdapter = new BookAdapter(bookList);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bookRecyclerView.setAdapter(bookAdapter);

        fetchLatestBooks();

        bookAdapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(getActivity(), BookDetailsActivity.class);
            intent.putExtra("book", (Parcelable) book);
            startActivity(intent);
        });

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return new EmptyBooksFragment();
            }

            @Override
            public int getItemCount() {
                return genres.length;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(genres[position])).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();

                if (tab.getPosition() == 0) {
                    fetchLatestBooks();
                } else {
                    fetchBooksByGenre(genres[tab.getPosition()]);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void checkUserDetails() {
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String name = document.getString("name");
                            String surname = document.getString("surname");
                            String phoneNumber = document.getString("phoneNumber");
                            String address = document.getString("address");

                            if (name == null || name.isEmpty() ||
                                    surname == null || surname.isEmpty()) {
                                showFillDetailsBottomSheet();
                            } else if ((phoneNumber == null || phoneNumber.isEmpty()) &&
                                    (address == null || address.isEmpty())) {
                                showFillDetailsBottomSheet();
                            }
                        } else {
                            showFillDetailsBottomSheet();
                        }
                    }
                });
    }

    private void setupDrawerLayout() {
        pendingBooksRecyclerView = binding.drawerLayout.findViewById(R.id.latestBooksRecyclerView);
        latestBooksAdapter = new BookAdapter(new ArrayList<>());
        pendingBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingBooksRecyclerView.setAdapter(latestBooksAdapter);

        notificationsButton.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            } else {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                loadPendingBooks();
            }
        });
    }

    private void fetchLatestBooks() {
        swipeRefreshLayout.setRefreshing(true);
        bookCollection.fetchLatestBooks(new BookCollection.OnBooksLoadedListener() {
            @Override
            public void onBooksLoaded(List<Book> books) {
                mainHandler.post(() -> {
                    bookList.clear();
                    bookList.addAll(books);
                    bookAdapter.notifyDataSetChanged();

                    if (bookList.isEmpty()) {
                        linearLayoutEmptyBooks.setVisibility(View.VISIBLE);
                    } else {
                        linearLayoutEmptyBooks.setVisibility(View.GONE);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onFailure(Exception e) {
                mainHandler.post(() -> swipeRefreshLayout.setRefreshing(false));
            }
        }, executorService);
    }

    private void fetchBooksByGenre(String genre) {
        swipeRefreshLayout.setRefreshing(true);
        bookCollection.fetchBooksByGenre(genre, new BookCollection.OnBooksLoadedListener() {
            @Override
            public void onBooksLoaded(List<Book> books) {
                mainHandler.post(() -> {
                    bookList.clear();
                    bookList.addAll(books);
                    bookAdapter.notifyDataSetChanged();

                    if (bookList.isEmpty()) {
                        linearLayoutEmptyBooks.setVisibility(View.VISIBLE);
                    } else {
                        linearLayoutEmptyBooks.setVisibility(View.GONE);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onFailure(Exception e) {
                mainHandler.post(() -> swipeRefreshLayout.setRefreshing(false));
            }
        }, executorService);
    }

    private void loadPendingBooks() {
        latestBooksAdapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(getActivity(), BookDetailsActivity.class);
            intent.putExtra("book", (Parcelable) book);
            startActivity(intent);
        });

        bookCollection.loadPendingBooks(new BookCollection.OnBooksLoadedListener() {
            @Override
            public void onBooksLoaded(List<Book> books) {
                latestBooksAdapter.setBooks(books);
            }

            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void showFillDetailsBottomSheet() {
        FillDetailsBottomSheet bottomSheet = new FillDetailsBottomSheet();
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public void onResume() {
        super.onResume();

        checkUserContacts();

        if (!(tabPosition == 0)) {
            fetchBooksByGenre(genres[tabPosition]);
        }

        if (isAdmin) setNotificationsBadge();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
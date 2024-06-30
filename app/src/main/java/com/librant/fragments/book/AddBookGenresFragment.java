package com.librant.fragments.book;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.librant.R;
import com.librant.activities.HomeActivity;
import com.librant.models.Book;

import java.util.ArrayList;
import java.util.List;

public class AddBookGenresFragment extends Fragment {
    private Book book;
    private List<String> selectedGenres = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_book_genres, container, false);

        Bundle args = getArguments();
        if (args != null) {
            book = (Book) args.getSerializable("book");
        }

        root.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
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

        MaterialButton continueButton = root.findViewById(R.id.continueButton);
        continueButton.setEnabled(false);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                book.setGenres(selectedGenres);

                AddBookInfoFragment uploadImageFragment = new AddBookInfoFragment();
                args.putSerializable("book", book);
                uploadImageFragment.setArguments(args);

                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container_add_book, uploadImageFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        ChipGroup chipGroup = root.findViewById(R.id.chipGroup);
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String genre = chip.getText().toString();
                    if (isChecked) {
                        if (!selectedGenres.contains(genre)) {
                            selectedGenres.add(genre);
                            System.out.println(selectedGenres);
                        }
                    } else {
                        selectedGenres.remove(genre);
                    }
                    if (!selectedGenres.isEmpty()) {
                        continueButton.setBackgroundColor(getResources().getColor(R.color.light_green));
                        continueButton.setEnabled(true);
                    } else {
                        continueButton.setBackgroundColor(getResources().getColor(R.color.button_gray));
                        continueButton.setEnabled(false);
                    }

                }
            });
        }

        return root;
    }

}

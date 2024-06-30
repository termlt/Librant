package com.librant.fragments.book;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.librant.databinding.FragmentBookAddedBinding;

public class BookAddedFragment extends Fragment {
    private FragmentBookAddedBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookAddedBinding.inflate(inflater, container, false);

        binding.btnBack.setOnClickListener(v -> getActivity().finish());
        binding.backHomeButton.setOnClickListener(v -> getActivity().finish());

        return binding.getRoot();
    }
}

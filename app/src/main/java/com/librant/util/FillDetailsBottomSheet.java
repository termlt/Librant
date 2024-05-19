package com.librant.util;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.librant.R;
import com.librant.activities.EditProfileActivity;

public class FillDetailsBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_fill_details, container, false);

        Button proceedButton = view.findViewById(R.id.proceedButton);

        proceedButton.setOnClickListener(v -> {
            navigateToEditProfileFragment();
            dismiss();
        });

        return view;
    }

    private void navigateToEditProfileFragment() {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        }
    }
}

package com.librant.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.librant.R;

public class BookOwnerHistoryFragment extends BottomSheetDialogFragment {
    private String ownerHistory;

    public BookOwnerHistoryFragment(String ownerHistory) {
        this.ownerHistory = ownerHistory;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_owner_history, container, false);
        TextView tvOwnerHistory = view.findViewById(R.id.tvOwnerHistory);
        tvOwnerHistory.setText(ownerHistory);
        return view;
    }
}

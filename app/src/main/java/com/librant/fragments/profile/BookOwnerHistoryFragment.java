package com.librant.fragments.profile;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textview.MaterialTextView;
import com.librant.R;

public class BookOwnerHistoryFragment extends BottomSheetDialogFragment {
    private static final String ARG_HISTORY = "history";
    private String history;

    public static BookOwnerHistoryFragment newInstance(String history) {
        BookOwnerHistoryFragment fragment = new BookOwnerHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HISTORY, history);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            history = getArguments().getString(ARG_HISTORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_owner_history, container, false);
        MaterialTextView historyTextView = view.findViewById(R.id.historyTextView);
        historyTextView.setText(fromHtml(history));
        return view;
    }

    @SuppressWarnings("deprecation")
    private Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }
}


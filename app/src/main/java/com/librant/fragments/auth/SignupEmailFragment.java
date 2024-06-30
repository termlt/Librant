package com.librant.fragments.auth;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.librant.R;
import com.librant.activities.PrivacyPolicyActivity;
import com.librant.activities.TermsConditionsActivity;
import com.librant.activities.auth.MainActivity;
import com.librant.databinding.FragmentSignupEmailBinding;

public class SignupEmailFragment extends Fragment {

    private FragmentSignupEmailBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupEmailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.btnBack.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        binding.email.addTextChangedListener(textWatcher);
        binding.password.addTextChangedListener(textWatcher);
        binding.confirmPassword.addTextChangedListener(textWatcher);

        binding.btnCreateAccount.setOnClickListener(v -> {
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();
            String confirmPassword = binding.confirmPassword.getText().toString().trim();

            if (!password.equals(confirmPassword)) {
                Snackbar.make(binding.getRoot(), "Passwords do not match", Snackbar.LENGTH_SHORT).show();
            } else if (password.length() > 20 || confirmPassword.length() > 20){
                Snackbar.make(binding.getRoot(), "Password length should not exceed 20 characters", Snackbar.LENGTH_SHORT).show();
            } else {
                signUpWithEmail(email, password);
            }
        });

        setTermsAndPrivacyText();

        return view;
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            checkFieldsForEmptyValues();
        }
    };

    private void checkFieldsForEmptyValues() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String confirmPassword = binding.confirmPassword.getText().toString().trim();

        boolean allFieldsFilled = !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty();
        boolean passwordsMatch = password.equals(confirmPassword);

        if (allFieldsFilled && passwordsMatch && !binding.btnCreateAccount.isEnabled()) {
            binding.btnCreateAccount.setEnabled(true);
            binding.btnCreateAccount.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorEnabled));
            binding.btnCreateAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.textColorEnabled));
            fadeButton(binding.btnCreateAccount, 0.6f, 1f);
        } else if ((!allFieldsFilled || !passwordsMatch) && binding.btnCreateAccount.isEnabled()) {
            binding.btnCreateAccount.setEnabled(false);
            binding.btnCreateAccount.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorDisabled));
            binding.btnCreateAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.textColorDisabled));
            fadeButton(binding.btnCreateAccount, 1f, 0.6f);
        }
    }

    private void fadeButton(View view, float startAlpha, float endAlpha) {
        ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(view, "alpha", startAlpha, endAlpha);
        fadeAnimator.setDuration(300);
        fadeAnimator.start();
    }

    private void setTermsAndPrivacyText() {
        String text = getString(R.string.terms_and_privacy);
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(getActivity(), TermsConditionsActivity.class);
                startActivity(intent);
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(getActivity(), PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        };

        String termsText = "Terms of Use";
        String privacyText = "Privacy Policy";

        int termsStart = text.indexOf(termsText);
        int termsEnd = termsStart + termsText.length();
        int privacyStart = text.indexOf(privacyText);
        int privacyEnd = privacyStart + privacyText.length();

        int customColor = getResources().getColor(R.color.main_text_color);

        spannableString.setSpan(new UnderlineSpan(), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(termsSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(customColor), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new UnderlineSpan(), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacySpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(customColor), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvTermsPrivacy.setText(spannableString);
        binding.tvTermsPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private void signUpWithEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Snackbar.make(binding.getRoot(), "Sign up successful. Verification email sent.", Snackbar.LENGTH_SHORT).show();
                                            new Handler().postDelayed(() -> {
                                                startActivity(new Intent(getActivity(), MainActivity.class));
                                                getActivity().finish();
                                            }, 4000);
                                        } else {
                                            Snackbar.make(binding.getRoot(), "Failed to send verification email: " + verificationTask.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Snackbar.make(binding.getRoot(), "Sign up failed: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
}

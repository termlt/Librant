package com.librant.fragments.auth;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.SpannableString;
import android.text.Spanned;
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
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.librant.BuildConfig;
import com.librant.R;
import com.librant.activities.HomeActivity;
import com.librant.activities.PrivacyPolicyActivity;
import com.librant.activities.TermsConditionsActivity;
import com.librant.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FragmentLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.btnBack.setOnClickListener(v -> getActivity().finish());

        binding.btnSignup.setOnClickListener(v -> {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            transaction.replace(R.id.fragment_container, new SignupFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        binding.btnLoginGoogle.setOnClickListener(v -> signInWithGoogle());

        binding.btnLogin.setOnClickListener(v -> loginWithEmail());

        setTermsAndPrivacyText();

        binding.email.addTextChangedListener(loginTextWatcher);
        binding.password.addTextChangedListener(loginTextWatcher);

        return view;
    }

    private final TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            checkFieldsForEmptyValues();
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private void checkFieldsForEmptyValues() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        boolean allFieldsFilled = !email.isEmpty() && !password.isEmpty();

        if (allFieldsFilled && !binding.btnLogin.isEnabled()) {
            binding.btnLogin.setEnabled(true);
            binding.btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorEnabled));
            binding.btnLogin.setTextColor(ContextCompat.getColor(getActivity(), R.color.textColorEnabled));
            fadeButton(binding.btnLogin, 0.6f, 1f);
        } else if (!allFieldsFilled && binding.btnLogin.isEnabled()) {
            binding.btnLogin.setEnabled(false);
            binding.btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorDisabled));
            binding.btnLogin.setTextColor(ContextCompat.getColor(getActivity(), R.color.textColorDisabled));
            fadeButton(binding.btnLogin, 1f, 0.6f);
        }
    }

    private void fadeButton(View view, float startAlpha, float endAlpha) {
        ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(view, "alpha", startAlpha, endAlpha);
        fadeAnimator.setDuration(300);
        fadeAnimator.start();
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Snackbar.make(binding.getRoot(), "Google sign in failed", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            startActivity(new Intent(getActivity(), HomeActivity.class));
                            getActivity().finish();
                        } else {
                            Snackbar.make(binding.getRoot(), "Please verify your email address.", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(binding.getRoot(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginWithEmail() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        if (email.isEmpty()) {
            binding.email.setError("Email is required.");
            return;
        }

        if (password.isEmpty()) {
            binding.password.setError("Password is required.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    startActivity(new Intent(getActivity(), HomeActivity.class));
                    getActivity().finish();
                } else {
                    Snackbar.make(binding.getRoot(), "Please verify your email address.", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(binding.getRoot(), "Login Failed. Please check your credentials.", Snackbar.LENGTH_SHORT).show();
            }
        });
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
}

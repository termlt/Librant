package com.librant.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.librant.R;
import com.librant.activities.HomeActivity;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        Button loginButton = findViewById(R.id.login_button);
        TextView signUpTextButton = findViewById(R.id.signup_text);
        AppCompatButton backButton = findViewById(R.id.back_button);

        loginButton.setOnClickListener(this::loginWithEmailAndPassword);

        signUpTextButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        backButton.setOnClickListener(v -> finish());
    }


    private void loginWithEmailAndPassword(View view) {
        TextInputLayout textInputLayoutEmail = findViewById(R.id.outlined_TextField_email);
        TextInputLayout textInputLayoutPassword = findViewById(R.id.outlined_TextField_password);

        String email = String.valueOf(textInputLayoutEmail.getEditText().getText());
        String password = String.valueOf(textInputLayoutPassword.getEditText().getText());

        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(view, "Fields can not be empty",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Snackbar.make(view, "Email not verified. Please check your inbox",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(view, "Login Failed",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
}
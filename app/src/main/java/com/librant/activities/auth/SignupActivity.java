package com.librant.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.librant.R;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        Button signUpButton = findViewById(R.id.signup_button);
        TextView signInTextButton = findViewById(R.id.login_text);
        AppCompatButton backButton = findViewById(R.id.back_button);

        signUpButton.setOnClickListener(this::signUpWithEmailAndPassword);

        signInTextButton.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        backButton.setOnClickListener(v -> finish());
    }


    private void signUpWithEmailAndPassword(View view) {
        TextInputLayout textInputLayoutEmail = findViewById(R.id.outlined_TextField_email);
        TextInputLayout textInputLayoutPassword = findViewById(R.id.outlined_TextField_password);
        TextInputLayout textInputLayoutPasswordConfirm = findViewById(R.id.outlined_TextField_passwordConfirm);

        String email = String.valueOf(textInputLayoutEmail.getEditText().getText());
        String password = String.valueOf(textInputLayoutPassword.getEditText().getText());
        String passwordConfirm = String.valueOf(textInputLayoutPasswordConfirm.getEditText().getText());

        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(view, "Fields can not be empty", Snackbar.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(passwordConfirm)) {
            Snackbar.make(view, "Passwords must match", Snackbar.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendEmailVerification(view);
                        } else {
                            Snackbar.make(view, "Registration Failed",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendEmailVerification(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Snackbar.make(view, "Registration Success. Verification email sent",
                                        Snackbar.LENGTH_SHORT).show();
                                new Handler().postDelayed(() -> {
                                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                    finish();
                                }, 4000);
                            } else {
                                Snackbar.make(view, "Failed to send verification email",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

}
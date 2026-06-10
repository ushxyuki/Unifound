package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword;
    private Button btnCreateAccount;
    private View btnBackLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        auth = FirebaseAuth.getInstance();
        db = FirestoreProvider.getFirestore();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBackLogin = findViewById(R.id.btnBackLogin);

        btnCreateAccount.setOnClickListener(v -> {
            Toast.makeText(CreateAccountActivity.this, "Sign up clicked", Toast.LENGTH_SHORT).show();

            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty()) {
                etFullName.setError("Full name is required");
                etFullName.requestFocus();
                return;
            }

            if (name.length() < 2) {
                etFullName.setError("Enter a valid full name");
                etFullName.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;
            }

            if (!email.toLowerCase().endsWith("@gmail.com")) {
                etEmail.setError("Only Gmail accounts are allowed");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            createAccount(name, email, password);
        });

        if (btnBackLogin != null) {
            btnBackLogin.setOnClickListener(v -> {
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void createAccount(String name, String email, String password) {
        btnCreateAccount.setEnabled(false);
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnCreateAccount.setEnabled(true);

                    if (task.isSuccessful() && auth.getCurrentUser() != null) {

                        String userId = auth.getCurrentUser().getUid();

                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                        auth.getCurrentUser().updateProfile(profileUpdates);

                        Map<String, Object> user = new HashMap<>();
                        user.put("userId", userId);
                        user.put("fullName", name);
                        user.put("email", email);
                        user.put("role", "user");
                        user.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("users")
                                .document(userId)
                                .set(user);

                        Toast.makeText(
                                CreateAccountActivity.this,
                                "Account created successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        goToMain();

                    } else {
                        String errorMessage = "Failed to create account";

                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }

                        Toast.makeText(
                                CreateAccountActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


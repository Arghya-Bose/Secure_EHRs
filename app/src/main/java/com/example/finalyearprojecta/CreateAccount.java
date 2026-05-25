package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateAccount extends AppCompatActivity {

    EditText emailEditText, passwordEditText, confirmPasswordEditText, nameUser;
    Button createAccountBtn;
    ProgressBar progressBar;
    TextView loginBtnTextView;

    RadioGroup roleGroup;
    RadioButton rbPatient, rbDoctor, rbLab;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.edit_email_text);
        passwordEditText = findViewById(R.id.edit_password_text);
        confirmPasswordEditText = findViewById(R.id.edit_conformpassword_text);
        nameUser = findViewById(R.id.name_user);
        createAccountBtn = findViewById(R.id.create_accountButton);
        progressBar = findViewById(R.id.progressBar);
        loginBtnTextView = findViewById(R.id.LoginTextView);

        roleGroup = findViewById(R.id.roleGroup);
        rbPatient = findViewById(R.id.rbPatient);
        rbDoctor = findViewById(R.id.rbDoctor);
        rbLab = findViewById(R.id.rbLab);

        createAccountBtn.setOnClickListener(v -> createAccount());
        loginBtnTextView.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void createAccount() {

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String name = nameUser.getText().toString().trim();

        if (name.isEmpty()) {
            nameUser.setError("Enter name");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter valid email");
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must be 6+ chars");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        // ROLE SELECTION
        String role;
        if (rbDoctor.isChecked()) {
            role = "doctor";
        } else if (rbLab.isChecked()) {
            role = "lab";
        } else {
            role = "patient";
        }

        changeInProgress(true);

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = fAuth.getCurrentUser();

                    // Generate Unique ID here
                    String uniqueId = UUID.randomUUID().toString();

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("FullName", name);
                    userInfo.put("UserEmail", email);
                    userInfo.put("role", role);
                    userInfo.put("uniqueId", uniqueId); // save unique ID

                    fStore.collection("User")
                            .document(user.getUid())
                            .set(userInfo)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Account created. Your ID: " + uniqueId, Toast.LENGTH_LONG).show();
                                changeInProgress(false);
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                changeInProgress(false);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    changeInProgress(false);
                });
    }

    private void changeInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        createAccountBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }
}

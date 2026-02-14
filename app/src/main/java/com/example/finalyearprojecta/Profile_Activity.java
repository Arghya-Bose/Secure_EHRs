package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class Profile_Activity extends AppCompatActivity {

    TextView tvUniqueId, nameTv, emailTv, qrImageOPenBtn;
    Button btnCopyUid;
    ImageView btnEditProfile;
    LinearLayout viewProfileDetail;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String currentUserId;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 🔒 Block screenshots & screen recording
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        // UI
        tvUniqueId = findViewById(R.id.tvUniqueId);
        btnCopyUid = findViewById(R.id.btnCopyUid);
        nameTv = findViewById(R.id.tvName);
        emailTv = findViewById(R.id.tvEmail);
        qrImageOPenBtn = findViewById(R.id.qr_code_tv);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getUid();

        btnEditProfile = findViewById(R.id.edit_profile);
        viewProfileDetail = findViewById(R.id.viewProfileDetail);

        btnEditProfile.setOnClickListener(v -> openEditBottomSheet());

        viewProfileDetail.setOnClickListener(v -> openViewBottomSheet());


        if (currentUserId == null) return;

        // 🔥 Fetch user data
        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvUniqueId.setText(doc.getString("uniqueId"));
                        nameTv.setText(doc.getString("FullName"));
                        emailTv.setText(doc.getString("UserEmail"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to fetch profile",
                                Toast.LENGTH_SHORT).show()
                );

        // 📋 Copy UID
        btnCopyUid.setOnClickListener(v -> {
            String uid = tvUniqueId.getText().toString().trim();
            if (!uid.isEmpty()) {
                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Patient UID", uid);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this,
                        "UID copied",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 🔐 Setup Biometric Authentication
        setupBiometric();

        // 🔒 QR Button Click → Ask for Authentication First
        qrImageOPenBtn.setOnClickListener(v -> {
            biometricPrompt.authenticate(promptInfo);
        });
    }

    // ================= BIOMETRIC SETUP =================
    private void setupBiometric() {

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(
                Profile_Activity.this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        showSecureQr(); // ✅ Show QR after success
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(Profile_Activity.this,
                                "Authentication Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify Your Identity")
                .setSubtitle("Authenticate to view your secure QR code")
                .setDeviceCredentialAllowed(true)
                .build();

    }

    // ================= SHOW QR IN BOTTOM SHEET =================
    private void showSecureQr() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.qr_page, null);
        ImageView bottomQrImage = view.findViewById(R.id.qrImage);

        String uid = tvUniqueId.getText().toString().trim();

        if (!uid.isEmpty()) {
            try {
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.encodeBitmap(
                        uid,
                        BarcodeFormat.QR_CODE,
                        400,
                        400
                );
                bottomQrImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dialog.setContentView(view);
        dialog.show();
    }

    private void openEditBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater()
                .inflate(R.layout.edit_profile_bottom, null);

        EditText etAge = view.findViewById(R.id.etAge);
        EditText etDob = view.findViewById(R.id.etDob);
        EditText etGender = view.findViewById(R.id.etGender);
        EditText etBloodGroup = view.findViewById(R.id.etBloodGroup);
        EditText etContact = view.findViewById(R.id.etContact);
        EditText etEmergency = view.findViewById(R.id.etEmergency);
        Button btnSave = view.findViewById(R.id.btnSaveProfile);

        // 🔥 Load existing data
        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etAge.setText(doc.getString("age"));
                        etDob.setText(doc.getString("dob"));
                        etGender.setText(doc.getString("gender"));
                        etBloodGroup.setText(doc.getString("bloodGroup"));
                        etContact.setText(doc.getString("contact"));
                        etEmergency.setText(doc.getString("emergencyContact"));
                    }
                });

        btnSave.setOnClickListener(v -> {

            if (currentUserId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("age", etAge.getText().toString());
            data.put("dob", etDob.getText().toString());
            data.put("gender", etGender.getText().toString());
            data.put("bloodGroup", etBloodGroup.getText().toString());
            data.put("contact", etContact.getText().toString());
            data.put("emergencyContact", etEmergency.getText().toString());

            db.collection("User")
                    .document(currentUserId)
                    .update(data)   // update same document
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this,
                                "Profile Updated",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        });


        dialog.setContentView(view);
        dialog.show();
    }

    private void openViewBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater()
                .inflate(R.layout.view_profile_bottom, null);

        TextView tvAllDetails = view.findViewById(R.id.tvAllDetails);

        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String details =
                                "Name: " + doc.getString("FullName") + "\n\n" +
                                        "Email: " + doc.getString("UserEmail") + "\n\n" +
                                        "Age: " + doc.getString("age") + "\n\n" +
                                        "DOB: " + doc.getString("dob") + "\n\n" +
                                        "Gender: " + doc.getString("gender") + "\n\n" +
                                        "Blood Group: " + doc.getString("bloodGroup") + "\n\n" +
                                        "Contact: " + doc.getString("contact") + "\n\n" +
                                        "Emergency: " + doc.getString("emergencyContact");

                        tvAllDetails.setText(details);
                    }
                });

        // ✅ Set the content and show the bottom sheet
        dialog.setContentView(view);
        dialog.show();
    }
}

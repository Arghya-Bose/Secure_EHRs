package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.annotation.Nullable;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.finalyearprojecta.databinding.ActivityProfileBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class Profile_Activity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore db, secondFirestore;
    String currentUserId;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    private static final int PICK_IMAGE = 101;
    Uri imageUri;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 🔒 Block screenshots & screen recording
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getUid();

        binding.editProfile.setOnClickListener(v -> openEditBottomSheet());
        binding.viewProfileDetail.setOnClickListener(v -> openViewBottomSheet());
        binding.btnBackView.setOnClickListener(v->{
            finish();
        });

        if (currentUserId == null) return;

        // 🔥 Fetch user data
        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        binding.tvUniqueId.setText(doc.getString("uniqueId"));
                        binding.tvName.setText(doc.getString("FullName"));
                        binding.tvEmail.setText(doc.getString("UserEmail"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to fetch profile",
                                Toast.LENGTH_SHORT).show()
                );

        // 📋 Copy UID
        binding.btnCopyUid.setOnClickListener(v -> {
            String uid = binding.tvUniqueId.getText().toString().trim();
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
        binding.qrCodeTv.setOnClickListener(v -> {
            biometricPrompt.authenticate(promptInfo);
        });

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBRCHYvnSUWqQTW3ZCKJAQBDO6_e1Xh2Ss")
                .setApplicationId("1:654325754:android:fd4258dd800897e4055643")
                .setProjectId("multiscreenapp-27573")
                .build();

        FirebaseApp secondApp;

        try {
            secondApp = FirebaseApp.initializeApp(this, options, "secondApp");
        } catch (IllegalStateException e) {
            secondApp = FirebaseApp.getInstance("secondApp");
        }

        secondFirestore = FirebaseFirestore.getInstance(secondApp);
        binding.picImageToProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        fetchProfileImage();

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

        String uid = binding.tvUniqueId.getText().toString().trim();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            binding.profileImg.setImageURI(imageUri);

            uploadImageToSecondFirestore(imageUri);
        }
    }

    private String imageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            // 🔹 Resize image (max 600x600)
            int maxSize = 600;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            float ratio = Math.min(
                    (float) maxSize / width,
                    (float) maxSize / height
            );

            int newWidth = Math.round(width * ratio);
            int newHeight = Math.round(height * ratio);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    newWidth,
                    newHeight,
                    true
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int quality = 60;  // start compression
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

            // 🔥 Reduce quality more if still large
            while (baos.toByteArray().length > 900000 && quality > 20) {
                baos.reset();
                quality -= 10;
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }

            byte[] imageBytes = baos.toByteArray();

            if (imageBytes.length > 1000000) {
                Toast.makeText(this,
                        "Image still too large. Choose smaller image.",
                        Toast.LENGTH_LONG).show();
                return null;
            }

            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadImageToSecondFirestore(Uri uri) {

        if (currentUserId == null) return;

        String base64Image = imageToBase64(uri);

        if (base64Image == null) {
            Toast.makeText(this, "Image too large", Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String, Object> map = new HashMap<>();
        map.put("profileImage", base64Image);

        secondFirestore.collection("users")
                .document(currentUserId)
                .set(map)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Profile image uploaded",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void fetchProfileImage() {

        if (currentUserId == null) return;

        secondFirestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String base64 = documentSnapshot.getString("profileImage");

                        if (base64 != null) {
                            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap =
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            binding.profileImg.setImageBitmap(bitmap);
                        }
                    }
                });
    }

}

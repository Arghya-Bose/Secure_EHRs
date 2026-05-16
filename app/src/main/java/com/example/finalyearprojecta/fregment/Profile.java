package com.example.finalyearprojecta.fregment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.finalyearprojecta.LoginActivity;
import com.example.finalyearprojecta.R;
import com.example.finalyearprojecta.databinding.FragmentProfileBinding;
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

public class Profile extends Fragment {

    private FragmentProfileBinding binding;

    FirebaseAuth auth;
    FirebaseFirestore db, secondFirestore;
    String currentUserId;

    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    Uri imageUri;

    // ✅ Modern Image Picker (Fragment Safe)
    ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.profileImg.setImageURI(uri);
                    uploadImageToSecondFirestore(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getUid();

        binding.editProfile.setOnClickListener(v -> openEditBottomSheet());
        binding.viewProfileDetail.setOnClickListener(v -> openViewBottomSheet());


        requireActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        if (currentUserId == null) return;

        // 🔥 Fetch Data
        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        binding.tvUniqueId.setText(doc.getString("uniqueId"));
                        binding.tvName.setText(doc.getString("FullName"));
                        binding.tvEmail.setText(doc.getString("UserEmail"));
                    }
                });

        // 📋 Copy UID
        binding.btnCopyUid.setOnClickListener(v -> {
            String uid = binding.tvUniqueId.getText().toString().trim();
            ClipboardManager clipboard =
                    (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("UID", uid));
            Toast.makeText(getContext(), "Copied", Toast.LENGTH_SHORT).show();
        });

        setupBiometric();

        binding.qrCodeTv.setOnClickListener(v ->
                biometricPrompt.authenticate(promptInfo)
        );

        // 🔥 SECOND FIREBASE
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBRCHYvnSUWqQTW3ZCKJAQBDO6_e1Xh2Ss")
                .setApplicationId("1:654325754:android:fd4258dd800897e4055643")
                .setProjectId("multiscreenapp-27573")
                .build();

        FirebaseApp secondApp;
        try {
            secondApp = FirebaseApp.initializeApp(requireContext(), options, "secondApp");
        } catch (Exception e) {
            secondApp = FirebaseApp.getInstance("secondApp");
        }

        secondFirestore = FirebaseFirestore.getInstance(secondApp);

        binding.picImageToProfile.setOnClickListener(v ->
                imagePicker.launch("image/*")
        );

        fetchProfileImage();

        binding.logoutProfile.setOnClickListener(V -> logout());

    }

    // ================= BIOMETRIC =================
    private void setupBiometric() {

        Executor executor = ContextCompat.getMainExecutor(requireContext());

        biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            BiometricPrompt.AuthenticationResult result) {
                        showSecureQr();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(getContext(),
                                "Failed", Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify Identity")
                .setSubtitle("View QR")
                .setDeviceCredentialAllowed(true)
                .build();
    }

    private void showSecureQr() {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.qr_page, null);

        ImageView qr = view.findViewById(R.id.qrImage);

        try {
            String uid = binding.tvUniqueId.getText().toString();

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(
                    uid,
                    BarcodeFormat.QR_CODE,
                    400,
                    400
            );

            qr.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.setContentView(view);
        dialog.show();
    }

    private void openEditBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.edit_profile_bottom, null);

        EditText etAge = view.findViewById(R.id.etAge);
        EditText etDob = view.findViewById(R.id.etDob);
        EditText etGender = view.findViewById(R.id.etGender);
        EditText etBloodGroup = view.findViewById(R.id.etBloodGroup);
        EditText etContact = view.findViewById(R.id.etContact);
        EditText etEmergency = view.findViewById(R.id.etEmergency);
        Button btnSave = view.findViewById(R.id.btnSaveProfile);

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
                Toast.makeText(requireContext(),
                        "User not logged in",
                        Toast.LENGTH_SHORT).show();
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
                    .update(data)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(),
                                "Profile Updated",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(),
                                    e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void openViewBottomSheet() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(requireContext());

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_profile_bottom, null);

        TextView tvAllDetails =
                view.findViewById(R.id.tvAllDetails);

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

        dialog.setContentView(view);
        dialog.show();
    }

    // ================= IMAGE =================
    private String imageToBase64(Uri uri) {
        try {
            InputStream inputStream =
                    requireContext().getContentResolver().openInputStream(uri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);

            return Base64.encodeToString(
                    baos.toByteArray(),
                    Base64.DEFAULT
            );

        } catch (Exception e) {
            return null;
        }
    }

    private void uploadImageToSecondFirestore(Uri uri) {

        String base64 = imageToBase64(uri);
        if (base64 == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("profileImage", base64);

        secondFirestore.collection("users")
                .document(currentUserId)
                .set(map);
    }

    private void fetchProfileImage() {

        secondFirestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {

                    String base64 = doc.getString("profileImage");

                    if (base64 != null) {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap =
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        binding.profileImg.setImageBitmap(bitmap);
                    }
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }
}
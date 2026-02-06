package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Upload_Report extends AppCompatActivity {
    // UI
    EditText patientUniqueIdEditText, detailEditText;
    TextView selectedFileText, uidTextView;
    Button chooseBtn, uploadBtn;
    ImageButton btnBack;
    // Data
    Uri fileUri;
    // Firebase
    FirebaseAuth auth;
    FirebaseFirestore db;
    String role;
    String uploaderId;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_report);

        // UI
        patientUniqueIdEditText = findViewById(R.id.patientUniqueIdEditText);
        detailEditText = findViewById(R.id.detail_edit_text);
        selectedFileText = findViewById(R.id.selectedFileText);
        chooseBtn = findViewById(R.id.chooseBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        btnBack = findViewById(R.id.btn_back_view);
        uidTextView = findViewById(R.id.uidText);
        progressBar = findViewById(R.id.progressBar);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        uploaderId = auth.getUid();
        role = getIntent().getStringExtra("role");

        if (role == null) role = "";

        // Patient UI
        if ("patient".equalsIgnoreCase(role)) {
            patientUniqueIdEditText.setVisibility(View.GONE);
            uidTextView.setVisibility(View.GONE);
        }


        chooseBtn.setOnClickListener(v -> chooseFile());
        uploadBtn.setOnClickListener(v -> startUpload());
        btnBack.setOnClickListener(v -> finish());

        // ✅ FIX: button visible initially
        changeInProgress(false);
    }


    // ================= FILE PICKER =================
    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            selectedFileText.setText("Selected: " + getFileName(fileUri));
            Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
        }
    }

    // ================= UPLOAD START =================
    private void startUpload() {

        if (fileUri == null) {
            Toast.makeText(this, "Select a PDF file first", Toast.LENGTH_SHORT).show();
            return;
        }

        String feedback = detailEditText.getText().toString().trim();
        if (feedback.isEmpty()) {
            detailEditText.setError("Please write feedback");
            return;
        }

        changeInProgress(true);

        // PATIENT uploads for self
        if ("patient".equalsIgnoreCase(role)) {
            db.collection("User").document(uploaderId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            uploadFile(doc.getString("uniqueId"), feedback);
                        }
                    });

        } else {
            // DOCTOR / LAB uploads for patient
            String patientUniqueId = patientUniqueIdEditText.getText().toString().trim();

            if (patientUniqueId.isEmpty()) {
                patientUniqueIdEditText.setError("Enter patient UID");
                changeInProgress(false);
                return;
            }

            uploadFile(patientUniqueId, feedback);
        }
    }



    // ================= FIRESTORE UPLOAD =================
    private void uploadFile(String patientUniqueId, String feedback) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }

            byte[] bytes = buffer.toByteArray();
            inputStream.close();

            String base64File = Base64.encodeToString(bytes, Base64.DEFAULT);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("uploadedBy", uploaderId);
            dataMap.put("role", role);
            dataMap.put("fileName", getFileName(fileUri));
            dataMap.put("fileData", base64File);
            dataMap.put("feedback", feedback);
            dataMap.put("timestamp", FieldValue.serverTimestamp());

            db.collection("patients")
                    .document(patientUniqueId)
                    .collection("documents")
                    .add(dataMap)
                    .addOnSuccessListener(doc -> {
                        changeInProgress(false);
                        Toast.makeText(this, "Report uploaded successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        changeInProgress(false);
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            changeInProgress(false);
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }




    // ================= FILE NAME HELPER =================
    private String getFileName(Uri uri) {
        String result = "document.pdf";

        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void changeInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        uploadBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }
}
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_report);

        // ===== UI INIT =====
        patientUniqueIdEditText = findViewById(R.id.patientUniqueIdEditText);
        detailEditText = findViewById(R.id.detail_edit_text);
        selectedFileText = findViewById(R.id.selectedFileText);
        chooseBtn = findViewById(R.id.chooseBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        btnBack = findViewById(R.id.btn_back_view);
        uidTextView = findViewById(R.id.uidText);
        // ===== FIREBASE =====
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        uploaderId = auth.getUid();
        role = getIntent().getStringExtra("role");

        // Hide UID field if patient
        if ("patient".equals(role)) {
            patientUniqueIdEditText.setVisibility(View.GONE);
            uidTextView.setVisibility(View.GONE);
        }

        // ===== LISTENERS =====
        chooseBtn.setOnClickListener(v -> chooseFile());
        uploadBtn.setOnClickListener(v -> startUpload());
        btnBack.setOnClickListener(v -> finish());
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

        // ===== PATIENT =====
        if ("patient".equals(role)) {
            db.collection("User")
                    .document(uploaderId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String uniqueId = doc.getString("uniqueId");
                            uploadFile(uniqueId, feedback);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                    );

        } else {
            // ===== DOCTOR / LAB =====
            String patientUniqueId = patientUniqueIdEditText.getText().toString().trim();

            if (patientUniqueId.isEmpty()) {
                patientUniqueIdEditText.setError("Enter patient UID");
                return;
            }

            db.collection("User")
                    .whereEqualTo("uniqueId", patientUniqueId)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            uploadFile(patientUniqueId, feedback);
                        } else {
                            Toast.makeText(this, "Invalid Patient UID", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error fetching patient data", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    // ================= FIRESTORE UPLOAD =================
    private void uploadFile(String patientUniqueId, String feedback) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            String base64File = Base64.encodeToString(bytes, Base64.DEFAULT);
            String fileName = getFileName(fileUri);

            Map<String, Object> data = new HashMap<>();
            data.put("uploadedBy", uploaderId);
            data.put("role", role);
            data.put("fileName", fileName);
            data.put("fileData", base64File);
            data.put("feedback", feedback); // ✅ NEW FIELD
            data.put("timestamp", FieldValue.serverTimestamp());

            db.collection("patients")
                    .document(patientUniqueId)
                    .collection("documents")
                    .add(data)
                    .addOnSuccessListener(doc ->
                            Toast.makeText(this, "Report uploaded successfully", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        } catch (Exception e) {
            e.printStackTrace();
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
}
package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Bundle;
import android.view.View;
import com.example.finalyearprojecta.databinding.ActivityViewReportBinding;
import com.example.finalyearprojecta.portrait.PortraitCaptureActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;

public class View_Report extends AppCompatActivity {
    private ActivityViewReportBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String currentUserId;
    List<DocumentModel> documents;
    DocumentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ===== FIREBASE =====
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getUid();

        // ===== RECYCLER =====
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        documents = new ArrayList<>();
        adapter = new DocumentAdapter(documents);
        binding.recyclerView.setAdapter(adapter);

        binding.btnBackView.setOnClickListener(v -> finish());

        if (currentUserId == null) return;

        // ===== LOAD USER ROLE FROM FIRESTORE =====
        loadUserRoleAndData();

        // ===== DOCTOR / LAB SEARCH =====
        binding.fetchBtn.setOnClickListener(v -> {
            String patientUniqueId = binding.patientUniqueIdEditText.getText().toString().trim();

            if (patientUniqueId.isEmpty()) {
                binding.patientUniqueIdEditText.setError("Enter patient UID");
                return;
            }

            db.collection("User")
                    .whereEqualTo("uniqueId", patientUniqueId)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            fetchDocuments(patientUniqueId);
                        } else {
                            Toast.makeText(this, "Invalid Patient UID", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error fetching patient", Toast.LENGTH_SHORT).show()
                    );
        });

        binding.scanQrBtn.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan Patient QR");
            options.setBeepEnabled(true);

            // ✅ FORCE PORTRAIT
            options.setCaptureActivity(PortraitCaptureActivity.class);

            qrLauncher.launch(options);
        });
    }

    // ================= LOAD ROLE =================
    private void loadUserRoleAndData() {
        db.collection("User")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String role = doc.getString("role");
                    String uniqueId = doc.getString("uniqueId");

                    // ===== PATIENT =====
                    if ("patient".equalsIgnoreCase(role)) {

                        binding.patientUniqueIdEditText.setVisibility(View.GONE);
                        binding.fetchBtn.setVisibility(View.GONE);
                        binding.searchLayout.setVisibility(View.GONE);

                        if (uniqueId != null && !uniqueId.isEmpty()) {
                            fetchDocuments(uniqueId); // ✅ AUTO LOAD
                        } else {
                            Toast.makeText(this, "Unique ID missing", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // ===== DOCTOR / LAB =====
                        binding.patientUniqueIdEditText.setVisibility(View.VISIBLE);
                        binding.fetchBtn.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= FETCH DOCUMENTS =================
    private void fetchDocuments(String patientUniqueId) {
        binding.viewProgress.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);

        documents.clear();
        adapter.notifyDataSetChanged();

        db.collection("patients")
                .document(patientUniqueId)
                .collection("documents")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        binding.viewProgress.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String uid = doc.getString("uploadedBy");
                        String role = doc.getString("role");
                        Timestamp ts = doc.getTimestamp("timestamp");
                        String uploadDate = formatDate(ts != null ? ts.toDate() : null);

                        // Fetch uploader name from User collection
                        db.collection("User").document(uid)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = "Unknown";
                                    if (userDoc.exists()) {
                                        name = userDoc.getString("FullName"); // or "fullName" depending on your schema
                                    }

                                    // Add document to list with resolved name
                                    documents.add(new DocumentModel(
                                            doc.getString("fileName"),
                                            name,   // now shows actual name
                                            role,
                                            doc.getString("fileData"),
                                            doc.getString("feedback"),
                                            uploadDate
                                    ));

                                    adapter.notifyDataSetChanged();
                                    binding.viewProgress.setVisibility(View.GONE);
                                    binding.recyclerView.setVisibility(View.VISIBLE);
                                })
                                .addOnFailureListener(e -> {
                                    // fallback: show UID if name fetch fails
                                    documents.add(new DocumentModel(
                                            doc.getString("fileName"),
                                            uid,
                                            role,
                                            doc.getString("fileData"),
                                            doc.getString("feedback"),
                                            uploadDate
                                    ));
                                    adapter.notifyDataSetChanged();
                                    binding.viewProgress.setVisibility(View.GONE);
                                    binding.recyclerView.setVisibility(View.VISIBLE);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    binding.viewProgress.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    Toast.makeText(this,
                            "Failed to load documents: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat(
                "dd MMM yyyy\nhh:mm a",
                Locale.getDefault()
        ).format(date);
    }

    private final ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedUid = result.getContents().trim();
                    binding.patientUniqueIdEditText.setText(scannedUid);
                    fetchDocuments(scannedUid); // ✅ SAME LOGIC
                }
            });


}

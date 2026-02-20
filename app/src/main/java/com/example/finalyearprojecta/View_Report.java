package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.finalyearprojecta.databinding.ActivityViewReportBinding;
import com.example.finalyearprojecta.viewprofile.ProfileViewModel;
import com.example.finalyearprojecta.portrait.PortraitCaptureActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;

public class View_Report extends AppCompatActivity {

    private ActivityViewReportBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String currentUserId;
    List<DocumentModel> allDocuments;
    List<DocumentModel> filteredDocuments;
    DocumentAdapter adapter;

    // Dropdown selected values
    String selectedCategory = "All";
    String selectedSubCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getUid();

        // ===== RECYCLER =====
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allDocuments = new ArrayList<>();
        filteredDocuments = new ArrayList<>();
        adapter = new DocumentAdapter(filteredDocuments);
        binding.recyclerView.setAdapter(adapter);

        binding.btnBackView.setOnClickListener(v -> finish());
        if (currentUserId == null) return;

        // ===== CATEGORY DROPDOWN =====
        setupCategoryDropdown();

        // ===== LOAD USER ROLE =====
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
                            saveProfileView(patientUniqueId);
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
            options.setCaptureActivity(PortraitCaptureActivity.class);
            qrLauncher.launch(options);
        });

        binding.categoryDropdown.setOnClickListener(v -> binding.categoryDropdown.showDropDown());
        binding.subCategoryDropdown.setOnClickListener(v -> binding.subCategoryDropdown.showDropDown());
    }

    // ================= CATEGORY & SUBCATEGORY DROPDOWN =================
    private void setupCategoryDropdown() {
        AutoCompleteTextView categoryDropdown = binding.categoryDropdown;
        AutoCompleteTextView subCategoryDropdown = binding.subCategoryDropdown;

        String[] categories = {"All", "Lab Tests", "Imaging", "Heart Tests", "Neuro Tests", "Infection Tests", "Other Reports"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                categories
        );
        categoryDropdown.setAdapter(categoryAdapter);

        categoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories[position];

            String[] subCategories;
            switch (selectedCategory) {
                case "Lab Tests":
                    subCategories = new String[]{"All", "CBC", "LFT", "KFT", "Lipid Profile"};
                    break;
                case "Imaging":
                    subCategories = new String[]{"All", "X-ray", "MRI", "CT Scan", "Ultrasound"};
                    break;
                case "Heart Tests":
                    subCategories = new String[]{"All", "ECG", "Echo", "TMT"};
                    break;
                case "Neuro Tests":
                    subCategories = new String[]{"All", "EEG", "NCV", "EMG"};
                    break;
                case "Infection Tests":
                    subCategories = new String[]{"All", "COVID-19", "Dengue", "Malaria", "HIV"};
                    break;
                default:
                    subCategories = new String[]{"All", "General Report"};
            }

            ArrayAdapter<String> subAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    subCategories
            );

            subCategoryDropdown.setText("All");
            selectedSubCategory = "All";
            subCategoryDropdown.setAdapter(subAdapter);

            filterDocuments();
        });

        subCategoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedSubCategory = subCategoryDropdown.getAdapter().getItem(position).toString();
            filterDocuments();
        });
    }

    // ================= FILTER DOCUMENTS =================
    private void filterDocuments() {
        filteredDocuments.clear();

        for (DocumentModel doc : allDocuments) {
            boolean matchesCategory = selectedCategory.equals("All") || doc.getCategory().equals(selectedCategory);
            boolean matchesSubCategory = selectedSubCategory.equals("All") || doc.getSubCategory().equals(selectedSubCategory);

            if (matchesCategory && matchesSubCategory) {
                filteredDocuments.add(doc);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredDocuments.isEmpty()) {
            binding.emptyText.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyText.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
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

                    if ("patient".equalsIgnoreCase(role)) {
                        binding.patientUniqueIdEditText.setVisibility(View.GONE);
                        binding.fetchBtn.setVisibility(View.GONE);
                        binding.searchLayout.setVisibility(View.GONE);

                        if (uniqueId != null && !uniqueId.isEmpty()) {
                            fetchDocuments(uniqueId); // Auto load
                        } else {
                            Toast.makeText(this, "Unique ID missing", Toast.LENGTH_SHORT).show();
                        }

                    } else {
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

        allDocuments.clear();
        filteredDocuments.clear();
        adapter.notifyDataSetChanged();

        db.collection("patients")
                .document(patientUniqueId)
                .collection("documents")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        binding.viewProgress.setVisibility(View.GONE);
                        binding.emptyText.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String uid = doc.getString("uploadedBy");
                        String role = doc.getString("role");
                        Timestamp ts = doc.getTimestamp("timestamp");
                        String uploadDate = formatDate(ts != null ? ts.toDate() : null);

                        db.collection("User").document(uid)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = "Unknown";
                                    if (userDoc.exists()) {
                                        name = userDoc.getString("FullName");
                                    }

                                    String category = doc.contains("category") ? doc.getString("category") : "N/A";
                                    String subCategory = doc.contains("subCategory") ? doc.getString("subCategory") : "N/A";

                                    allDocuments.add(new DocumentModel(
                                            doc.getString("fileName"),
                                            name,
                                            role,
                                            doc.getString("fileData"),
                                            doc.getString("feedback"),
                                            uploadDate,
                                            category,
                                            subCategory
                                    ));

                                    filterDocuments(); // Apply filter immediately
                                    binding.viewProgress.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    String category = doc.contains("category") ? doc.getString("category") : "N/A";
                                    String subCategory = doc.contains("subCategory") ? doc.getString("subCategory") : "N/A";

                                    allDocuments.add(new DocumentModel(
                                            doc.getString("fileName"),
                                            uid,
                                            role,
                                            doc.getString("fileData"),
                                            doc.getString("feedback"),
                                            uploadDate,
                                            category,
                                            subCategory
                                    ));

                                    filterDocuments();
                                    binding.viewProgress.setVisibility(View.GONE);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    binding.viewProgress.setVisibility(View.GONE);
                    binding.emptyText.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Failed to load documents: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat("dd MMM yyyy\nhh:mm a", Locale.getDefault()).format(date);
    }

    private final ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedUid = result.getContents().trim();
                    binding.patientUniqueIdEditText.setText(scannedUid);
                    saveProfileView(scannedUid);
                    fetchDocuments(scannedUid);
                }
            });

    private void saveProfileView(String patientUniqueId) {
        if (currentUserId == null) return;

        db.collection("User")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) return;

                    String viewerName = userDoc.getString("FullName");
                    String viewerRole = userDoc.getString("role");
                    if (viewerName == null) viewerName = "Unknown";

                    String currentUserUniqueId = userDoc.getString("uniqueId");
                    if (patientUniqueId.equals(currentUserUniqueId)) return;

                    db.collection("patients")
                            .document(patientUniqueId)
                            .collection("profileViews")
                            .add(new ProfileViewModel(
                                    currentUserId,
                                    viewerName,
                                    viewerRole,
                                    Timestamp.now()
                            ));
                });
    }
}
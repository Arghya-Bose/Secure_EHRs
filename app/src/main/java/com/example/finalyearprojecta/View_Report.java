package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class View_Report extends AppCompatActivity {

    EditText patientUniqueIdEditText;
    ImageView fetchBtn;
    RecyclerView recyclerView;
    ImageButton backBtn;
    LinearLayout searchLayout;
    FirebaseAuth auth;
    FirebaseFirestore db;

    String currentUserId;

    List<DocumentModel> documents;
    DocumentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        // ===== UI =====
        patientUniqueIdEditText = findViewById(R.id.patientUniqueIdEditText);
        fetchBtn = findViewById(R.id.fetchBtn);
        recyclerView = findViewById(R.id.recyclerView);
        backBtn = findViewById(R.id.btn_back_view);
        searchLayout = findViewById(R.id.search_layout);

        // ===== FIREBASE =====
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getUid();

        // ===== RECYCLER =====
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        documents = new ArrayList<>();
        adapter = new DocumentAdapter(documents);
        recyclerView.setAdapter(adapter);

        backBtn.setOnClickListener(v -> finish());

        if (currentUserId == null) return;

        // ===== LOAD USER ROLE FROM FIRESTORE =====
        loadUserRoleAndData();

        // ===== DOCTOR / LAB SEARCH =====
        fetchBtn.setOnClickListener(v -> {
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
                            fetchDocuments(patientUniqueId);
                        } else {
                            Toast.makeText(this, "Invalid Patient UID", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error fetching patient", Toast.LENGTH_SHORT).show()
                    );
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

                        patientUniqueIdEditText.setVisibility(View.GONE);
                        fetchBtn.setVisibility(View.GONE);
                        searchLayout.setVisibility(View.GONE);

                        if (uniqueId != null && !uniqueId.isEmpty()) {
                            fetchDocuments(uniqueId); // ✅ AUTO LOAD
                        } else {
                            Toast.makeText(this, "Unique ID missing", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // ===== DOCTOR / LAB =====
                        patientUniqueIdEditText.setVisibility(View.VISIBLE);
                        fetchBtn.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= FETCH DOCUMENTS =================
    private void fetchDocuments(String patientUniqueId) {
        documents.clear();
        adapter.notifyDataSetChanged();

        db.collection("patients")
                .document(patientUniqueId)
                .collection("documents")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        documents.add(new DocumentModel(
                                doc.getString("fileName"),
                                doc.getString("uploadedBy"),
                                doc.getString("role"),
                                doc.getString("fileData"),
                                doc.getString("feedback")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load documents", Toast.LENGTH_SHORT).show()
                );
    }
}

package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class View_Report extends AppCompatActivity {

    EditText patientUniqueIdEditText;
    Button fetchBtn;
    RecyclerView recyclerView;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String role;
    String currentUserId;

    List<DocumentModel> documents;
    DocumentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        patientUniqueIdEditText = findViewById(R.id.patientUniqueIdEditText);
        fetchBtn = findViewById(R.id.fetchBtn);
        recyclerView = findViewById(R.id.recyclerView);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        currentUserId = auth.getUid();
        role = getIntent().getStringExtra("role");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        documents = new ArrayList<>();
        adapter = new DocumentAdapter(documents);
        recyclerView.setAdapter(adapter);

        // Patient sees own documents automatically
        if("patient".equals(role)){
            patientUniqueIdEditText.setVisibility(EditText.GONE);
            db.collection("User").document(currentUserId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String uniqueId = doc.getString("uniqueId");
                        fetchDocuments(uniqueId);
                    });
        }

        // Doctor/Lab fetch button
        fetchBtn.setOnClickListener(v -> {
            String patientUniqueId = patientUniqueIdEditText.getText().toString().trim();
            if(patientUniqueId.isEmpty()){
                patientUniqueIdEditText.setError("Enter patient Unique ID");
                return;
            }

            db.collection("User")
                    .whereEqualTo("uniqueId", patientUniqueId)
                    .get()
                    .addOnSuccessListener(query -> {
                        if(!query.isEmpty()){
                            String uniqueId = query.getDocuments().get(0).getString("uniqueId");
                            fetchDocuments(uniqueId);
                        } else {
                            Toast.makeText(this,"Invalid Unique ID",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,"Error fetching patient data",Toast.LENGTH_SHORT).show());
        });
    }

    private void fetchDocuments(String patientUniqueId){
        documents.clear();

        db.collection("patients")
                .document(patientUniqueId)
                .collection("documents")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {
                    for(QueryDocumentSnapshot doc: query){
                        String fileName = doc.getString("fileName");
                        String uploadedBy = doc.getString("uploadedBy");
                        String roleUploaded = doc.getString("role");
                        String fileData = doc.getString("fileData");

                        documents.add(new DocumentModel(fileName, uploadedBy, roleUploaded, fileData));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this,"Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }
}

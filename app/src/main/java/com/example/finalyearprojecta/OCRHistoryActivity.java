package com.example.finalyearprojecta;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.ocrimgtotext.OCRAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;

public class OCRHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<DocumentSnapshot> list;
    FirebaseFirestore db;
    OCRAdapter adapter;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrhistory);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        recyclerView = findViewById(R.id.recyclerView);
        btnBack = findViewById(R.id.btn_back_view);

        btnBack.setOnClickListener(V -> finish());

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        list = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        adapter = new OCRAdapter(this, list);
        recyclerView.setAdapter(adapter);

        fetchData();
    }

    private void fetchData() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this,
                    "User not logged in",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        db.collection("MedicalReports")
                .whereEqualTo("uid", currentUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();
                    list.addAll(queryDocumentSnapshots.getDocuments());
                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {
                        Toast.makeText(this,
                                "No reports found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
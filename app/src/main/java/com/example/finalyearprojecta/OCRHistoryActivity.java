package com.example.finalyearprojecta;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.ocrimgtotext.OCRAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;

public class OCRHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<DocumentSnapshot> list;
    FirebaseFirestore db;
    OCRAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrhistory);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        list = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        adapter = new OCRAdapter(this, list);
        recyclerView.setAdapter(adapter);

        fetchData();
    }

    private void fetchData() {

        db.collection("MedicalReports")
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();
                    list.addAll(queryDocumentSnapshots.getDocuments());
                    adapter.notifyDataSetChanged();
                });
    }
}
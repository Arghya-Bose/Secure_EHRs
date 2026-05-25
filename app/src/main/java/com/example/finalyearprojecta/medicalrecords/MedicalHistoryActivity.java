package com.example.finalyearprojecta.medicalrecords;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.finalyearprojecta.R;
import com.example.finalyearprojecta.databinding.ActivityMedicalHistoryBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalHistoryActivity extends AppCompatActivity {
    private ActivityMedicalHistoryBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore secondFirestore;
    String currentUserId;
    List<MedicalRecord> list;
    MedicalHistoryAdapter adapter;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicalHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        binding.recyclerMedicalHistory.setLayoutManager(new LinearLayoutManager(this));
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getUid();
        btnBack = findViewById(R.id.btn_back_view);

        btnBack.setOnClickListener(v -> finish());

        // 🔥 Initialize Second Firebase
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

        list = new ArrayList<>();
        adapter = new MedicalHistoryAdapter(list);
        binding.recyclerMedicalHistory.setAdapter(adapter);

        binding.btnAddHistory.setOnClickListener(v -> openAddBottomSheet());
        fetchMedicalHistory();
    }


    private void saveMedicalHistory(String type, String title, String year, String notes) {

        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("title", title);
        map.put("year", year);
        map.put("notes", notes);
        map.put("timestamp", System.currentTimeMillis());

        secondFirestore.collection("users")
                .document(currentUserId)
                .collection("medical_history")
                .add(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    fetchMedicalHistory();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // FETCH

    private void fetchMedicalHistory() {

        list.clear();

        secondFirestore.collection("users")
                .document(currentUserId)
                .collection("medical_history")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        String type = doc.getString("type");
                        String title = doc.getString("title");
                        String year = doc.getString("year");
                        String notes = doc.getString("notes");

                        list.add(new MedicalRecord(type, title, year, notes));
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // ADD BOTTOM SHEET

    private void openAddBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater()
                .inflate(R.layout.bottom_add_medical_history, null);

        EditText etType = view.findViewById(R.id.etType);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etYear = view.findViewById(R.id.etYear);
        EditText etNotes = view.findViewById(R.id.etNotes);
        Button btnSave = view.findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {

            String type = etType.getText().toString().trim();
            String title = etTitle.getText().toString().trim();
            String year = etYear.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (type.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            saveMedicalHistory(type, title, year, notes);
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }
}

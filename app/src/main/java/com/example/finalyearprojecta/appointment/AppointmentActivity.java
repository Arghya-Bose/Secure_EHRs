package com.example.finalyearprojecta.apoinment;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class AppointmentActivity extends AppCompatActivity {

    // UI
    LinearLayout bookLayout, viewLayout;
    Button btnBookTab, btnViewTab, btnBook, btnFetch;
    EditText doctorIdEt, patientIdEt, dateEt, timeEt;
    RecyclerView recyclerView;

    FirebaseFirestore secondDb;
    FirebaseAuth auth;
    FirebaseApp secondApp;

    List<AppointmentModel> list;
    AppointmentAdapter adapter;

    String role = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        // UI
        bookLayout = findViewById(R.id.bookLayout);
        viewLayout = findViewById(R.id.viewLayout);

        btnBookTab = findViewById(R.id.btnBookTab);
        btnViewTab = findViewById(R.id.btnViewTab);

        btnBook = findViewById(R.id.btnBook);
        btnFetch = findViewById(R.id.btnFetch);

        doctorIdEt = findViewById(R.id.doctorIdEt);
        patientIdEt = findViewById(R.id.patientIdEt);
        dateEt = findViewById(R.id.dateEt);
        timeEt = findViewById(R.id.timeEt);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new AppointmentAdapter(list, this);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();

        // 🔥 SECOND FIREBASE
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBRCHYvnSUWqQTW3ZCKJAQBDO6_e1Xh2Ss")
                .setApplicationId("1:654325754:android:fd4258dd800897e4055643")
                .setProjectId("multiscreenapp-27573")
                .build();

        if (FirebaseApp.getApps(this).size() < 2) {
            secondApp = FirebaseApp.initializeApp(this, options, "secondApp");
        } else {
            secondApp = FirebaseApp.getInstance("secondApp");
        }

        secondDb = FirebaseFirestore.getInstance(secondApp);

        role = getIntent().getStringExtra("role");

        // Tabs
        btnBookTab.setOnClickListener(v -> {
            bookLayout.setVisibility(View.VISIBLE);
            viewLayout.setVisibility(View.GONE);
        });

        btnViewTab.setOnClickListener(v -> {
            bookLayout.setVisibility(View.GONE);
            viewLayout.setVisibility(View.VISIBLE);
        });

        // Book
        btnBook.setOnClickListener(v -> bookAppointment());

        // Fetch
        btnFetch.setOnClickListener(v -> loadAppointments());
    }

    // ================= BOOK =================
    private void bookAppointment() {

        String doctorId = doctorIdEt.getText().toString().trim();
        String patientId = patientIdEt.getText().toString().trim();
        String date = dateEt.getText().toString().trim();
        String time = timeEt.getText().toString().trim();

        if (doctorId.isEmpty() || patientId.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("doctorId", doctorId);
        map.put("patientId", patientId);
        map.put("date", date);
        map.put("time", time);
        map.put("status", "pending");
        map.put("timestamp", FieldValue.serverTimestamp());

        secondDb.collection("patients")
                .document(patientId)
                .collection("appointments")
                .add(map)
                .addOnSuccessListener(d ->
                        Toast.makeText(this, "Booked", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= LOAD =================
    private void loadAppointments() {

        list.clear();

        String uid = patientIdEt.getText().toString().trim();

        secondDb.collection("patients")
                .document(uid)
                .collection("appointments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(q -> {

                    for (QueryDocumentSnapshot doc : q) {

                        list.add(new AppointmentModel(
                                doc.getId(),
                                doc.getString("doctorId"),
                                doc.getString("date"),
                                doc.getString("time"),
                                doc.getString("status"),
                                uid
                        ));
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
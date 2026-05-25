package com.example.finalyearprojecta.notes;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.finalyearprojecta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity {

    EditText etDiagnosis, etTreatment, etDate;
    Button btnSave;
    ImageButton btnBack;
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        etDiagnosis = findViewById(R.id.etDiagnosis);
        etTreatment = findViewById(R.id.etTreatment);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btn_back_view);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveNote());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, y, m, d) -> etDate.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void saveNote() {
        String diagnosis = etDiagnosis.getText().toString().trim();
        String treatment = etTreatment.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (diagnosis.isEmpty() || treatment.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUid = auth.getCurrentUser().getUid();

        Map<String, Object> note = new HashMap<>();
        note.put("diagnosis", diagnosis);
        note.put("treatment", treatment);
        note.put("followUpDate", date);
        note.put("timestamp", System.currentTimeMillis());
        note.put("uid", currentUid);   // important fix

        db.collection("DoctorNotes")
                .add(note)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Note Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
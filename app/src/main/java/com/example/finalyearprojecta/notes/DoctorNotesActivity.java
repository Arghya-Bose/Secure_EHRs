package com.example.finalyearprojecta.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.finalyearprojecta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class DoctorNotesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fab;
    List<Note> noteList;
    ImageView btnBack;
    NoteAdapter adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_notes);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        recyclerView = findViewById(R.id.recyclerViewNotes);
        fab = findViewById(R.id.fabAddNote);
        btnBack = findViewById(R.id.btn_back_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteList = new ArrayList<>();
        adapter = new NoteAdapter(this, noteList);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        loadNotes();

        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, AddNoteActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(); // refresh after returning
    }

    private void loadNotes() {

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("DoctorNotes")
                .whereEqualTo("uid", currentUid)
                .get()
                .addOnSuccessListener(query -> {

                    noteList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Note note = doc.toObject(Note.class);
                        noteList.add(note);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
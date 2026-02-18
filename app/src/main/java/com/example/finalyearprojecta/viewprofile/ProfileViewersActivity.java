package com.example.finalyearprojecta.viewprofile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.finalyearprojecta.databinding.ActivityProfileViewersBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.*;

public class ProfileViewersActivity extends AppCompatActivity {
    ActivityProfileViewersBinding binding;
    FirebaseFirestore db;
    List<ProfileViewModel> viewersList;
    ViewerAdapter adapter;
    String patientUniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileViewersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        patientUniqueId = getIntent().getStringExtra("uniqueId");
        viewersList = new ArrayList<>();
        adapter = new ViewerAdapter(viewersList);

        binding.recyclerViewViewers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewViewers.setAdapter(adapter);
        binding.btnBackView.setOnClickListener(v -> finish());

        loadProfileViewers();
        loadViewerCount();
    }

    private void loadProfileViewers() {

        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("patients")
                .document(patientUniqueId)
                .collection("profileViews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {

                    viewersList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        ProfileViewModel model = doc.toObject(ProfileViewModel.class);
                        viewersList.add(model);
                    }

                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load viewers", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadViewerCount() {

        db.collection("patients")
                .document(patientUniqueId)
                .collection("profileViews")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    int count = querySnapshot.size();

                    binding.tvViewerCount.setText("Total Views: " + count);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load count",
                                Toast.LENGTH_SHORT).show()
                );
    }

}

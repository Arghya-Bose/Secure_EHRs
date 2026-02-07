package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile_Activity extends AppCompatActivity {

    TextView tvUniqueId, nameTv, emailTv;
    Button btnCopyUid;

    FirebaseAuth auth;
    FirebaseFirestore db;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUniqueId = findViewById(R.id.tvUniqueId);
        btnCopyUid = findViewById(R.id.btnCopyUid);
        nameTv = findViewById(R.id.tvName);
        emailTv = findViewById(R.id.tvEmail);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getUid();

        // Fetch UID from Firestore
        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if(doc.exists()){
                        String uniqueId = doc.getString("uniqueId");
                        tvUniqueId.setText(uniqueId);
                        String name = doc.getString("FullName");
                        String email = doc.getString("UserEmail");
                        emailTv.setText(email);
                        nameTv.setText(name);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,"Failed to fetch UID",Toast.LENGTH_SHORT).show());
        // Copy UID to clipboard
        btnCopyUid.setOnClickListener(v -> {
            String uid = tvUniqueId.getText().toString().trim();
            if(!uid.isEmpty()){
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Patient UID", uid);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this,"UID copied to clipboard",Toast.LENGTH_SHORT).show();
            }
        });
    }
}

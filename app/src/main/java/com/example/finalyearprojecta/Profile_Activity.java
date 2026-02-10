package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile_Activity extends AppCompatActivity {

    TextView tvUniqueId, nameTv, emailTv;
    Button btnCopyUid;
    ImageView qrImage;

    FirebaseAuth auth;
    FirebaseFirestore db;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // UI
        tvUniqueId = findViewById(R.id.tvUniqueId);
        btnCopyUid = findViewById(R.id.btnCopyUid);
        nameTv = findViewById(R.id.tvName);
        emailTv = findViewById(R.id.tvEmail);
        qrImage = findViewById(R.id.qrImage);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getUid();

        if (currentUserId == null) return;

        // 🔥 Fetch user data
        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String uniqueId = doc.getString("uniqueId");
                        String name = doc.getString("FullName");
                        String email = doc.getString("UserEmail");

                        tvUniqueId.setText(uniqueId);
                        nameTv.setText(name);
                        emailTv.setText(email);

                        // ✅ Generate QR AFTER UID is available
                        if (uniqueId != null && !uniqueId.isEmpty()) {
                            generateQr(uniqueId);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                );

        // 📋 Copy UID
        btnCopyUid.setOnClickListener(v -> {
            String uid = tvUniqueId.getText().toString().trim();
            if (!uid.isEmpty()) {
                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Patient UID", uid);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "UID copied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= QR GENERATION =================
    private void generateQr(String uid) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(
                    uid,
                    BarcodeFormat.QR_CODE,
                    400,
                    400
            );
            qrImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

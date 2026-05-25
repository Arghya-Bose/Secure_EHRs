package com.example.finalyearprojecta.prescription;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.DocumentAdapter;
import com.example.finalyearprojecta.DocumentModel;
import com.example.finalyearprojecta.R;
import com.example.finalyearprojecta.utils.AESUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

public class Prescription extends AppCompatActivity {

    ScrollView uploadLayout;
    LinearLayout viewLayout;
    Button btnUploadTab, btnViewTab, uploadBtn, fetchBtn, chooseFileBtn;
    EditText patientUid, notes, searchUid;
    ImageButton backView;
    TextView fileText;
    RecyclerView recyclerView;
    Uri fileUri;
    FirebaseAuth auth;
    FirebaseFirestore secondDb;
    FirebaseApp secondApp;

    List<DocumentModel> list;
    DocumentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        // UI init
        uploadLayout = findViewById(R.id.uploadLayout);
        viewLayout = findViewById(R.id.viewLayout);

        btnUploadTab = findViewById(R.id.btnUploadTab);
        btnViewTab = findViewById(R.id.btnViewTab);

        uploadBtn = findViewById(R.id.uploadBtn);
        fetchBtn = findViewById(R.id.fetchBtn);
        chooseFileBtn = findViewById(R.id.chooseFileBtn);
        backView = findViewById(R.id.btn_back_view);

        patientUid = findViewById(R.id.patientUid);
        notes = findViewById(R.id.notes);
        searchUid = findViewById(R.id.searchUid);
        fileText = findViewById(R.id.fileText);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new DocumentAdapter(list);
        recyclerView.setAdapter(adapter);

        backView.setOnClickListener(v -> finish());

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

        // Tabs
        btnUploadTab.setOnClickListener(v -> {
            uploadLayout.setVisibility(View.VISIBLE);
            viewLayout.setVisibility(View.GONE);
        });

        btnViewTab.setOnClickListener(v -> {
            uploadLayout.setVisibility(View.GONE);
            viewLayout.setVisibility(View.VISIBLE);
        });

        // File picker
        chooseFileBtn.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            startActivityForResult(i, 101);
        });

        uploadBtn.setOnClickListener(v -> uploadPrescription());
        fetchBtn.setOnClickListener(v -> fetchPrescriptions());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            fileText.setText("Selected: " + getFileName(fileUri));
        }
    }

    private void uploadPrescription() {

        String uid = patientUid.getText().toString().trim();
        String note = notes.getText().toString().trim();

        if (uid.isEmpty() || fileUri == null) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream is = getContentResolver().openInputStream(fileUri);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int n;

            while ((n = is.read(data)) != -1) {
                buffer.write(data, 0, n);
            }

            byte[] bytes = buffer.toByteArray();
            is.close();

            String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);

            String fileName = getFileName(fileUri);

            // 🔥 Detect type
            String fileType;

            if (fileName.toLowerCase().endsWith(".pdf")) {
                fileType = "pdf";
            } else if (
                    fileName.toLowerCase().endsWith(".jpg") ||
                            fileName.toLowerCase().endsWith(".jpeg") ||
                            fileName.toLowerCase().endsWith(".png")
            ) {
                fileType = "image";
            } else {
                fileType = "unknown";
            }

            Map<String, Object> map = new HashMap<>();
            map.put("fileName", AESUtils.encrypt(fileName));
            map.put("fileData", AESUtils.encrypt(base64));
            map.put("notes", AESUtils.encrypt(note));
            map.put("fileType", fileType);
            map.put("uploadedBy", auth.getUid());
            map.put("timestamp", FieldValue.serverTimestamp());

            secondDb.collection("patients")
                    .document(uid)
                    .collection("prescriptions")
                    .add(map)
                    .addOnSuccessListener(d ->
                            Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                    );

        } catch (Exception e) {
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPrescriptions() {

        String uid = searchUid.getText().toString().trim();

        if (uid.isEmpty()) {
            searchUid.setError("Enter UID");
            return;
        }

        list.clear();

        secondDb.collection("patients")
                .document(uid)
                .collection("prescriptions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(q -> {

                    for (QueryDocumentSnapshot doc : q) {

                        try {
                            String fileName = AESUtils.decrypt(doc.getString("fileName"));
                            String fileData = AESUtils.decrypt(doc.getString("fileData"));
                            String notes = AESUtils.decrypt(doc.getString("notes"));

                            String fileType = doc.getString("fileType");

                            list.add(new DocumentModel(
                                    fileName,
                                    "",
                                    "",
                                    fileData,
                                    notes,
                                    "",
                                    fileType,   // 🔥 IMPORTANT
                                    "",
                                    ""
                            ));

                        } catch (Exception ignored) {}
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private String getFileName(Uri uri) {

        String result = "file";

        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        return result;
    }
}
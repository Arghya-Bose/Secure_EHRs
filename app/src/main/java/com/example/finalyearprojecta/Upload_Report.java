package com.example.finalyearprojecta;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Upload_Report extends AppCompatActivity {

    EditText patientUniqueIdEditText;
    Button chooseBtn, uploadBtn;
    Uri fileUri;

    FirebaseAuth auth;
    FirebaseFirestore db;
    String role;
    String uploaderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_report);

        patientUniqueIdEditText = findViewById(R.id.patientUniqueIdEditText);
        chooseBtn = findViewById(R.id.chooseBtn);
        uploadBtn = findViewById(R.id.uploadBtn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        uploaderId = auth.getUid();
        role = getIntent().getStringExtra("role");

        // Patient uploads only for self → hide input
        if("patient".equals(role)){
            patientUniqueIdEditText.setVisibility(View.GONE);
        }

        chooseBtn.setOnClickListener(v -> chooseFile());
        uploadBtn.setOnClickListener(v -> startUpload());
    }

    private void chooseFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf"); // you can allow images too
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==101 && resultCode==RESULT_OK && data!=null){
            fileUri = data.getData();
            Toast.makeText(this,"File selected",Toast.LENGTH_SHORT).show();
        }
    }

    private void startUpload(){
        if(fileUri==null){
            Toast.makeText(this,"Select a file first",Toast.LENGTH_SHORT).show();
            return;
        }

        if("patient".equals(role)){
            // Patient uploads for self
            db.collection("User").document(uploaderId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String uniqueId = doc.getString("uniqueId");
                        uploadFile(uniqueId);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,"Error fetching user data",Toast.LENGTH_SHORT).show());

        } else {
            // Doctor/Lab → get patient uniqueId from input
            String patientUniqueId = patientUniqueIdEditText.getText().toString().trim();
            if(patientUniqueId.isEmpty()){
                patientUniqueIdEditText.setError("Enter patient Unique ID");
                return;
            }

            db.collection("User")
                    .whereEqualTo("uniqueId", patientUniqueId)
                    .get()
                    .addOnSuccessListener(query -> {
                        if(!query.isEmpty()){
                            String uniqueId = query.getDocuments().get(0).getString("uniqueId");
                            uploadFile(uniqueId); // ✅ always use uniqueId
                        } else {
                            Toast.makeText(this,"Invalid Unique ID",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,"Error fetching patient data",Toast.LENGTH_SHORT).show());
        }
    }

    private void uploadFile(String patientUniqueId){
        try{
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            String base64File = Base64.encodeToString(bytes, Base64.DEFAULT);

            Map<String,Object> data = new HashMap<>();
            data.put("uploadedBy", uploaderId);
            data.put("role", role);
            data.put("fileName","medical_report.pdf");
            data.put("fileData",base64File);
            data.put("timestamp", FieldValue.serverTimestamp());

            db.collection("patients")
                    .document(patientUniqueId) // ✅ Use uniqueId as patient doc
                    .collection("documents")
                    .add(data)
                    .addOnSuccessListener(doc -> Toast.makeText(this,"Upload successful",Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this,"Upload failed: "+e.getMessage(),Toast.LENGTH_SHORT).show());

        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this,"Error reading file",Toast.LENGTH_SHORT).show();
        }
    }
}

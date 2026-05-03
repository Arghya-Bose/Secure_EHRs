package com.example.finalyearprojecta.fregment;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.finalyearprojecta.MainActivity;
import com.example.finalyearprojecta.OCRHistoryActivity;
import com.example.finalyearprojecta.R;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Record extends Fragment{

    View view;
    LinearLayout start_scanning;
    MaterialButton ocrRecord;
    ImageView saveBtn;
    TextView all_scaned_text;

    FirebaseFirestore db;

    ArrayList<DocumentSnapshot> list = new ArrayList<>();
    int currentIndex = 0;

    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_record,
                container, false);

        start_scanning = view.findViewById(R.id.start_scanning);
        saveBtn = view.findViewById(R.id.saveBtn);
        ocrRecord = view.findViewById(R.id.view_scaned_records);

        all_scaned_text = view.findViewById(R.id.all_scaned_text);

        saveBtn.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();

        initLauncher();

        start_scanning.setOnClickListener(v -> chooseImage());

        saveBtn.setOnClickListener(v -> saveFirestore());
        ocrRecord.setOnClickListener(v -> {
            Intent intent =
                    new Intent(getActivity(),
                            OCRHistoryActivity.class); // target activity

            startActivity(intent);

            fetchData();


        });

        return view;
    }

    //====================== PICK ======================

    private void chooseImage() {

        String[] items = {"Camera", "Gallery"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Select")
                .setItems(items, (dialog, which) -> {

                    if (which == 0) {

                        Intent i = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(i);

                    } else {

                        Intent i = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        galleryLauncher.launch(i);
                    }

                }).show();
    }

    private void initLauncher() {

        cameraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode()
                                    == Activity.RESULT_OK) {

                                Bitmap bitmap =
                                        (Bitmap) result.getData()
                                                .getExtras().get("data");

                                runOCR(bitmap);
                            }
                        });

        galleryLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode()
                                    == Activity.RESULT_OK) {

                                try {

                                    Uri uri = result.getData().getData();

                                    Bitmap bitmap =
                                            MediaStore.Images.Media.getBitmap(
                                                    requireActivity()
                                                            .getContentResolver(),
                                                    uri);

                                    runOCR(bitmap);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }

    //====================== OCR ======================

    // inside runOCR()

    private void runOCR(Bitmap bitmap) {

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(text -> {

                    String result = text.getText().trim();

                    all_scaned_text.setText(result);

                    // if text found show save button
                    if (!result.isEmpty()) {
                        saveBtn.setVisibility(View.VISIBLE);
                    } else {
                        saveBtn.setVisibility(View.GONE);
                    }

                })
                .addOnFailureListener(e -> {
                    saveBtn.setVisibility(View.GONE);
                });
    }

    //====================== EXTRACT ======================

    private String findName(String txt) {

        String[] lines = txt.split("\n");

        for (String line : lines) {

            if (line.matches("^[A-Za-z ]+$")
                    && line.length() > 5) {
                return line;
            }
        }

        return "Unknown";
    }

    private String findHB(String txt) {

        Pattern p = Pattern.compile(
                "(?i)(hb|hemoglobin)[: ]*([0-9.]+)");

        Matcher m = p.matcher(txt);

        if (m.find()) return m.group(2) + " g/dL";

        return "Not Found";
    }

    //====================== SAVE FIRESTORE ======================

    private void saveFirestore() {

        String text = all_scaned_text.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(requireContext(),
                    "Nothing to Save",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        HashMap<String, Object> map = new HashMap<>();
        map.put("ocrtext", text);
        map.put("time", FieldValue.serverTimestamp());
        map.put("uid", currentUid);   // important fix

        db.collection("MedicalReports")
                .add(map)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(requireContext(),
                            "Saved Successfully",
                            Toast.LENGTH_SHORT).show();

                    all_scaned_text.setText("");
                    saveBtn.setVisibility(View.GONE);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Save Failed",
                                Toast.LENGTH_SHORT).show());
    }

    //====================== FETCH ONE BY ONE ======================

    private void fetchData() {

        String currentUid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        db.collection("MedicalReports")
                .whereEqualTo("uid", currentUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();
                    list.addAll(queryDocumentSnapshots.getDocuments());

                    if (list.size() > 0) {
                        showData(0);
                    }
                });

        saveBtn.setOnLongClickListener(v -> {

            currentIndex++;

            if (currentIndex >= list.size())
                currentIndex = 0;

            showData(currentIndex);

            return true;
        });
    }

    private void showData(int pos) {

        DocumentSnapshot doc = list.get(pos);

        all_scaned_text.setText(
                doc.getString("ocrtext"));
    }
}
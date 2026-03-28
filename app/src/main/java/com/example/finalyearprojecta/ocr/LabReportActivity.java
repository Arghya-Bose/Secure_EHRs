package com.example.finalyearprojecta.ocr;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.finalyearprojecta.R;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabReportActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Button btnUpload;

    private LinearLayout resultContainer;
    private static final int PICK_IMAGE = 100;

    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_report);

        btnUpload = findViewById(R.id.btnUpload);
        resultContainer = findViewById(R.id.resultContainer);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btn_back_view);

        btnBack.setOnClickListener(v -> finish());

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            progressBar.setVisibility(View.VISIBLE);
            btnUpload.setEnabled(false);

            Uri imageUri = data.getData();

            try {
                InputImage image = InputImage.fromFilePath(this, imageUri);

                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        .process(image)
                        .addOnSuccessListener(text -> {
                            processReport(text.getText());

                            progressBar.setVisibility(View.GONE);
                            btnUpload.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            btnUpload.setEnabled(true);
                        });

            } catch (Exception e) {
                progressBar.setVisibility(View.GONE);
                btnUpload.setEnabled(true);
                e.printStackTrace();
            }
        }
    }

    // ================= SMART PARSER =================

    private void processReport(String text) {

        resultContainer.removeAllViews();

        Map<String, TestItem> testMap = new HashMap<>();

// ================= CBC =================
        testMap.put("Hemoglobin", new TestItem(13, 17));
        testMap.put("RBC", new TestItem(4.5, 5.9));
        testMap.put("WBC", new TestItem(4000, 11000));
        testMap.put("Platelet", new TestItem(150000, 450000));
        testMap.put("Hematocrit", new TestItem(40, 50));
        testMap.put("MCV", new TestItem(80, 100));
        testMap.put("MCH", new TestItem(27, 33));
        testMap.put("MCHC", new TestItem(32, 36));
        testMap.put("ESR", new TestItem(0, 20));

// ================= Diabetes =================
        testMap.put("Glucose", new TestItem(70, 140));
        testMap.put("FBS", new TestItem(70, 99));
        testMap.put("PPBS", new TestItem(70, 140));
        testMap.put("HbA1c", new TestItem(4, 5.6));
        testMap.put("Random Blood Sugar", new TestItem(70, 140));

// ================= Lipid Profile =================
        testMap.put("Cholesterol", new TestItem(125, 200));
        testMap.put("Total Cholesterol", new TestItem(125, 200));
        testMap.put("HDL", new TestItem(40, 60));
        testMap.put("LDL", new TestItem(0, 100));
        testMap.put("Triglycerides", new TestItem(0, 150));
        testMap.put("VLDL", new TestItem(5, 40));

// ================= Kidney Function =================
        testMap.put("Creatinine", new TestItem(0.6, 1.3));
        testMap.put("Urea", new TestItem(15, 40));
        testMap.put("Uric Acid", new TestItem(3.5, 7.2));
        testMap.put("BUN", new TestItem(7, 20));

// ================= Liver Function =================
        testMap.put("SGPT", new TestItem(7, 56));
        testMap.put("ALT", new TestItem(7, 56));
        testMap.put("SGOT", new TestItem(10, 40));
        testMap.put("AST", new TestItem(10, 40));
        testMap.put("Bilirubin", new TestItem(0.1, 1.2));
        testMap.put("ALP", new TestItem(44, 147));
        testMap.put("Albumin", new TestItem(3.5, 5.0));

// ================= Thyroid =================
        testMap.put("TSH", new TestItem(0.4, 4.0));
        testMap.put("T3", new TestItem(80, 200));
        testMap.put("T4", new TestItem(5, 12));

// ================= Electrolytes =================
        testMap.put("Sodium", new TestItem(135, 145));
        testMap.put("Potassium", new TestItem(3.5, 5.0));
        testMap.put("Chloride", new TestItem(96, 106));
        testMap.put("Calcium", new TestItem(8.6, 10.2));
        testMap.put("Magnesium", new TestItem(1.7, 2.2));

// ================= Vitamins =================
        testMap.put("Vitamin D", new TestItem(20, 50));
        testMap.put("Vitamin B12", new TestItem(200, 900));

// ================= Cardiac =================
        testMap.put("Troponin", new TestItem(0, 0.04));
        testMap.put("CK-MB", new TestItem(0, 5));
        testMap.put("CRP", new TestItem(0, 3));
        testMap.put("D-Dimer", new TestItem(0, 0.5));

        Map<String, TestInfo> infoMap = new HashMap<>();

// ================= CBC =================
        infoMap.put("Hemoglobin", new TestInfo(
                "Hemoglobin (Hb)",
                "Protein in red blood cells that carries oxygen throughout the body."
        ));

        infoMap.put("RBC", new TestInfo(
                "Red Blood Cell Count",
                "Number of red blood cells responsible for carrying oxygen."
        ));

        infoMap.put("WBC", new TestInfo(
                "White Blood Cell Count",
                "Cells that fight infections and protect the body."
        ));

        infoMap.put("Platelet", new TestInfo(
                "Platelet Count",
                "Helps in blood clotting and stopping bleeding."
        ));

        infoMap.put("Hematocrit", new TestInfo(
                "Hematocrit (HCT)",
                "Percentage of red blood cells in total blood volume."
        ));

        infoMap.put("MCV", new TestInfo(
                "Mean Corpuscular Volume",
                "Average size of red blood cells."
        ));

        infoMap.put("MCH", new TestInfo(
                "Mean Corpuscular Hemoglobin",
                "Average amount of hemoglobin in each red blood cell."
        ));

        infoMap.put("MCHC", new TestInfo(
                "Mean Corpuscular Hemoglobin Concentration",
                "Average concentration of hemoglobin in red blood cells."
        ));

        infoMap.put("ESR", new TestInfo(
                "Erythrocyte Sedimentation Rate",
                "Indicates inflammation in the body."
        ));

// ================= Diabetes =================
        infoMap.put("Glucose", new TestInfo(
                "Blood Glucose",
                "Measures sugar level in blood."
        ));

        infoMap.put("FBS", new TestInfo(
                "Fasting Blood Sugar",
                "Blood sugar level after fasting for 8–10 hours."
        ));

        infoMap.put("PPBS", new TestInfo(
                "Postprandial Blood Sugar",
                "Blood sugar level measured after meals."
        ));

        infoMap.put("HbA1c", new TestInfo(
                "Glycated Hemoglobin",
                "Shows average blood sugar level over last 3 months."
        ));

        infoMap.put("Random Blood Sugar", new TestInfo(
                "Random Blood Sugar",
                "Blood sugar measured at any time of the day."
        ));

// ================= Lipid Profile =================
        infoMap.put("Cholesterol", new TestInfo(
                "Total Cholesterol",
                "Total amount of cholesterol in blood."
        ));

        infoMap.put("Total Cholesterol", new TestInfo(
                "Total Cholesterol",
                "Measures overall cholesterol level."
        ));

        infoMap.put("HDL", new TestInfo(
                "High Density Lipoprotein",
                "Good cholesterol that protects against heart disease."
        ));

        infoMap.put("LDL", new TestInfo(
                "Low Density Lipoprotein",
                "Bad cholesterol that increases heart disease risk."
        ));

        infoMap.put("Triglycerides", new TestInfo(
                "Triglycerides",
                "Type of fat in blood used for energy."
        ));

        infoMap.put("VLDL", new TestInfo(
                "Very Low Density Lipoprotein",
                "Carries triglycerides in blood."
        ));

// ================= Kidney Function =================
        infoMap.put("Creatinine", new TestInfo(
                "Serum Creatinine",
                "Waste product that indicates kidney function."
        ));

        infoMap.put("Urea", new TestInfo(
                "Blood Urea",
                "Waste product formed from protein breakdown."
        ));

        infoMap.put("Uric Acid", new TestInfo(
                "Uric Acid",
                "High levels may cause gout or kidney stones."
        ));

        infoMap.put("BUN", new TestInfo(
                "Blood Urea Nitrogen",
                "Measures nitrogen level in blood to assess kidney health."
        ));

// ================= Liver Function =================
        infoMap.put("SGPT", new TestInfo(
                "Serum Glutamic Pyruvic Transaminase",
                "Liver enzyme. High value may indicate liver damage."
        ));

        infoMap.put("ALT", new TestInfo(
                "Alanine Aminotransferase",
                "Liver enzyme used to detect liver injury."
        ));

        infoMap.put("SGOT", new TestInfo(
                "Serum Glutamic Oxaloacetic Transaminase",
                "Enzyme found in liver and heart."
        ));

        infoMap.put("AST", new TestInfo(
                "Aspartate Aminotransferase",
                "Enzyme that may indicate liver or heart damage."
        ));

        infoMap.put("Bilirubin", new TestInfo(
                "Bilirubin",
                "Yellow pigment produced during red blood cell breakdown."
        ));

        infoMap.put("ALP", new TestInfo(
                "Alkaline Phosphatase",
                "Enzyme related to liver and bone health."
        ));

        infoMap.put("Albumin", new TestInfo(
                "Serum Albumin",
                "Protein made by liver that maintains fluid balance."
        ));

// ================= Thyroid =================
        infoMap.put("TSH", new TestInfo(
                "Thyroid Stimulating Hormone",
                "Controls thyroid gland activity."
        ));

        infoMap.put("T3", new TestInfo(
                "Triiodothyronine",
                "Thyroid hormone regulating metabolism."
        ));

        infoMap.put("T4", new TestInfo(
                "Thyroxine",
                "Main hormone produced by thyroid gland."
        ));

// ================= Electrolytes =================
        infoMap.put("Sodium", new TestInfo(
                "Sodium",
                "Electrolyte controlling fluid balance and nerves."
        ));

        infoMap.put("Potassium", new TestInfo(
                "Potassium",
                "Important for heart and muscle function."
        ));

        infoMap.put("Chloride", new TestInfo(
                "Chloride",
                "Maintains fluid and acid-base balance."
        ));

        infoMap.put("Calcium", new TestInfo(
                "Calcium",
                "Essential for bones, muscles, and nerves."
        ));

        infoMap.put("Magnesium", new TestInfo(
                "Magnesium",
                "Supports muscle, nerve, and heart function."
        ));

// ================= Vitamins =================
        infoMap.put("Vitamin D", new TestInfo(
                "Vitamin D",
                "Helps absorb calcium and supports immunity."
        ));

        infoMap.put("Vitamin B12", new TestInfo(
                "Vitamin B12",
                "Important for nerve function and red blood cell formation."
        ));

// ================= Cardiac =================
        infoMap.put("Troponin", new TestInfo(
                "Cardiac Troponin",
                "Protein released during heart muscle damage."
        ));

        infoMap.put("CK-MB", new TestInfo(
                "Creatine Kinase-MB",
                "Enzyme indicating heart muscle injury."
        ));

        infoMap.put("CRP", new TestInfo(
                "C-Reactive Protein",
                "Marker of inflammation in the body."
        ));

        infoMap.put("D-Dimer", new TestInfo(
                "D-Dimer",
                "Protein fragment indicating blood clot formation."
        ));

        for (String key : testMap.keySet()) {
            double value = extractValue(text, key);
            if (value != -1) {
                showCard(key, value, testMap.get(key), infoMap.get(key));
            }
        }
    }

    // ================= REGEX EXTRACTION =================

    private double extractValue(String text, String keyword) {
        // (?i) -> case-insensitive
        // [^\\d]* -> skip non-numeric characters between keyword and value
        Pattern pattern = Pattern.compile(
                "(?i)" + keyword + "[^\\d]*?(\\d+\\.?\\d*)",
                Pattern.MULTILINE
        );

        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return -1;
    }

    // ================= UI CARD DISPLAY =================

    private void showCard(String name, double value, TestItem range, TestInfo info) {

        CardView card = new CardView(this);
        card.setRadius(25);
        card.setCardElevation(10);
        card.setUseCompatPadding(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(40, 40, 40, 40);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setPadding(0, 0, 0, 10);

        TextView fullFormText = new TextView(this);
        TextView meaningText = new TextView(this);

        if (info != null) {
            fullFormText.setText(info.fullForm);
            fullFormText.setTextSize(14);
            fullFormText.setTextColor(Color.DKGRAY);

            meaningText.setText(info.meaning);
            meaningText.setTextSize(13);
            meaningText.setTextColor(Color.GRAY);
            meaningText.setVisibility(TextView.GONE);
        }

        TextView valueText = new TextView(this);
        valueText.setTextSize(18);

        TextView statusBadge = new TextView(this);
        statusBadge.setTextSize(14);
        statusBadge.setPadding(20, 10, 20, 10);
        statusBadge.setTextColor(Color.WHITE);

        double borderlineMargin = (range.max - range.min) * 0.05;
        String status;

        if (value < range.min) {
            status = "LOW";
            card.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
            statusBadge.setBackgroundColor(Color.parseColor("#1565C0"));
            valueText.setTextColor(Color.parseColor("#1565C0"));
        }
        else if (value > range.max) {
            status = "HIGH";
            card.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            statusBadge.setBackgroundColor(Color.parseColor("#C62828"));
            valueText.setTextColor(Color.parseColor("#C62828"));
        }
        else if (value <= range.min + borderlineMargin ||
                value >= range.max - borderlineMargin) {
            status = "BORDERLINE";
            card.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
            statusBadge.setBackgroundColor(Color.parseColor("#EF6C00"));
            valueText.setTextColor(Color.parseColor("#EF6C00"));
        }
        else {
            status = "NORMAL";
            card.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            statusBadge.setBackgroundColor(Color.parseColor("#2E7D32"));
            valueText.setTextColor(Color.parseColor("#2E7D32"));
        }

        valueText.setText("Level: " + value);
        statusBadge.setText(status);

        // Toggle meaning on title click
        card.setOnClickListener(v -> {
            if (meaningText.getVisibility() == TextView.GONE) {
                meaningText.setVisibility(TextView.VISIBLE);
            } else {
                meaningText.setVisibility(TextView.GONE);
            }
        });

        // Add views ONLY ONCE (correct order)
        layout.addView(title);
        layout.addView(fullFormText);
        layout.addView(valueText);
        layout.addView(statusBadge);
        layout.addView(meaningText);

        card.addView(layout);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, 0, 0, 32);
        card.setLayoutParams(params);

        resultContainer.addView(card);
    }

    // ================= RANGE MODEL =================

    static class TestItem {
        double min, max;

        TestItem(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }

    static class TestInfo {
        String fullForm;
        String meaning;

        TestInfo(String fullForm, String meaning) {
            this.fullForm = fullForm;
            this.meaning = meaning;
        }
    }
}

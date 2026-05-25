package com.example.finalyearprojecta.fregment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.finalyearprojecta.*;
import com.example.finalyearprojecta.medicalrecords.MedicalHistoryActivity;
import com.example.finalyearprojecta.notes.DoctorNotesActivity;
import com.example.finalyearprojecta.notification.RiminderNotification;
import com.example.finalyearprojecta.ocr.LabReportActivity;
import com.example.finalyearprojecta.prescription.Prescription;
import com.example.finalyearprojecta.viewprofile.ProfileViewersActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {
    DrawerLayout drawerLayout;
    ImageView cProfile, bg_icon, leftProfile;
    ImageButton btnMenu;
    TextView logoutBtn, nameText, viewProfile, aboutBtn, NameText, doctorNotes, reminder;
    Button scanImageBtn;
    LinearLayout option1, option2, option3, option4, medicalHistory, reportAnalysis, prescMng, mHistory;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    String currentUserId;
    View view;
    private static final int PERMISSION_REQUEST_CODE = 101;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        // 🔐 Firebase
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (fAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
            return view;
        }

        currentUserId = fAuth.getUid();

        requireActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        // INIT VIEWS
        drawerLayout = view.findViewById(R.id.drawerLayout);
        btnMenu = view.findViewById(R.id.btn_menu);
        scanImageBtn = view.findViewById(R.id.scan_btn);
        logoutBtn = view.findViewById(R.id.btn_loout);
        cProfile = view.findViewById(R.id.profile);
        bg_icon = view.findViewById(R.id.bgIcon);
        nameText = view.findViewById(R.id.name_text);
        NameText = view.findViewById(R.id.tvName_home);
        leftProfile = view.findViewById(R.id.left_profile);
        medicalHistory = view.findViewById(R.id.medical_history_view);
        viewProfile = view.findViewById(R.id.profile_viewers);
        prescMng = view.findViewById(R.id.Prescs_mng);
        reportAnalysis = view.findViewById(R.id.report_analysis_view);
        mHistory = view.findViewById(R.id.historyCard);
        aboutBtn = view.findViewById(R.id.btn_about);
        doctorNotes = view.findViewById(R.id.doctor_notes);
        reminder = view.findViewById(R.id.m_reminder);


        bg_icon.setVisibility(View.GONE);

        // Get role from Activity
        String role = getActivity().getIntent().getStringExtra("role");

        //Firestore
        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("FullName");
                        String uniqueId = documentSnapshot.getString("uniqueId");

                        nameText.setText(name);

                        viewProfile.setOnClickListener(v -> {
                            Intent intent = new Intent(getContext(), ProfileViewersActivity.class);
                            intent.putExtra("uniqueId", uniqueId);
                            startActivity(intent);
                        });
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load name", Toast.LENGTH_SHORT).show()
                );

        // 🔹 Role UI
        if ("lab".equalsIgnoreCase(role)) {
            bg_icon.setVisibility(View.VISIBLE);
            mHistory.setVisibility(View.GONE);
        } else if ("doctor".equalsIgnoreCase(role)) {
            mHistory.setVisibility(View.GONE);
            doctorNotes.setVisibility(View.VISIBLE);
        } else {
            bg_icon.setVisibility(View.GONE);
        }

        // Four Cards
        option1 = view.findViewById(R.id.option_layout_1);
        option2 = view.findViewById(R.id.option_layout_2);
        option3 = view.findViewById(R.id.option_layout_3);
        option4 = view.findViewById(R.id.option_layout_4);

        // Click Listeners
        btnMenu.setOnClickListener(this);
        scanImageBtn.setOnClickListener(this);
        aboutBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        cProfile.setOnClickListener(this);
        leftProfile.setOnClickListener(this);

        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);
        medicalHistory.setOnClickListener(this);
        reportAnalysis.setOnClickListener(this);
        prescMng.setOnClickListener(this);
        mHistory.setOnClickListener(this);
        doctorNotes.setOnClickListener(this);
        reminder.setOnClickListener(this);

        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        NameText.setText(doc.getString("FullName"));
                    }
                });


        requestAllPermissions();
        return view;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        String role = getActivity().getIntent().getStringExtra("role");

        if (id == R.id.btn_menu) {
            drawerLayout.openDrawer(GravityCompat.START);

        }else if (id == R.id.doctor_notes) {
            startActivity(new Intent(getContext(), DoctorNotesActivity.class));

        }else if (id == R.id.m_reminder) {
        startActivity(new Intent(getContext(), RiminderNotification.class));

        } else if (id == R.id.scan_btn) {
            startActivity(new Intent(getContext(), LabReportActivity.class));

        } else if (id == R.id.btn_about) {
            BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
            View v1 = getLayoutInflater().inflate(R.layout.bottom_sheet_about, null);
            dialog.setContentView(v1);
            dialog.show();

        } else if (id == R.id.profile || id == R.id.left_profile) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, new Profile())
                    .commit();
            BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.settings);


        } else if (id == R.id.medical_history_view) {
            startActivity(new Intent(getContext(), MedicalHistoryActivity.class));

        } else if (id == R.id.report_analysis_view) {
            startActivity(new Intent(getContext(), LabReportActivity.class));

        } else if (id == R.id.Prescs_mng) {
            startActivity(new Intent(getContext(), ChatActivity.class));

        } else if (id == R.id.option_layout_1) {

            if ("lab".equalsIgnoreCase(role)) {
                Toast.makeText(getContext(),
                        "Access denied", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(getContext(), View_Report.class));
            }

        } else if (id == R.id.option_layout_2) {
            Intent intent = new Intent(getContext(), Upload_Report.class);
            intent.putExtra("role", role);
            startActivity(intent);

        } else if (id == R.id.option_layout_3) {
            Intent intent = new Intent(getContext(), Prescription.class);
            intent.putExtra("role", role);
            startActivity(intent);

        } else if (id == R.id.option_layout_4) {
            startActivity(new Intent(getContext(), RiminderNotification.class));

        }else if (id == R.id.historyCard) {
            Intent intent = new Intent(getContext(), MedicalHistoryActivity.class);
            intent.putExtra("role", role);
            startActivity(intent);

        } else if (id == R.id.btn_loout) {
            logout();
        }
    }

    //LOGOUT
    private void logout() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void requestAllPermissions() {

        List<String> permissionsList = new ArrayList<>();

        // Camera
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.CAMERA);
        }

        // Notification (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsList.isEmpty()) {
            requestPermissions(
                    permissionsList.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            Toast.makeText(requireContext(),
                    "Permissions processed",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
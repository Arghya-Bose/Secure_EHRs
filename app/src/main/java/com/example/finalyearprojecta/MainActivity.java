package com.example.finalyearprojecta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // UI Elements
    DrawerLayout drawerLayout;
    ImageView  cProfile;
    ImageButton btnMenu;
    TextView navDashboard, logoutBtn;
    Button scanImageBtn, aboutBtn;
    LinearLayout option1, option2, option3, option4;
    // Firebase
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔐 CHECK IF USER IS LOGGED IN
        fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() == null) {
            // User not logged in, redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        // INIT VIEWS
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btn_menu);
        navDashboard = findViewById(R.id.dashboard);
        scanImageBtn = findViewById(R.id.scan_btn);
        aboutBtn = findViewById(R.id.btn_about);
        logoutBtn = findViewById(R.id.btn_loout);
        cProfile = findViewById(R.id.profile);

        //Four Card
        option1 = findViewById(R.id.option_layout_1);
        option2 = findViewById(R.id.option_layout_2);
        option3 = findViewById(R.id.option_layout_3);
        option4 = findViewById(R.id.option_layout_4);

        // SET CLICK LISTENERS
        btnMenu.setOnClickListener(this);
        navDashboard.setOnClickListener(this);
        scanImageBtn.setOnClickListener(this);
        aboutBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        cProfile.setOnClickListener(this);

        // set click on four card
        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_menu) {
            drawerLayout.openDrawer(GravityCompat.START);

        } else if (id == R.id.dashboard) {
            Toast.makeText(this, "Dashboard clicked", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);

        } else if (id == R.id.scan_btn) {
            startActivity(new Intent(this, Scan_Image.class));

        } else if (id == R.id.btn_about) {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.bottom_sheet_about, null);
            dialog.setContentView(view);
            dialog.show();

        }else if (id ==R.id.profile) {
            startActivity(new Intent(this, Profile_Activity.class));

        }else if(id == R.id.option_layout_1) {
            startActivity(new Intent(this, View_Report.class));

        }else if(id ==R.id.option_layout_2){
            Intent intent = new Intent(MainActivity.this, Upload_Report.class);
            intent.putExtra("role", getIntent().getStringExtra("role"));
            startActivity(intent);

        } else if (id == R.id.btn_loout) {
            logout(v);
        }
    }

    // 🚪 LOGOUT METHOD
    public void logout(View view) {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Go to LoginActivity and clear back stack
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish MainActivity
        finish();
    }
}
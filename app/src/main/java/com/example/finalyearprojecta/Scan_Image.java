package com.example.finalyearprojecta;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Scan_Image extends AppCompatActivity {

    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan_image);

        back = findViewById(R.id.btn_back_view);

        back.setOnClickListener(V-> finish());



    }
}
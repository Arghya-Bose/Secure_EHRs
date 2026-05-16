package com.example.finalyearprojecta.splash;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.finalyearprojecta.LoginActivity;
import com.example.finalyearprojecta.MainActivity;
import com.example.finalyearprojecta.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView statusText;
    private Button retryButton;

    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);
        retryButton = findViewById(R.id.retryButton);

        fAuth = FirebaseAuth.getInstance();

        retryButton.setOnClickListener(v -> checkInternetAndProceed());

        checkInternetAndProceed();
    }

    private void checkInternetAndProceed() {

        if (isInternetAvailable()) {

            progressBar.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);
            statusText.setText("Establishing secure link...");

            progressBar.setProgress(0);

            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

            Runnable runnable = new Runnable() {
                int progress = 0;

                @Override
                public void run() {
                    progress += 10;
                    progressBar.setProgress(progress);

                    if (progress < 100) {
                        handler.postDelayed(this, 100);
                    } else {

                        if (fAuth.getCurrentUser() != null) {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        }

                        finish();
                    }
                }
            };

            handler.post(runnable);

        } else {

            progressBar.setVisibility(View.GONE);
            retryButton.setVisibility(View.VISIBLE);
            statusText.setText("No Internet Connection");
        }
    }

    private boolean isInternetAvailable() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities cap = cm.getNetworkCapabilities(network);

        return cap != null &&
                cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED); // 🔥 important
    }
}
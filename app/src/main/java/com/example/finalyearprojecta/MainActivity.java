package com.example.finalyearprojecta;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.finalyearprojecta.fregment.Assistant;
import com.example.finalyearprojecta.fregment.HomeFragment;
import com.example.finalyearprojecta.fregment.Profile;
import com.example.finalyearprojecta.fregment.Record;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setItemHorizontalTranslationEnabled(false);

        // Default fragment (Home)
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                selectedFragment = new HomeFragment();

            } else if (itemId == R.id.records) {
                selectedFragment = new Record();

            } else if (itemId == R.id.assistant) {
                selectedFragment = new Assistant();

            } else if (itemId == R.id.settings) {
                selectedFragment = new Profile();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment =
                getSupportFragmentManager()
                        .findFragmentById(R.id.frame_container);

        if (currentFragment instanceof HomeFragment) {

            new AlertDialog.Builder(this)
                    .setTitle("Exit App")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();

        } else {

            getSupportFragmentManager().popBackStack();

            bottomNav.setSelectedItemId(R.id.home);
        }
    }
}
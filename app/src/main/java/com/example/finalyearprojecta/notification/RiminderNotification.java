package com.example.finalyearprojecta.notification;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.finalyearprojecta.R;

import java.util.Calendar;

public class RiminderNotification extends AppCompatActivity {

    EditText etMedicine;
    Button btnPickTime, btnSetReminder;
    TextView tvTime;

    int selectedHour = -1;
    int selectedMinute = -1;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Toast.makeText(this,
                                    "Notification Permission Granted",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "Notification Permission Denied",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riminder_notification);

        etMedicine = findViewById(R.id.etMedicine);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        tvTime = findViewById(R.id.tvTime);

        askNotificationPermission();

        btnPickTime.setOnClickListener(v -> openTimePicker());
        btnSetReminder.setOnClickListener(v -> setReminder());
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            }
        }
    }

    private void openTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;

                    tvTime.setText("Reminder Time: " + hourOfDay + ":" + minute);
                },
                12,
                0,
                false
        );

        dialog.show();
    }

    private void setReminder() {
        String medicine = etMedicine.getText().toString().trim();

        if (medicine.isEmpty()) {
            Toast.makeText(this,
                    "Enter medicine name",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedHour == -1) {
            Toast.makeText(this,
                    "Select reminder time",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("medicine", medicine);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

            Toast.makeText(this,
                    "Reminder Set Successfully",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
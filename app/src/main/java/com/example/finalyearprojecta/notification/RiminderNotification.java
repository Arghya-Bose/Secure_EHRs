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
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.R;

import java.util.ArrayList;
import java.util.Calendar;

public class RiminderNotification extends AppCompatActivity {

    EditText etMedicine;
    Button btnPickTime, btnSetReminder;
    ImageButton back;
    TextView tvTime;

    int selectedHour = -1;
    int selectedMinute = -1;

    RecyclerView recyclerView;
    ArrayList<ReminderModel> reminderList;
    ReminderAdapter adapter;

    // ✅ NEW: persistence helper
    ReminderPreferenceHelper prefHelper;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> Toast.makeText(this,
                            isGranted ? "Notification Permission Granted"
                                    : "Notification Permission Denied",
                            Toast.LENGTH_SHORT).show());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riminder_notification);

        etMedicine     = findViewById(R.id.etMedicine);
        btnPickTime    = findViewById(R.id.btnPickTime);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        tvTime         = findViewById(R.id.tvTime);
        back           = findViewById(R.id.btn_back_view);
        recyclerView   = findViewById(R.id.reminderRecycler);

        // ✅ NEW: initialise helper and load saved reminders
        prefHelper   = new ReminderPreferenceHelper(this);
        reminderList = prefHelper.load();   // restores previous list on every open

        // Pass prefHelper to adapter so it can save after delete
        adapter = new ReminderAdapter(this, reminderList, prefHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        askNotificationPermission();

        btnPickTime.setOnClickListener(v -> openTimePicker());
        btnSetReminder.setOnClickListener(v -> setReminder());

        btnSetReminder.setOnLongClickListener(v -> {
            Intent testIntent = new Intent(this, ReminderReceiver.class);
            testIntent.putExtra("medicine", "Paracetamol");
            sendBroadcast(testIntent);
            Toast.makeText(this, "Test notification sent", Toast.LENGTH_SHORT).show();
            return true;
        });

        back.setOnClickListener(v -> finish());
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void openTimePicker() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedHour   = hourOfDay;
                    selectedMinute = minute;
                    tvTime.setText(String.format("Reminder Time: %02d:%02d", hourOfDay, minute));
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void setReminder() {

        String medicine = etMedicine.getText().toString().trim();

        if (medicine.isEmpty()) {
            Toast.makeText(this, "Enter medicine name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedHour == -1) {
            Toast.makeText(this, "Select reminder time", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE,      selectedMinute);
        calendar.set(Calendar.SECOND,      0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If selected time already passed today, schedule for tomorrow same time
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        int requestCode = (int) System.currentTimeMillis();

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("medicine", medicine);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            Toast.makeText(this, "Enable exact alarm permission", Toast.LENGTH_SHORT).show();
            return;
        }

        alarmManager.setAlarmClock(
                new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent),
                pendingIntent);

        String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
        reminderList.add(new ReminderModel(medicine, formattedTime, requestCode));

        // ✅ NEW: save to SharedPreferences immediately after adding
        prefHelper.save(reminderList);

        adapter.notifyItemInserted(reminderList.size() - 1);

        etMedicine.setText("");
        tvTime.setText("No Time Selected");
        selectedHour   = -1;
        selectedMinute = -1;

        Toast.makeText(this, "Reminder Set Successfully", Toast.LENGTH_SHORT).show();
    }
}
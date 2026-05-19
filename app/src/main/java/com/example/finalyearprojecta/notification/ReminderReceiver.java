package com.example.finalyearprojecta.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.finalyearprojecta.R;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "medicine_channel";
    private static final String CHANNEL_NAME = "Medicine Reminder";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("ReminderReceiver", "Receiver Triggered");

        String medicineName = intent.getStringExtra("medicine");

        if (medicineName == null || medicineName.isEmpty()) {
            medicineName = "Your Medicine";
        }

        createNotificationChannel(context);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logoprogress)
                        .setContentTitle("Medicine Reminder")
                        .setContentText("Time to take: " + medicineName)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.d("ReminderReceiver",
                        "Notification permission not granted");

                return;
            }
        }

        NotificationManagerCompat.from(context)
                .notify(
                        (int) System.currentTimeMillis(),
                        builder.build()
                );

        Log.d("ReminderReceiver", "Notification Sent Successfully");
    }

    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Channel for medicine reminder notifications"
            );

            NotificationManager manager =
                    context.getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
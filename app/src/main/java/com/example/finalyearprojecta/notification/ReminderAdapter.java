package com.example.finalyearprojecta.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.R;

import java.util.ArrayList;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    Context context;
    ArrayList<ReminderModel> list;
    ReminderPreferenceHelper prefHelper; // ✅ NEW: needed to save after delete

    public ReminderAdapter(Context context,
                           ArrayList<ReminderModel> list,
                           ReminderPreferenceHelper prefHelper) {
        this.context    = context;
        this.list       = list;
        this.prefHelper = prefHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ReminderModel model = list.get(position);
        holder.medName.setText(model.getMedicineName());
        holder.time.setText(model.getTime());

        holder.delete.setOnClickListener(v -> {

            // Always resolve live position — captured `position` goes stale
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_ID) return;

            ReminderModel current = list.get(currentPosition);

            // Cancel the alarm
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    current.getRequestCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );
            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }

            // Remove from list and update UI
            list.remove(currentPosition);
            notifyItemRemoved(currentPosition);

            // NEW: persist the updated list so delete also survives app restart
            prefHelper.save(list);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView medName, time;
        ImageView delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            medName = itemView.findViewById(R.id.tvMedicineName);
            time    = itemView.findViewById(R.id.tvReminderTime);
            delete  = itemView.findViewById(R.id.deleteReminder);
        }
    }
}
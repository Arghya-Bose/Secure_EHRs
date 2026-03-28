package com.example.finalyearprojecta.apoinment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    List<AppointmentModel> list;
    Context context;

    FirebaseFirestore db;

    public AppointmentAdapter(List<AppointmentModel> list, Context context) {
        this.list = list;
        this.context = context;
        db = FirebaseFirestore.getInstance(FirebaseApp.getInstance("secondApp"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        AppointmentModel m = list.get(pos);

        h.txt.setText(
                "Doctor: " + m.getDoctorId() +
                        "\nDate: " + m.getDate() +
                        "\nTime: " + m.getTime() +
                        "\nStatus: " + m.getStatus()
        );

        h.btnApprove.setOnClickListener(v -> update(m, "approved"));
        h.btnReject.setOnClickListener(v -> update(m, "rejected"));
        h.btnCancel.setOnClickListener(v -> update(m, "cancelled"));
    }

    private void update(AppointmentModel m, String status) {
        db.collection("patients")
                .document(m.getPatientId())
                .collection("appointments")
                .document(m.getId())
                .update("status", status)
                .addOnSuccessListener(d ->
                        Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt;
        Button btnApprove, btnReject, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txt);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}

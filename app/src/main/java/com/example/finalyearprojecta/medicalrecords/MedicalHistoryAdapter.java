package com.example.finalyearprojecta.medicalrecords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.R;

import java.util.List;

public class MedicalHistoryAdapter
        extends RecyclerView.Adapter<MedicalHistoryAdapter.ViewHolder> {

    List<MedicalRecord> list;

    public MedicalHistoryAdapter(List<MedicalRecord> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medical_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MedicalRecord record = list.get(position);

        holder.tvType.setText(record.getType());
        holder.tvTitle.setText(record.getTitle());
        holder.tvYear.setText(record.getYear());
        holder.tvNotes.setText(record.getNotes());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvType, tvTitle, tvYear, tvNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tvType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvNotes = itemView.findViewById(R.id.tvNotes);
        }
    }
}

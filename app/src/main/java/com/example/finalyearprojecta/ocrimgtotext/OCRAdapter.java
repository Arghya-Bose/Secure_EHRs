package com.example.finalyearprojecta.ocrimgtotext;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.finalyearprojecta.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OCRAdapter extends RecyclerView.Adapter<OCRAdapter.MyViewHolder> {

    Context context;
    ArrayList<DocumentSnapshot> list;

    public OCRAdapter(Context context,
                      ArrayList<DocumentSnapshot> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_ocr, parent, false);

        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull MyViewHolder holder, int position) {

        DocumentSnapshot doc = list.get(position);

        String text = doc.getString("ocrtext");

        holder.title.setText("Report " + (position + 1));

        // OCR Preview
        if (text.length() > 70)
            holder.preview.setText(text.substring(0, 70) + "...");
        else
            holder.preview.setText(text);

        // SHOW DATE TIME
        Timestamp timestamp = doc.getTimestamp("time");

        if (timestamp != null) {

            Date date = timestamp.toDate();

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault());

            holder.timeTv.setText(sdf.format(date));

        } else {
            holder.timeTv.setText("No Time");
        }

        holder.itemView.setOnClickListener(v -> {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(context);

            builder.setTitle("Full OCR Text");
            builder.setMessage(text);
            builder.setPositiveButton("Close", null);
            builder.show();
        });
    }

    private String formatText(String text) {

        return text.replace(".", ".\n")
                .replace(",", ", ")
                .replace("\n\n", "\n");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, preview, timeTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title   = itemView.findViewById(R.id.titleTv);
            preview = itemView.findViewById(R.id.previewTv);
            timeTv  = itemView.findViewById(R.id.timeTv);
        }
    }
}

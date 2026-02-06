package com.example.finalyearprojecta;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private final List<DocumentModel> documents;

    public DocumentAdapter(List<DocumentModel> documents) {
        this.documents = documents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DocumentModel doc = documents.get(position);

        // ===== FILE NAME =====
        holder.fileNameText.setText(doc.getFileName());

        // ===== UPLOADED BY =====
        holder.uploadedByText.setText(
                "Uploaded by: " + doc.getUploadedBy() + " (" + doc.getRole() + ")"
        );

        // ===== FEEDBACK =====
        holder.feedbackText.setText(
                doc.getFeedback() == null || doc.getFeedback().isEmpty()
                        ? "No detail founded"
                        : "Detail: " + doc.getFeedback()
        );

        // ===== DATE =====
        holder.dateText.setText(
                doc.getUploadDate() == null
                        ? "Date not available"
                        : doc.getUploadDate()
        );

        // ===== OPEN PDF =====
        holder.itemView.setOnClickListener(v ->
                openPdf(v.getContext(), doc)
        );
    }

    private void openPdf(Context context, DocumentModel doc) {
        try {
            byte[] pdfBytes = Base64.decode(doc.getFileData(), Base64.DEFAULT);

            File file = new File(context.getCacheDir(), doc.getFileName());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(pdfBytes);
            fos.close();

            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView fileNameText, uploadedByText, feedbackText, dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.fileNameText);
            uploadedByText = itemView.findViewById(R.id.uploadedByText);
            feedbackText = itemView.findViewById(R.id.feedbackText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}

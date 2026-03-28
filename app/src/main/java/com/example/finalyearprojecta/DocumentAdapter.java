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
import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private final List<DocumentModel> documents;     // current displayed list
    private final List<DocumentModel> allDocuments;  // full list for filtering

    public DocumentAdapter(List<DocumentModel> documents) {
        this.documents = documents;
        this.allDocuments = new ArrayList<>(documents);
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
                        ? "No detail found"
                        : "Detail: " + doc.getFeedback()
        );

        // ===== DATE =====
        holder.dateText.setText(
                doc.getUploadDate() == null
                        ? "Date not available"
                        : doc.getUploadDate()
        );

        // ===== CATEGORY & SUBCATEGORY =====
//        holder.categoryText.setText("Category: " + doc.getCategory());
//        holder.subCategoryText.setText("Sub Category: "+doc.getSubCategory());

        // ===== OPEN PDF =====
        holder.itemView.setOnClickListener(v -> openFile(v.getContext(), doc));
    }

    private void openFile(Context context, DocumentModel doc) {
        try {
            byte[] bytes = Base64.decode(doc.getFileData(), Base64.DEFAULT);

            // 🔥 Ensure correct filename
            String fileName = doc.getFileName();
            if (fileName == null || fileName.isEmpty()) {
                fileName = "file";
            }

            File file = new File(context.getCacheDir(), fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();

            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 🔥 GET FILE TYPE
            String fileType = doc.getFileType();

            // 🔥 Fallback if null (old data)
            if (fileType == null) {
                if (fileName.toLowerCase().endsWith(".pdf")) {
                    fileType = "pdf";
                } else {
                    fileType = "image";
                }
            }

            // 🔥 SET CORRECT MIME TYPE
            if (fileType.equals("pdf")) {
                intent.setDataAndType(uri, "application/pdf");
            } else if (fileType.equals("image")) {
                intent.setDataAndType(uri, "image/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }

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

    // ================== FILTERING ==================
    public void filter(String category, String subCategory) {
        documents.clear();

        for (DocumentModel doc : allDocuments) {
            boolean matchCategory = category == null || category.isEmpty() || category.equals("All") || doc.getCategory().equals(category);
            boolean matchSubCategory = subCategory == null || subCategory.isEmpty() || subCategory.equals("All") || doc.getSubCategory().equals(subCategory);

            if (matchCategory && matchSubCategory) {
                documents.add(doc);
            }
        }

        notifyDataSetChanged();
    }

    // ================== VIEW HOLDER ==================
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView fileNameText, uploadedByText, feedbackText, dateText, categoryText, subCategoryText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.fileNameText);
            uploadedByText = itemView.findViewById(R.id.uploadedByText);
            feedbackText = itemView.findViewById(R.id.feedbackText);
            dateText = itemView.findViewById(R.id.dateText);
//            categoryText = itemView.findViewById(R.id.categoryText);
//            subCategoryText = itemView.findViewById(R.id.subCategoryText);
        }
    }
}

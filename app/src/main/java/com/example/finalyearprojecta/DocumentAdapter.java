package com.example.finalyearprojecta;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    List<DocumentModel> documents;

    public DocumentAdapter(List<DocumentModel> documents){
        this.documents = documents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        DocumentModel doc = documents.get(position);
        holder.fileNameText.setText(doc.getFileName());
        holder.uploadedByText.setText("Uploaded By: "+doc.getUploadedBy()+" ("+doc.getRole()+")");

        // On click → you can decode Base64 or download
        holder.itemView.setOnClickListener(v -> {
            String base64 = doc.getFileData();
            if(base64 != null){
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                Toast.makeText(v.getContext(), "File bytes: "+data.length, Toast.LENGTH_SHORT).show();
                // Optionally save file locally or open PDF viewer
            }
        });
    }

    @Override
    public int getItemCount(){ return documents.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView fileNameText, uploadedByText;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            fileNameText = itemView.findViewById(R.id.fileNameText);
            uploadedByText = itemView.findViewById(R.id.uploadedByText);
        }
    }
}

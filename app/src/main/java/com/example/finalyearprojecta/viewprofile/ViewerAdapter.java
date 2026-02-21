package com.example.finalyearprojecta.viewprofile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojecta.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ViewerAdapter extends RecyclerView.Adapter<ViewerAdapter.ViewHolder> {

    List<ProfileViewModel> list;

    public ViewerAdapter(List<ProfileViewModel> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ProfileViewModel model = list.get(position);

        holder.name.setText(model.getViewerName());
        holder.role.setText(model.getViewerRole());
        //holder.contact.setText("Emergency contact: "+model.getvContact());

        if (model.getTimestamp() != null) {

            long timeInMillis = model.getTimestamp().toDate().getTime();
            long now = System.currentTimeMillis();

            CharSequence timeAgo = android.text.format.DateUtils.getRelativeTimeSpanString(
                    timeInMillis,
                    now,
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
            );

            holder.time.setText(timeAgo);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, role, time, contact;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvViewerName);
            role = itemView.findViewById(R.id.tvViewerRole);
            time = itemView.findViewById(R.id.tvViewerTime);
            //contact = itemView.findViewById(R.id.tvViewerContact);
        }
    }
}

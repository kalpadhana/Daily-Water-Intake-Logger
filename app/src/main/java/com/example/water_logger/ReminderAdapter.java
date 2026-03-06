package com.example.water_logger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders;

    public ReminderAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.tvReminderName.setText(reminder.getName());
        holder.tvReminderTime.setText(reminder.getTime());
        holder.switchReminder.setChecked(reminder.isEnabled());

        holder.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminder.setEnabled(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvReminderName;
        TextView tvReminderTime;
        ImageView ivEditReminder;
        SwitchCompat switchReminder;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReminderName = itemView.findViewById(R.id.tvReminderName);
            tvReminderTime = itemView.findViewById(R.id.tvReminderTime);
            ivEditReminder = itemView.findViewById(R.id.ivEditReminder);
            switchReminder = itemView.findViewById(R.id.switchReminder);
        }
    }
}
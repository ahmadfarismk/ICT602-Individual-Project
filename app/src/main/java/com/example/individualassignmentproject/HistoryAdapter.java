package com.example.individualassignmentproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Custom adapter for displaying bill records in the History ListView.
 * Each item shows: month initial icon, month name, and final cost.
 */
public class HistoryAdapter extends ArrayAdapter<BillRecord> {

    private final LayoutInflater inflater;

    public HistoryAdapter(@NonNull Context context, @NonNull List<BillRecord> bills) {
        super(context, 0, bills);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_history, parent, false);
            holder = new ViewHolder();
            holder.tvMonthIcon = convertView.findViewById(R.id.tvMonthIcon);
            holder.tvItemMonth = convertView.findViewById(R.id.tvItemMonth);
            holder.tvItemFinalCost = convertView.findViewById(R.id.tvItemFinalCost);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BillRecord record = getItem(position);
        if (record != null) {
            // Show first letter of the month in the circle
            String monthInitial = record.getMonth().substring(0, 1).toUpperCase();
            holder.tvMonthIcon.setText(monthInitial);

            holder.tvItemMonth.setText(record.getMonth());
            holder.tvItemFinalCost.setText(
                    String.format(Locale.getDefault(), "RM %.2f", record.getFinalCost()));
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvMonthIcon;
        TextView tvItemMonth;
        TextView tvItemFinalCost;
    }
}

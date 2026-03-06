package com.example.water_logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryDayFragment extends Fragment {

    private DatabaseHelper db;
    private long currentDayMillis;
    private int userId;

    private static final String PREFS_NAME = "WaterLoggerPrefs";
    private static final String KEY_USER_ID = "userId";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_day, container, false);

        db = new DatabaseHelper(getContext());
        currentDayMillis = System.currentTimeMillis();

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(KEY_USER_ID, -1);

        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        ImageView ivPreviousDay = view.findViewById(R.id.ivPreviousDay);
        ImageView ivNextDay = view.findViewById(R.id.ivNextDay);
        BarChart barChart = view.findViewById(R.id.barChartDay);

        ivPreviousDay.setOnClickListener(v -> {
            currentDayMillis -= 24 * 60 * 60 * 1000;
            updateChart(tvSelectedDate, barChart);
        });

        ivNextDay.setOnClickListener(v -> {
            currentDayMillis += 24 * 60 * 60 * 1000;
            updateChart(tvSelectedDate, barChart);
        });

        setupChart(barChart);
        updateChart(tvSelectedDate, barChart);

        return view;
    }

    private void setupChart(BarChart barChart) {
        barChart.setRenderer(new RoundedBarChartRenderer(barChart, barChart.getAnimator(), barChart.getViewPortHandler(), 20f));
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setTouchEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getHours()));
    }

    private void updateChart(TextView tvSelectedDate, BarChart barChart) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(currentDayMillis));

        if (userId != -1) {
            int[] hourlyIntake = db.getHourlyIntakeForDay(userId, currentDayMillis);
            ArrayList<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < hourlyIntake.length; i++) {
                entries.add(new BarEntry(i, hourlyIntake[i]));
            }

            BarDataSet dataSet = new BarDataSet(entries, "Hourly Water Intake");
            dataSet.setColor(ContextCompat.getColor(getContext(), R.color.light_blue));
            dataSet.setDrawValues(false);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.6f);

            barChart.setData(barData);
            barChart.invalidate(); // refresh
        }
    }

    private String[] getHours() {
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format(Locale.getDefault(), "%02d", i);
        }
        return hours;
    }
}
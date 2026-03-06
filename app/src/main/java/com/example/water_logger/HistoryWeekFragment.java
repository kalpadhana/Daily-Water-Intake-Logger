package com.example.water_logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Calendar;
import java.util.Locale;

public class HistoryWeekFragment extends Fragment {

    private DatabaseHelper db;
    private long currentDayMillis;
    private int userId;

    private static final String PREFS_NAME = "WaterLoggerPrefs";
    private static final String KEY_USER_ID = "userId";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_week, container, false);

        db = new DatabaseHelper(getContext());
        currentDayMillis = System.currentTimeMillis();

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(KEY_USER_ID, -1);

        TextView tvDateRange = view.findViewById(R.id.tvDateRange);
        BarChart barChart = view.findViewById(R.id.barChartWeek);

        setupChart(barChart);
        updateChart(tvDateRange, barChart);

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
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getWeekDays()));
    }

    private void updateChart(TextView tvDateRange, BarChart barChart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentDayMillis);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        long startOfWeek = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, 6);
        long endOfWeek = calendar.getTimeInMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
        tvDateRange.setText(String.format(Locale.getDefault(), "%s - %s, %d", sdf.format(startOfWeek), sdf.format(endOfWeek), calendar.get(Calendar.YEAR)));

        if (userId != -1) {
            int[] dailyIntake = db.getDailyIntakeForWeek(userId, currentDayMillis);
            ArrayList<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < dailyIntake.length; i++) {
                entries.add(new BarEntry(i, dailyIntake[i]));
            }

            BarDataSet dataSet = new BarDataSet(entries, "Daily Water Intake");
            dataSet.setColor(ContextCompat.getColor(getContext(), R.color.light_blue));
            dataSet.setDrawValues(false);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.6f);

            barChart.setData(barData);
            barChart.invalidate(); // refresh
        }
    }

    private String[] getWeekDays() {
        return new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    }
}
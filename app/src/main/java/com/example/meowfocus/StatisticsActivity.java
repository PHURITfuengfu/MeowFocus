package com.example.meowfocus;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private ImageView btnBack, homeImageStats, catImageStats;
    private BarChart barChart;
    private TextView tvTotalTime, tvCurrentDate, tvSelectedDate;
    private Button btnPrevDay, btnNextDay;
    private SharedPreferences sharedPreferences;

    private CatModel currentCat;
    private Calendar selectedCalendar;

    // ✅ เพิ่มตัวแปรภาษา
    private String currentLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ โหลดและตั้งค่าภาษาก่อน
        loadLanguagePreference();
        setAppLocale(currentLanguage);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        barChart = findViewById(R.id.barChart);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);
        homeImageStats = findViewById(R.id.homeImageStats);
        catImageStats = findViewById(R.id.catImageStats);

        sharedPreferences = getSharedPreferences("MeowFocusPrefs", MODE_PRIVATE);
        selectedCalendar = Calendar.getInstance();

        currentCat = CatManager.getCurrentCat(this);

        loadHomeAndCatImages();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPrevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDate(-1);
            }
        });

        btnNextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDate(1);
            }
        });

        updateChart();
    }

    // ✅ Method 1: โหลดภาษา
    private void loadLanguagePreference() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        currentLanguage = prefs.getString("My_Lang", "en");
    }

    // ✅ Method 2: ตั้งค่าภาษา
    private void setAppLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            createConfigurationContext(config);
        }

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void loadHomeAndCatImages() {
        String homeDrawableName = sharedPreferences.getString("selected_home", "ic_home");
        int homeResId = getResources().getIdentifier(homeDrawableName, "drawable", getPackageName());
        if (homeResId != 0) {
            homeImageStats.setImageResource(homeResId);
        }

        if (currentCat != null) {
            int catResId = getResources().getIdentifier(
                    currentCat.getWalkFrame1(),
                    "drawable",
                    getPackageName()
            );

            if (catResId != 0) {
                catImageStats.setImageResource(catResId);
                catImageStats.setTranslationX(30);
                catImageStats.setTranslationY(-170);

                android.util.Log.d("StatisticsActivity", "Cat ID: " + currentCat.getCatId());
                android.util.Log.d("StatisticsActivity", "Walk Frame1: " + currentCat.getWalkFrame1());
            } else {
                android.util.Log.e("StatisticsActivity", "Cannot find cat image: " + currentCat.getWalkFrame1());
            }
        }
    }

    private void changeDate(int days) {
        selectedCalendar.add(Calendar.DAY_OF_MONTH, days);
        updateChart();
    }

    private void updateChart() {
        // ✅ ใช้ Locale ตามภาษาที่เลือก
        Locale displayLocale = currentLanguage.equals("th") ? new Locale("th") : Locale.ENGLISH;

        SimpleDateFormat sdfFull = new SimpleDateFormat("MMMM d, yyyy", displayLocale);
        SimpleDateFormat sdfShort = new SimpleDateFormat("MMM d", displayLocale);

        String fullDate = sdfFull.format(selectedCalendar.getTime());
        Calendar today = Calendar.getInstance();
        if (isSameDay(selectedCalendar, today)) {
            // ✅ แสดง "Today" หรือ "วันนี้" ตามภาษา
            String todayText = currentLanguage.equals("th") ? "วันนี้" : "Today";
            tvCurrentDate.setText(fullDate + " (" + todayText + ")");
        } else {
            tvCurrentDate.setText(fullDate);
        }

        tvSelectedDate.setText(sdfShort.format(selectedCalendar.getTime()));

        String dateKey = getDateKey();
        String focusDataJson = sharedPreferences.getString(dateKey, "");

        ArrayList<BarEntry> entries = new ArrayList<>();
        float totalMinutes = 0;

        if (focusDataJson.isEmpty()) {
            for (int i = 0; i < 24; i++) {
                entries.add(new BarEntry(i, 0));
            }
        } else {
            String[] hourData = focusDataJson.split(",");
            for (int i = 0; i < 24; i++) {
                float minutes = i < hourData.length ? Float.parseFloat(hourData[i]) : 0;
                entries.add(new BarEntry(i, minutes));
                totalMinutes += minutes;
            }
        }

        int hours = (int) (totalMinutes / 60);
        int minutes = (int) (totalMinutes % 60);
        int seconds = (int) ((totalMinutes - (int)totalMinutes) * 60);

        // ✅ ใช้ string resource พร้อม format
        String timeText = getString(R.string.total_focus_time_format, hours, minutes, seconds);
        tvTotalTime.setText(timeText);

        BarDataSet dataSet = new BarDataSet(entries, "Minutes");
        dataSet.setColor(Color.parseColor("#CA9158"));
        dataSet.setValueTextSize(0f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setTouchEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.parseColor("#666666"));

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int hour = (int) value;
                if (hour == 0 || hour == 6 || hour == 12 || hour == 18 || hour == 23) {
                    return String.format("%02d:00", hour);
                }
                return "";
            }
        });
        xAxis.setLabelCount(24, false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(60f);
        leftAxis.setGranularity(10f);
        leftAxis.setLabelCount(7, true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setEnabled(true);
        leftAxis.setTextSize(10f);
        leftAxis.setTextColor(Color.parseColor("#666666"));

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.invalidate();
    }

    private String getDateKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return "focus_data_" + sdf.format(selectedCalendar.getTime());
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}

package com.example.meowfocus;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class BeginnerChallengesActivity extends AppCompatActivity {
    private static final String PREF_NAME = "MeowFocusPrefs";
    private SharedPreferences sharedPreferences;

    // Challenge 1
    private ProgressBar progressFirstFocus;
    private TextView tvFirstFocusStatus;
    private Button btnFirstFocusClaim;

    // Challenge 2
    private ProgressBar progressFocus5min;
    private TextView tvFocus5minStatus;
    private Button btnFocus5minClaim;

    // Challenge 3
    private ProgressBar progressFocus10min;
    private TextView tvFocus10minStatus;
    private Button btnFocus10minClaim;

    // Challenge 4
    private ProgressBar progressFeed5;
    private TextView tvFeed5Status;
    private Button btnFeed5Claim;

    // ✅ เพิ่มตัวแปรภาษา
    private String currentLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ โหลดและตั้งค่าภาษาก่อน
        loadLanguagePreference();
        setAppLocale(currentLanguage);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beginner_challenges);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize views
        progressFirstFocus = findViewById(R.id.progressFirstFocus);
        tvFirstFocusStatus = findViewById(R.id.tvFirstFocusStatus);
        btnFirstFocusClaim = findViewById(R.id.btnFirstFocusClaim);

        progressFocus5min = findViewById(R.id.progressFocus5min);
        tvFocus5minStatus = findViewById(R.id.tvFocus5minStatus);
        btnFocus5minClaim = findViewById(R.id.btnFocus5minClaim);

        progressFocus10min = findViewById(R.id.progressFocus10min);
        tvFocus10minStatus = findViewById(R.id.tvFocus10minStatus);
        btnFocus10minClaim = findViewById(R.id.btnFocus10minClaim);

        progressFeed5 = findViewById(R.id.progressFeed5);
        tvFeed5Status = findViewById(R.id.tvFeed5Status);
        btnFeed5Claim = findViewById(R.id.btnFeed5Claim);

        ImageView btnBack = findViewById(R.id.btnBackChallenges);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupMissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMissions();
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

    private void setupMissions() {
        // Load data
        boolean firstFocusDone = sharedPreferences.getBoolean("mission_first_timer_started", false);
        boolean firstFocusClaimed = sharedPreferences.getBoolean("mission_first_reward_claimed", false);
        int totalMins = sharedPreferences.getInt("mission_total_focus_minutes", 0);
        boolean focus5minClaimed = sharedPreferences.getBoolean("mission_focus_5min_claimed", false);
        boolean focus10minClaimed = sharedPreferences.getBoolean("mission_focus_10min_claimed", false);
        int feedCount = sharedPreferences.getInt("mission_feed_cat", 0);
        boolean feed5Claimed = sharedPreferences.getBoolean("mission_feed_cat_claimed", false);

        // ===== Challenge 1: First Focus Session =====
        if (firstFocusClaimed) {
            progressFirstFocus.setProgress(0);
            setProgressBarColor(progressFirstFocus, android.R.color.darker_gray);
            tvFirstFocusStatus.setText(R.string.claimed);
            tvFirstFocusStatus.setTextColor(0xFF4CAF50);
            btnFirstFocusClaim.setEnabled(false);
            btnFirstFocusClaim.setText(R.string.claimed_button);
            btnFirstFocusClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        } else if (firstFocusDone) {
            progressFirstFocus.setProgress(100);
            setProgressBarColor(progressFirstFocus, 0xFF4CAF50);
            String progressText = "1/1";
            tvFirstFocusStatus.setText(getString(R.string.completed_ready, progressText));
            tvFirstFocusStatus.setTextColor(0xFF4CAF50);
            btnFirstFocusClaim.setEnabled(true);
            btnFirstFocusClaim.setText(R.string.first_focus_reward);
            btnFirstFocusClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        } else {
            progressFirstFocus.setProgress(0);
            setProgressBarColor(progressFirstFocus, 0xFF9E9E9E);
            String progressText = "0/1";
            tvFirstFocusStatus.setText(getString(R.string.in_progress, progressText));
            tvFirstFocusStatus.setTextColor(0xFF666666);
            btnFirstFocusClaim.setEnabled(false);
            btnFirstFocusClaim.setText(R.string.claim_reward);
            btnFirstFocusClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }

        btnFirstFocusClaim.setOnClickListener(v -> {
            if (firstFocusDone && !firstFocusClaimed) {
                incrementFishBones(3);
                incrementCoins(5);

                sharedPreferences.edit()
                        .putBoolean("mission_first_reward_claimed", true)
                        .putBoolean("mission_first_timer_started", false)
                        .apply();

                Toast.makeText(this, R.string.first_focus_claimed_toast, Toast.LENGTH_SHORT).show();
                setupMissions();
            }
        });

        // ===== Challenge 2: Focus for 5 Minutes =====
        int progress5min = (int) (Math.min(totalMins, 5) / 5f * 100);
        progressFocus5min.setProgress(progress5min);

        if (focus5minClaimed) {
            setProgressBarColor(progressFocus5min, android.R.color.darker_gray);
            tvFocus5minStatus.setText(R.string.claimed);
            tvFocus5minStatus.setTextColor(0xFF4CAF50);
            btnFocus5minClaim.setEnabled(false);
            btnFocus5minClaim.setText(R.string.claimed_button);
            btnFocus5minClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        } else if (totalMins >= 5) {
            setProgressBarColor(progressFocus5min, 0xFF4CAF50);
            String progressText = totalMins + "/5 " + getString(R.string.minutes);
            tvFocus5minStatus.setText(getString(R.string.completed_ready, progressText));
            tvFocus5minStatus.setTextColor(0xFF4CAF50);
            btnFocus5minClaim.setEnabled(true);
            btnFocus5minClaim.setText(R.string.focus_5min_reward);
            btnFocus5minClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        } else {
            setProgressBarColor(progressFocus5min, 0xFF9E9E9E);
            String progressText = totalMins + "/5 " + getString(R.string.minutes);
            tvFocus5minStatus.setText(getString(R.string.in_progress, progressText));
            tvFocus5minStatus.setTextColor(0xFF666666);
            btnFocus5minClaim.setEnabled(false);
            btnFocus5minClaim.setText(R.string.claim_reward);
            btnFocus5minClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }

        btnFocus5minClaim.setOnClickListener(v -> {
            if (totalMins >= 5 && !focus5minClaimed) {
                incrementFishBones(5);
                incrementCoins(10);

                sharedPreferences.edit()
                        .putBoolean("mission_focus_5min_claimed", true)
                        .putInt("mission_total_focus_minutes", 0)
                        .apply();

                Toast.makeText(this, R.string.focus_5min_claimed_toast, Toast.LENGTH_SHORT).show();
                setupMissions();
            }
        });

        // ===== Challenge 3: Focus for 10 Minutes =====
        int progress10min = (int) (Math.min(totalMins, 10) / 10f * 100);
        progressFocus10min.setProgress(progress10min);

        if (focus10minClaimed) {
            setProgressBarColor(progressFocus10min, android.R.color.darker_gray);
            tvFocus10minStatus.setText(R.string.claimed);
            tvFocus10minStatus.setTextColor(0xFF4CAF50);
            btnFocus10minClaim.setEnabled(false);
            btnFocus10minClaim.setText(R.string.claimed_button);
            btnFocus10minClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        } else if (totalMins >= 10) {
            setProgressBarColor(progressFocus10min, 0xFF4CAF50);
            String progressText = totalMins + "/10 " + getString(R.string.minutes);
            tvFocus10minStatus.setText(getString(R.string.completed_ready, progressText));
            tvFocus10minStatus.setTextColor(0xFF4CAF50);
            btnFocus10minClaim.setEnabled(true);
            btnFocus10minClaim.setText(R.string.focus_10min_reward);
            btnFocus10minClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        } else {
            setProgressBarColor(progressFocus10min, 0xFF9E9E9E);
            String progressText = totalMins + "/10 " + getString(R.string.minutes);
            tvFocus10minStatus.setText(getString(R.string.in_progress, progressText));
            tvFocus10minStatus.setTextColor(0xFF666666);
            btnFocus10minClaim.setEnabled(false);
            btnFocus10minClaim.setText(R.string.claim_reward);
            btnFocus10minClaim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }

        btnFocus10minClaim.setOnClickListener(v -> {
            if (totalMins >= 10 && !focus10minClaimed) {
                incrementFishBones(7);
                incrementCoins(15);

                sharedPreferences.edit()
                        .putBoolean("mission_focus_10min_claimed", true)
                        .putInt("mission_total_focus_minutes", 0)
                        .apply();

                Toast.makeText(this, R.string.focus_10min_claimed_toast, Toast.LENGTH_SHORT).show();
                setupMissions();
            }
        });

        // ===== Challenge 4: Feed Your Cat 5 Times =====
        int progressFeed = (int) (Math.min(feedCount, 5) / 5f * 100);
        progressFeed5.setProgress(progressFeed);

        if (feed5Claimed) {
            setProgressBarColor(progressFeed5, android.R.color.darker_gray);
            tvFeed5Status.setText(R.string.claimed);
            tvFeed5Status.setTextColor(0xFF4CAF50);
            btnFeed5Claim.setEnabled(false);
            btnFeed5Claim.setText(R.string.claimed_button);
            btnFeed5Claim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        } else if (feedCount >= 5) {
            setProgressBarColor(progressFeed5, 0xFF4CAF50);
            String progressText = feedCount + "/5 " + getString(R.string.sessions);
            tvFeed5Status.setText(getString(R.string.completed_ready, progressText));
            tvFeed5Status.setTextColor(0xFF4CAF50);
            btnFeed5Claim.setEnabled(true);
            btnFeed5Claim.setText(R.string.feed_5_reward);
            btnFeed5Claim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        } else {
            setProgressBarColor(progressFeed5, 0xFF9E9E9E);
            String progressText = feedCount + "/5 " + getString(R.string.sessions);
            tvFeed5Status.setText(getString(R.string.in_progress, progressText));
            tvFeed5Status.setTextColor(0xFF666666);
            btnFeed5Claim.setEnabled(false);
            btnFeed5Claim.setText(R.string.claim_reward);
            btnFeed5Claim.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }

        btnFeed5Claim.setOnClickListener(v -> {
            if (feedCount >= 5 && !feed5Claimed) {
                incrementFishBones(10);
                incrementCoins(7);

                sharedPreferences.edit()
                        .putBoolean("mission_feed_cat_claimed", true)
                        .putInt("mission_feed_cat", 0)
                        .apply();

                Toast.makeText(this, R.string.feed_5_claimed_toast, Toast.LENGTH_SHORT).show();
                setupMissions();
            }
        });
    }

    private void setProgressBarColor(ProgressBar progressBar, int color) {
        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void incrementFishBones(int amount) {
        int fishBones = sharedPreferences.getInt("fishBones", 0) + amount;
        sharedPreferences.edit().putInt("fishBones", fishBones).apply();
    }

    private void incrementCoins(int amount) {
        int coins = sharedPreferences.getInt("coins", 0) + amount;
        sharedPreferences.edit().putInt("coins", coins).apply();
    }
}

package com.example.meowfocus;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.content.ClipData;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.drawable.GradientDrawable;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;

public class FocusActivity extends AppCompatActivity {

    // Navigation Drawer variables
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private OnBackPressedCallback onBackPressedCallback;
    private Button btnLanguageThai, btnLanguageEnglish;
    private String currentLanguage = "en";
    // Original variables
    private TextView catNameDisplay, timerDisplay, fishBoneCount, coinCount;
    private ImageView fishBoneIcon, catImageView, catWalkView;
    private Button focusButton, startTimerButton, backButton;
    private ProgressBar hungerBar;
    private FrameLayout timerDialogOverlay, roomContainer;
    private LinearLayout timerDialogBox;
    private Switch musicSwitch;
    private RecyclerView minutesPicker;
    private MinuteAdapter minuteAdapter;
    private EditText selectedMinutesInput, selectedSecondsInput;
    private int selectedMinute = 15;
    private LinearLayoutManager layoutManager;
    private SnapHelper snapHelper;
    private SharedPreferences sharedPreferences;
    private CountDownTimer countDownTimer;
    private int fishBones = 10, coins, hungerLevel = 50;
    private long timeLeftInMillis = 40 * 60 * 1000;
    private Handler hungerHandler;
    private Runnable hungerRunnable;
    private static final int HUNGER_DECREASE_INTERVAL = 5000; // 2 วินาที
    private static final int HUNGER_DECREASE_AMOUNT = 10;

    private long originalTimeInMillis = 0;
    private boolean isTimerRunning = false;
    private ObjectAnimator moveX;
    private ObjectAnimator moveY;
    private boolean isCatAtTable = false;
    private CatModel currentCat;

    // บันทึกเวลาที่โฟกัส
    private void saveFocusTime(long milliseconds) {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        float minutes = milliseconds / 60000f;

        android.util.Log.d("FocusActivity", "Saving focus time: " + minutes + " minutes at hour " + currentHour);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = "focus_data_" + sdf.format(now.getTime());

        String existingData = sharedPreferences.getString(dateKey, "");
        float[] hourData = new float[24];

        if (!existingData.isEmpty()) {
            String[] parts = existingData.split(",");
            for (int i = 0; i < Math.min(parts.length, 24); i++) {
                try {
                    hourData[i] = Float.parseFloat(parts[i]);
                } catch (NumberFormatException e) {
                    hourData[i] = 0f;
                }
            }
        }

        hourData[currentHour] += minutes;

        android.util.Log.d("FocusActivity", "Total minutes at hour " + currentHour + ": " + hourData[currentHour]);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            sb.append(hourData[i]);
            if (i < 23) sb.append(",");
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(dateKey, sb.toString());
        editor.apply();

        android.util.Log.d("FocusActivity", "Saved data: " + sb.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("MeowFocusPrefs", MODE_PRIVATE);
        loadLanguagePreference();
        setAppLocale(currentLanguage);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);

        sharedPreferences = getSharedPreferences("MeowFocusPrefs", Context.MODE_PRIVATE);
        currentCat = CatManager.getCurrentCat(this);

        // Debug
        android.util.Log.d("FocusActivity", "Current Cat ID: " + currentCat.getCatId());
        android.util.Log.d("FocusActivity", "Walk Animation: " + currentCat.getWalkAnimation());

        // Initialize Navigation Drawer
        initializeNavigationDrawer();

        setupLanguageButtons();

        // Setup Back Press Handler
        setupBackPressHandler();

        // Initialize original views
        initializeViews();

        // Initialize cat walking animation
        initializeCatWalking();

        // ✅ โหลดชื่อแมวจาก ShakeActivity
        String catName = sharedPreferences.getString("cat_name", "Your Cat");
        if (catNameDisplay != null) {
            catNameDisplay.setText(catName);
        }

        // Debug
        android.util.Log.d("FocusActivity", "Cat Name: " + catName);

        // ตรวจสอบว่าเป็นครั้งแรกหรือไม่
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            fishBones = 10;
            coins = 5;
            hungerLevel = 50;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("fishBones", fishBones);
            editor.putInt("coins", coins);
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            android.util.Log.d("FocusActivity", "First time setup: fishBones=10, coins=5");
        } else {
            fishBones = sharedPreferences.getInt("fishBones", 10);
            coins = sharedPreferences.getInt("coins", 5);
            hungerLevel = 50;

            android.util.Log.d("FocusActivity", "Loaded: fishBones=" + fishBones + ", coins=" + coins);
        }

        if (fishBoneCount != null) {
            fishBoneCount.setText(String.valueOf(fishBones));
        }
        if (coinCount != null) {
            coinCount.setText(String.valueOf(coins));
        }
        if (hungerBar != null) {
            hungerBar.setProgress(hungerLevel);
        }

        setupTextWatchers();
        setupMinutesPicker();
        setupDragAndDrop();
        setupTimerDialog();

        hungerHandler = new Handler();
        startHungerDecrease();

        if (focusButton != null) {
            focusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isTimerRunning) {
                        pauseTimer();
                    } else {
                        showTimerDialog();
                    }
                }
            });
        }

        loadHomeImage();

        if (roomContainer != null) {
            loadHomeDecorations();
        }
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

    // ✅ Method 3: ตั้งค่าปุ่ม
    private void setupLanguageButtons() {
        View headerView = navigationView.getHeaderView(0);
        btnLanguageThai = headerView.findViewById(R.id.btnLanguageThai);
        btnLanguageEnglish = headerView.findViewById(R.id.btnLanguageEnglish);

        updateLanguageButtons();

        btnLanguageThai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("th");
            }
        });

        btnLanguageEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("en");
            }
        });
    }

    // ✅ Method 4: เปลี่ยนภาษา
    private void changeLanguage(String lang) {
        if (!currentLanguage.equals(lang)) {
            currentLanguage = lang;

            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putString("My_Lang", lang);
            editor.apply();

            recreate();
        }
    }

    // ✅ Method 5: อัปเดตสีปุ่ม
    private void updateLanguageButtons() {
        if (btnLanguageThai == null || btnLanguageEnglish == null) return;

        if (currentLanguage.equals("th")) {
            // ภาษาไทย - ปุ่มไทยสีน้ำตาล, อังกฤษสีเทา
            btnLanguageThai.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCA9158));
            btnLanguageEnglish.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFA0A0A0));
        } else {
            // ภาษาอังกฤษ - ปุ่มอังกฤษสีน้ำตาล, ไทยสีเทา
            btnLanguageEnglish.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCA9158));
            btnLanguageThai.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFA0A0A0));
        }
    }

    // เริ่มลดหลอดอาหารทุกๆ 2 วินาที
    private void startHungerDecrease() {
        hungerRunnable = new Runnable() {
            @Override
            public void run() {
                // ลดหลอดอาหารเฉพาะตอนไม่ได้จับเวลา
                if (!isTimerRunning) {
                    decreaseHunger();
                }

                // เรียกซ้ำทุกๆ 2 วินาที
                hungerHandler.postDelayed(this, HUNGER_DECREASE_INTERVAL);
            }
        };

        // เริ่มต้นการลดหลอดอาหาร
        hungerHandler.postDelayed(hungerRunnable, HUNGER_DECREASE_INTERVAL);
    }

    // ฟังก์ชันลดหลอดอาหาร
    // ฟังก์ชันลดหลอดอาหาร
    private void decreaseHunger() {
        if (hungerLevel > 0) {
            int oldHungerLevel = hungerLevel;
            hungerLevel = Math.max(hungerLevel - HUNGER_DECREASE_AMOUNT, 0);

            if (hungerBar != null) {
                ObjectAnimator animation = ObjectAnimator.ofInt(hungerBar, "progress", oldHungerLevel, hungerLevel);
                animation.setDuration(300);
                animation.start();
            }

            // ✅ แจ้งเตือนและเปลี่ยนภาพแมวเมื่อหลอดอาหารหมด
            if (hungerLevel == 0) {
                Toast.makeText(this, R.string.cat_starving, Toast.LENGTH_SHORT).show();

                // ปิดใช้งานปุ่ม FOCUS
                if (focusButton != null) {
                    focusButton.setEnabled(false);
                    focusButton.setAlpha(0.5f);
                }

                // ✅ หยุดแอนิเมชันเดิน
                stopCatWalking();

                // ✅ เปลี่ยนเป็นภาพแมวหิว
                showHungryCat();
            }
        }
    }

    // หยุดแอนิเมชันเดินของแมว
    private void stopCatWalking() {
        // หยุด ObjectAnimator
        if (moveX != null && moveX.isRunning()) {
            moveX.cancel();
        }
        if (moveY != null && moveY.isRunning()) {
            moveY.cancel();
        }

        // หยุด AnimationDrawable
        if (catWalkView != null) {
            AnimationDrawable catAnim = (AnimationDrawable) catWalkView.getBackground();
            if (catAnim != null && catAnim.isRunning()) {
                catAnim.stop();
            }
        }
    }

    // แสดงภาพแมวหิว (นอนราบ)
    private void showHungryCat() {
        if (catWalkView != null) {
            // ✅ ลองใช้ภาพหิวก่อน
            int hungryImageId = getResources().getIdentifier(
                    currentCat.getHungryImage(),
                    "drawable",
                    getPackageName()
            );

            // ✅ ถ้าไม่มีภาพหิว ให้ใช้ภาพนั่งแทน
            if (hungryImageId == 0) {
                hungryImageId = getResources().getIdentifier(
                        currentCat.getSitImage(),  // ✅ แก้เป็น getSitImage() แทน getHungryImage()
                        "drawable",
                        getPackageName()
                );
                android.util.Log.w("FocusActivity", "No hungry image found, using sit image instead");
            }

            if (hungryImageId != 0) {
                catWalkView.setBackgroundResource(hungryImageId);

                // ✅ Debug: ตรวจสอบว่าได้ภาพอะไร
                android.util.Log.d("FocusActivity", "Showing hungry cat: " + currentCat.getHungryImage());

                // ✅ ปรับขนาดให้เท่ากับแมวเดิน (100dp)
                float dpToPx = catWalkView.getResources().getDisplayMetrics().density;

                android.view.ViewGroup.LayoutParams params = catWalkView.getLayoutParams();
                params.width = (int) (100 * dpToPx);   // ✅ เปลี่ยนจาก 150 เป็น 100
                params.height = (int) (100 * dpToPx);  // ✅ เปลี่ยนจาก 150 เป็น 100
                catWalkView.setLayoutParams(params);

                float positionX = 130;  // ✅ ปรับค่านี้เพื่อเลื่อนซ้าย-ขวา
                float positionY = -110;  // ✅ ปรับค่านี้เพื่อเลื่อนบน-ล่าง

                catWalkView.setTranslationX(positionX * dpToPx);
                catWalkView.setTranslationY(positionY * dpToPx);

                android.util.Log.d("FocusActivity", "Hungry cat position - X: " + positionX + ", Y: " + positionY);
            } else {
                android.util.Log.e("FocusActivity", "Cannot find any image for hungry cat!");
            }
        }
    }

    // กลับไปเดินปกติ
    private void resumeCatWalking() {
        if (catWalkView != null) {
            // ✅ รีเซ็ตขนาดภาพกลับเป็นปกติ
            android.view.ViewGroup.LayoutParams params = catWalkView.getLayoutParams();
            params.width = (int) (100 * catWalkView.getResources().getDisplayMetrics().density);
            params.height = (int) (100 * catWalkView.getResources().getDisplayMetrics().density);
            catWalkView.setLayoutParams(params);

            // ใช้แอนิเมชันเดินของแมวที่ถูกต้อง
            int walkAnimId = getResources().getIdentifier(
                    currentCat.getWalkAnimation(),
                    "drawable",
                    getPackageName()
            );
            catWalkView.setBackgroundResource(walkAnimId);

            AnimationDrawable catAnim = (AnimationDrawable) catWalkView.getBackground();
            if (catAnim != null) {
                catAnim.start();
            }

            float dpToPx = catWalkView.getResources().getDisplayMetrics().density;
            catWalkView.setTranslationY(-70 * dpToPx);
            catWalkView.setTranslationX(150 * dpToPx);

            if (!isTimerRunning) {
                float fromX = catWalkView.getTranslationX();
                float toX = fromX + (-20 * dpToPx);
                float fromY = catWalkView.getTranslationY();
                float toY = fromY - (80 * dpToPx);

                moveX = ObjectAnimator.ofFloat(catWalkView, "translationX", fromX, toX);
                moveX.setDuration(3500);
                moveX.setRepeatCount(ObjectAnimator.INFINITE);
                moveX.setRepeatMode(ObjectAnimator.REVERSE);

                moveY = ObjectAnimator.ofFloat(catWalkView, "translationY", fromY, toY);
                moveY.setDuration(3500);
                moveY.setRepeatCount(ObjectAnimator.INFINITE);
                moveY.setRepeatMode(ObjectAnimator.REVERSE);

                moveX.start();
                moveY.start();
            }
        }
    }

    // หยุดการลดหลอดอาหาร
    private void stopHungerDecrease() {
        if (hungerHandler != null && hungerRunnable != null) {
            hungerHandler.removeCallbacks(hungerRunnable);
        }
    }

    private void setupBackPressHandler() {
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    if (isEnabled()) {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void initializeNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            // ✅ ซ่อนชื่อแอปบน Toolbar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    0, 0);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        if (navigationView != null) {
            setupEditNameIcon();
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_statistics) {
                    Intent intent = new Intent(FocusActivity.this, StatisticsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_challenges) {
                    Intent intent = new Intent(FocusActivity.this, BeginnerChallengesActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_shop) {
                    Intent intent = new Intent(FocusActivity.this, ShopActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    showLogoutDialog();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    // แสดง Dialog ยืนยันการ Logout
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)  // ✅
                .setMessage(R.string.logout_message)  // ✅
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {  // ✅
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton(R.string.cancel, null)  // ✅
                .show();
    }

    // ทำการ Logout และรีเซ็ตข้อมูลทั้งหมด
    private void performLogout() {
        // หยุด Timer ถ้ากำลังรันอยู่
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // หยุดแอนิเมชัน
        if (moveX != null && moveX.isRunning()) {
            moveX.cancel();
        }
        if (moveY != null && moveY.isRunning()) {
            moveY.cancel();
        }

        // ✅ รีเซ็ตแมว
        CatManager.resetCat(this);

        // ลบข้อมูลทั้งหมดใน SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // กลับไปหน้า Login
        Intent intent = new Intent(FocusActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // ปิด Activity ปัจจุบัน
        finish();

        Toast.makeText(this, R.string.logged_out_success, Toast.LENGTH_SHORT).show();
    }

    // บันทึกตัวเลือกแมว
    private void saveSelectedCat(String catDrawableName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_cat", catDrawableName);
        editor.apply();
    }

    // บันทึกตัวเลือกบ้าน
    private void saveSelectedHome(String homeDrawableName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_home", homeDrawableName);
        editor.apply();
    }

    // ตั้งค่าโปรไฟล์ / ปากกาเปลี่ยนชื่อ
    private void setupEditNameIcon() {
        if (navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        TextView profileName = headerView.findViewById(R.id.profile_name);
        ImageView editIcon = headerView.findViewById(R.id.edit_name_icon);

        String currentName = sharedPreferences.getString("user_name", "User Name");
        if (profileName != null) {
            profileName.setText(currentName);
        }

        if (editIcon != null) {
            editIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditNameDialog();
                }
            });
        }

        if (profileName != null) {
            profileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditNameDialog();
                }
            });
        }
    }

    private void loadHomeImage() {
        ImageView homeImageView = findViewById(R.id.isometricRoom);
        if (homeImageView != null) {
            String homeDrawable = sharedPreferences.getString("selected_home", "ic_home");
            int resId = getResources().getIdentifier(homeDrawable, "drawable", getPackageName());
            if (resId != 0) {
                homeImageView.setImageResource(resId);
            }
        }
    }

    // โหลดของตกแต่งห้องจาก SharedPreferences
    private void loadHomeDecorations() {
        if (roomContainer == null) return;

        Set<String> items = sharedPreferences.getStringSet("purchased_items", new HashSet<String>());

        for (String item : items) {
            if (item.equals("cat_tree")) {
                ImageView catTree = new ImageView(this);
                catTree.setImageResource(R.drawable.cat_tree);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        (int) (80 * getResources().getDisplayMetrics().density),
                        (int) (120 * getResources().getDisplayMetrics().density));
                params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
                params.setMargins(0, 0,
                        (int) (30 * getResources().getDisplayMetrics().density),
                        (int) (20 * getResources().getDisplayMetrics().density));
                catTree.setLayoutParams(params);

                // ไม่กำหนด id เพื่อให้ลบออกได้ใน onResume
                catTree.setId(View.NO_ID);
                roomContainer.addView(catTree);

            } else if (item.equals("lamp")) {
                ImageView lamp = new ImageView(this);
                lamp.setImageResource(R.drawable.lamp);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        (int) (60 * getResources().getDisplayMetrics().density),
                        (int) (100 * getResources().getDisplayMetrics().density));
                params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
                params.setMargins(
                        (int) (30 * getResources().getDisplayMetrics().density),
                        0,
                        0,
                        (int) (20 * getResources().getDisplayMetrics().density));
                lamp.setLayoutParams(params);

                lamp.setId(View.NO_ID);
                roomContainer.addView(lamp);
            }
        }
    }

    // Dialog เปลี่ยนชื่อผู้ใช้
    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_name);  // ✅

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);

        String currentName = sharedPreferences.getString("user_name", getString(R.string.user_name));  // ✅
        input.setText(currentName);
        input.setSelection(currentName.length());

        builder.setView(input);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {  // ✅
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();

                if (newName.isEmpty()) {
                    Toast.makeText(FocusActivity.this, R.string.name_cannot_empty, Toast.LENGTH_SHORT).show();  // ✅
                    return;
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("user_name", newName);
                editor.apply();

                if (navigationView != null) {
                    View headerView = navigationView.getHeaderView(0);
                    TextView profileName = headerView.findViewById(R.id.profile_name);
                    if (profileName != null) {
                        profileName.setText(newName);
                    }
                }

                Toast.makeText(FocusActivity.this, R.string.name_updated, Toast.LENGTH_SHORT).show();  // ✅
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {  // ✅
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void initializeViews() {
        catNameDisplay = findViewById(R.id.catNameDisplay);
        timerDisplay = findViewById(R.id.timerDisplay);
        fishBoneCount = findViewById(R.id.fishBoneCount);
        coinCount = findViewById(R.id.coinCount);
        fishBoneIcon = findViewById(R.id.fishBoneIcon);
        catImageView = findViewById(R.id.catImageView);
        focusButton = findViewById(R.id.focusButton);
        hungerBar = findViewById(R.id.hungerBar);
        timerDialogOverlay = findViewById(R.id.timerDialogOverlay);
        timerDialogBox = findViewById(R.id.timerDialogBox);
        startTimerButton = findViewById(R.id.startTimerButton);
        backButton = findViewById(R.id.backButton);
        selectedMinutesInput = findViewById(R.id.selectedMinutes);
        selectedSecondsInput = findViewById(R.id.selectedSeconds);
        minutesPicker = findViewById(R.id.minutesPicker);
        roomContainer = findViewById(R.id.roomContainer);
        catWalkView = findViewById(R.id.catWalkView);
    }

    private void initializeCatWalking() {
        android.view.ViewGroup.LayoutParams params = catWalkView.getLayoutParams();
        params.width = (int) (100 * catWalkView.getResources().getDisplayMetrics().density);
        params.height = (int) (100 * catWalkView.getResources().getDisplayMetrics().density);
        catWalkView.setLayoutParams(params);
        if (roomContainer != null && catWalkView != null) {
            roomContainer.post(new Runnable() {
                @Override
                public void run() {
                    // ✅ ใช้แอนิเมชันเดินของแมวที่สุ่มได้
                    int walkAnimId = getResources().getIdentifier(
                            currentCat.getWalkAnimation(),
                            "drawable",
                            getPackageName()
                    );
                    catWalkView.setBackgroundResource(walkAnimId);

                    AnimationDrawable catAnim = (AnimationDrawable) catWalkView.getBackground();
                    if (catAnim != null) {
                        catAnim.start();
                    }

                    float dpToPx = catWalkView.getResources().getDisplayMetrics().density;
                    catWalkView.setTranslationY(-70 * dpToPx);
                    catWalkView.setTranslationX(150 * dpToPx);

                    float fromX = catWalkView.getTranslationX();
                    float toX = fromX + (-20 * dpToPx);
                    float fromY = catWalkView.getTranslationY();
                    float toY = fromY - (80 * dpToPx);

                    moveX = ObjectAnimator.ofFloat(catWalkView, "translationX", fromX, toX);
                    moveX.setDuration(3500);
                    moveX.setRepeatCount(ObjectAnimator.INFINITE);
                    moveX.setRepeatMode(ObjectAnimator.REVERSE);

                    moveY = ObjectAnimator.ofFloat(catWalkView, "translationY", fromY, toY);
                    moveY.setDuration(3500);
                    moveY.setRepeatCount(ObjectAnimator.INFINITE);
                    moveY.setRepeatMode(ObjectAnimator.REVERSE);

                    if (!isTimerRunning) {
                        moveX.start();
                        moveY.start();
                    }
                }
            });
        }
    }

    private void setupTextWatchers() {
        if (selectedMinutesInput == null || selectedSecondsInput == null) return;

        selectedMinutesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        int minutes = Integer.parseInt(s.toString());
                        if (minutes > 60) {
                            selectedMinutesInput.setText("60");
                            selectedMinutesInput.setSelection(2);
                        }
                        selectedMinute = Math.min(minutes, 60);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        selectedSecondsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        int seconds = Integer.parseInt(s.toString());
                        if (seconds > 59) {
                            selectedSecondsInput.setText("59");
                            selectedSecondsInput.setSelection(2);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setupDragAndDrop() {
        if (fishBoneIcon == null || catImageView == null) {
            return;
        }

        fishBoneIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isTimerRunning) {
                        Toast.makeText(FocusActivity.this, R.string.cannot_feed_while_focusing, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if (fishBones <= 0) {
                        // ✅ แก้จุดที่ 1
                        Toast.makeText(FocusActivity.this, R.string.no_fish_bones, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if (hungerLevel >= 100) {
                        // ✅ แก้จุดที่ 2
                        Toast.makeText(FocusActivity.this, R.string.cat_full, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    try {
                        // ✅ สร้าง Bitmap จากภาพก้างปลา
                        ImageView tempImageView = new ImageView(FocusActivity.this);
                        tempImageView.setImageResource(R.drawable.fish_bone);

                        int size = (int) (70 * getResources().getDisplayMetrics().density);
                        tempImageView.setLayoutParams(new ViewGroup.LayoutParams(size, size));

                        tempImageView.measure(
                                View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
                        );
                        tempImageView.layout(0, 0, size, size);

                        // ✅ สร้าง DragShadow
                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(tempImageView);
                        ClipData data = ClipData.newPlainText("fishbone", "drag");

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            v.startDragAndDrop(data, shadowBuilder, null, 0);
                        } else {
                            v.startDrag(data, shadowBuilder, null, 0);
                        }

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return false;
            }
        });

        catImageView.setOnDragListener(createDragListener());

        View isometricRoom = findViewById(R.id.isometricRoom);
        if (isometricRoom != null) {
            isometricRoom.setOnDragListener(createDragListener());
        }
    }

    private View.OnDragListener createDragListener() {
        return new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                try {
                    switch (event.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            return true;

                        case DragEvent.ACTION_DRAG_ENTERED:
                            if (catImageView != null) {
                                catImageView.setAlpha(0.7f);
                            }
                            return true;

                        case DragEvent.ACTION_DRAG_EXITED:
                            if (catImageView != null) {
                                catImageView.setAlpha(1.0f);
                            }
                            return true;

                        case DragEvent.ACTION_DROP:
                            if (catImageView != null) {
                                catImageView.setAlpha(1.0f);
                            }
                            feedCat();
                            return true;

                        case DragEvent.ACTION_DRAG_ENDED:
                            if (catImageView != null) {
                                catImageView.setAlpha(1.0f);
                            }
                            return true;

                        default:
                            return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    private void feedCat() {
        try {
            if (fishBones <= 0) {
                Toast.makeText(this, R.string.no_fish_bones, Toast.LENGTH_SHORT).show();  // ✅ แก้
                return;
            }

            if (hungerLevel >= 100) {
                Toast.makeText(this, R.string.cat_full, Toast.LENGTH_SHORT).show();  // ✅ แก้
                return;
            }

            fishBones--;

            // ✅ บันทึกก้างปลาลง SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("fishBones", fishBones);

            if (fishBoneCount != null) {
                fishBoneCount.setText(String.valueOf(fishBones));
            }

            // บันทึกภารกิจให้อาหาร
            int feedCount = sharedPreferences.getInt("mission_feed_cat", 0);
            editor.putInt("mission_feed_cat", feedCount + 1);
            editor.apply();

            int oldHungerLevel = hungerLevel;
            hungerLevel = Math.min(hungerLevel + 10, 100);

            if (hungerBar != null) {
                ObjectAnimator animation = ObjectAnimator.ofInt(hungerBar, "progress", oldHungerLevel, hungerLevel);
                animation.setDuration(500);
                animation.start();
            }

            // ปลดล็อกปุ่ม FOCUS ถ้าความหิวเพิ่มขึ้น
            if (hungerLevel > 0 && focusButton != null) {
                focusButton.setEnabled(true);
                focusButton.setAlpha(1.0f);
            }

            // กลับมาเดินถ้าความหิวเพิ่มจาก 0
            if (oldHungerLevel <= 0) {
                resumeCatWalking();
            }

            if (hungerLevel >= 100) {
                Toast.makeText(this, R.string.cat_full, Toast.LENGTH_SHORT).show();  // ✅ แก้
            } else {
                String message = getString(R.string.cat_happy, hungerLevel);  // ✅ แก้
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMinutesPicker() {
        if (minutesPicker == null) return;

        int[] minutes = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        minutesPicker.setLayoutManager(layoutManager);

        MinuteAdapter adapter = new MinuteAdapter(minutes, new MinuteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int minute) {
                if (selectedMinutesInput != null) {
                    selectedMinutesInput.setText(String.valueOf(minute));
                    selectedMinute = minute;
                }
            }
        });
        minutesPicker.setAdapter(adapter);

        // ✅ เพิ่ม SnapHelper
        snapHelper = new androidx.recyclerview.widget.LinearSnapHelper();
        snapHelper.attachToRecyclerView(minutesPicker);

        // ✅ เพิ่ม Scale Effect
        minutesPicker.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updatePickerScale();
            }
        });

        // Scroll to default position (15 minutes = index 3)
        minutesPicker.post(() -> {
            minutesPicker.scrollToPosition(3);
            updatePickerScale();
        });
    }

    // ✅ เพิ่ม method สำหรับ Scale Effect
    private void updatePickerScale() {
        if (minutesPicker == null || layoutManager == null) return;

        int centerX = minutesPicker.getWidth() / 2;

        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            View child = layoutManager.getChildAt(i);
            if (child != null) {
                int childCenterX = (child.getLeft() + child.getRight()) / 2;
                float distance = Math.abs(centerX - childCenterX);
                float maxDistance = minutesPicker.getWidth() / 2f;

                // Calculate scale (1.0 at center, 0.7 at edges)
                float scale = 1.0f - (distance / maxDistance) * 0.3f;
                scale = Math.max(0.7f, Math.min(1.0f, scale));

                // Calculate alpha (1.0 at center, 0.4 at edges)
                float alpha = 1.0f - (distance / maxDistance) * 0.6f;
                alpha = Math.max(0.4f, Math.min(1.0f, alpha));

                child.setScaleX(scale);
                child.setScaleY(scale);
                child.setAlpha(alpha);
            }
        }
    }

    private void setupTimerDialog() {
        timerDialogOverlay = findViewById(R.id.timerDialogOverlay);
        timerDialogBox = findViewById(R.id.timerDialogBox);
        backButton = findViewById(R.id.backButton);
        startTimerButton = findViewById(R.id.startTimerButton);

        // กดนอกกล่องเพื่อปิด
        if (timerDialogOverlay != null) {
            timerDialogOverlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideTimerDialog();
                }
            });
        }

        // ป้องกันไม่ให้ click ผ่านไปที่ overlay
        if (timerDialogBox != null) {
            timerDialogBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Block click - ไม่ทำอะไร
                }
            });
        }

        // ปุ่ม BACK
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideTimerDialog();
                }
            });
        }

        // ปุ่ม START
        if (startTimerButton != null) {
            startTimerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTimerFromDialog();
                    hideTimerDialog();
                }
            });
        }
    }

    private void showTimerDialog() {
        if (hungerLevel <= 0) {
            Toast.makeText(this, R.string.cat_starving, Toast.LENGTH_LONG).show();
            return;
        }

        if (timerDialogOverlay != null && timerDialogBox != null) {
            timerDialogOverlay.setVisibility(View.VISIBLE);
            timerDialogOverlay.setAlpha(0f);

            // Fade in overlay
            timerDialogOverlay.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            // Slide up dialog
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            timerDialogBox.setTranslationY(screenHeight);

            ObjectAnimator animator = ObjectAnimator.ofFloat(timerDialogBox, "translationY", screenHeight, 0f);
            animator.setDuration(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }
    }

    private void hideTimerDialog() {
        if (timerDialogOverlay != null && timerDialogBox != null) {
            // Fade out overlay
            timerDialogOverlay.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .start();

            // Slide down dialog
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            ObjectAnimator animator = ObjectAnimator.ofFloat(timerDialogBox, "translationY", 0f, screenHeight);
            animator.setDuration(250);
            animator.setInterpolator(new DecelerateInterpolator());

            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    super.onAnimationEnd(animation);
                    timerDialogOverlay.setVisibility(View.GONE);
                }
            });

            animator.start();
        }
    }

    private void startTimerFromDialog() {
        try {
            int minutes = 0;
            int seconds = 0;

            if (selectedMinutesInput != null && selectedMinutesInput.getText().length() > 0) {
                minutes = Integer.parseInt(selectedMinutesInput.getText().toString());
            }

            if (selectedSecondsInput != null && selectedSecondsInput.getText().length() > 0) {
                seconds = Integer.parseInt(selectedSecondsInput.getText().toString());
            }

            long totalMillis = (minutes * 60 + seconds) * 1000L;

            if (totalMillis <= 0) {
                Toast.makeText(this, R.string.invalid_time, Toast.LENGTH_SHORT).show();  // ✅ แก้
                return;
            }

            timeLeftInMillis = totalMillis;
            originalTimeInMillis = totalMillis;
            startTimer();

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_time, Toast.LENGTH_SHORT).show();  // ✅ แก้
            e.printStackTrace();
        }
    }

    private void startTimer() {
        // โค้ดเดิมของ startTimer()
        if (moveX != null && moveX.isRunning()) {
            moveX.cancel();
        }
        if (moveY != null && moveY.isRunning()) {
            moveY.cancel();
        }

        if (catWalkView != null) {
            AnimationDrawable catAnim = (AnimationDrawable) catWalkView.getBackground();
            if (catAnim != null && catAnim.isRunning()) {
                catAnim.stop();
            }

            int sitImageId = getResources().getIdentifier(currentCat.getSitImage(), "drawable", getPackageName());
            catWalkView.setBackgroundResource(sitImageId);

            float dpToPx = catWalkView.getResources().getDisplayMetrics().density;
            float deskX = 69 * dpToPx;
            float deskY = -92 * dpToPx;

            catWalkView.animate()
                    .translationX(deskX)
                    .translationY(deskY)
                    .setDuration(700)
                    .start();

            isCatAtTable = true;
        }

        // Navigation Drawer
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        // บันทึก mission
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("mission_first_timer_started", true);
        editor.apply();

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                long usedTime = originalTimeInMillis;
                saveFocusTime(usedTime);

                int focusedMinutes = (int) (usedTime / 60000);
                int currentTotalMinutes = sharedPreferences.getInt("mission_total_focus_minutes", 0);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("mission_total_focus_minutes", currentTotalMinutes + focusedMinutes);
                editor.apply();

                if (focusButton != null) {
                    focusButton.setText(R.string.focus_button);  // ✅ แก้
                    setButtonColor(focusButton, "#CA9158");
                }

                Toast.makeText(FocusActivity.this, R.string.times_up, Toast.LENGTH_SHORT).show();

                // ให้เหรียญ
                coins += 5;
                editor = sharedPreferences.edit();
                editor.putInt("coins", coins);
                editor.apply();

                if (coinCount != null) {
                    coinCount.setText(String.valueOf(coins));
                }

                // Navigation Drawer
                if (drawerLayout != null) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }

                returnCatToWalking();
            }
        }.start();

        isTimerRunning = true;

        if (focusButton != null) {
            focusButton.setText(R.string.pause_button);  // ✅ แก้
            setButtonColor(focusButton, "#EB5757");
        }
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        long usedTime = originalTimeInMillis - timeLeftInMillis;
        if (usedTime > 0) {
            saveFocusTime(usedTime);
        }

        isTimerRunning = false;
        if (focusButton != null) {
            focusButton.setText(R.string.focus_button);
            setButtonColor(focusButton, "#CA9158");
        }

        // ✅ ปลดล็อก Navigation Drawer
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }

        returnCatToWalking();
    }

    private void returnCatToWalking() {
        if (catWalkView != null) {
            // ✅ ดึงแอนิเมชันของแมวที่ถูกต้อง
            int walkAnimId = getResources().getIdentifier(
                    currentCat.getWalkAnimation(),
                    "drawable",
                    getPackageName()
            );
            catWalkView.setBackgroundResource(walkAnimId);

            AnimationDrawable catAnim = (AnimationDrawable) catWalkView.getBackground();
            if (catAnim != null) {
                catAnim.start();
            }

            float dpToPx = catWalkView.getResources().getDisplayMetrics().density;
            float startX = 130 * dpToPx;
            float startY = -50 * dpToPx;

            catWalkView.animate()
                    .translationX(startX)
                    .translationY(startY)
                    .setDuration(500)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            if (!isTimerRunning) {
                                if (moveX != null) moveX.start();
                                if (moveY != null) moveY.start();
                            }
                        }
                    })
                    .start();

            isCatAtTable = false;
        }
    }

    private void setButtonColor(Button button, String colorHex) {
        try {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(android.graphics.Color.parseColor(colorHex));
            drawable.setCornerRadius(16 * getResources().getDisplayMetrics().density);
            button.setBackground(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTimerText() {
        if (timerDisplay != null) {
            int minutes = (int) (timeLeftInMillis / 1000) / 60;
            int seconds = (int) (timeLeftInMillis / 1000) % 60;
            String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            timerDisplay.setText(timeFormatted);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null && isTimerRunning) {
            countDownTimer.cancel();
        }

        // หยุดการลดหลอดอาหารเมื่อแอปอยู่เบื้องหลัง
        stopHungerDecrease();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ โหลดชื่อแมวใหม่ (กรณีแก้ไขชื่อ)
        String catName = sharedPreferences.getString("cat_name", "Your Cat");
        if (catNameDisplay != null) {
            catNameDisplay.setText(catName);
        }

        // โหลดค่าจาก SharedPreferences
        int savedCoins = sharedPreferences.getInt("coins", 5);
        int savedFishBones = sharedPreferences.getInt("fishBones", 10);

        if (savedCoins != coins) {
            coins = savedCoins;
            if (coinCount != null) {
                coinCount.setText(String.valueOf(coins));
            }
            android.util.Log.d("FocusActivity", "Coins updated to: " + coins);
        }

        if (savedFishBones != fishBones) {
            fishBones = savedFishBones;
            if (fishBoneCount != null) {
                fishBoneCount.setText(String.valueOf(fishBones));
            }
            android.util.Log.d("FocusActivity", "FishBones updated to: " + fishBones);
        }

        // โหลดของตกแต่งใหม่
        if (roomContainer != null) {
            int childCount = roomContainer.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View child = roomContainer.getChildAt(i);
                if (child instanceof ImageView && child.getId() == View.NO_ID) {
                    roomContainer.removeViewAt(i);
                }
            }
            loadHomeDecorations();
        }

        loadHomeImage();
        startHungerDecrease();

        if (focusButton != null) {
            if (hungerLevel <= 0) {
                focusButton.setEnabled(false);
                focusButton.setAlpha(0.5f);
            } else {
                focusButton.setEnabled(true);
                focusButton.setAlpha(1.0f);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // หยุดการลดหลอดอาหาร
        stopHungerDecrease();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (moveX != null) {
            moveX.cancel();
        }
        if (moveY != null) {
            moveY.cancel();
        }
        if (onBackPressedCallback != null) {
            onBackPressedCallback.remove();
        }
    }
}

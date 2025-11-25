package com.example.meowfocus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ShakeActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float acceleration = 0f;
    private float currentAcceleration = 0f;
    private float lastAcceleration = 0f;

    private ImageView boxClosedImage, boxOpenImage, catImage;
    private ConfettiView confettiView;
    private TextView titleText, hintText, catNameText;
    private LinearLayout nameInputLayout;
    private EditText catNameInput;
    private Button confirmNameButton;

    private boolean hasShaken = false;
    private SharedPreferences sharedPreferences;
    private CatModel randomCat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ ตรวจสอบว่ามีแมวอยู่แล้วหรือไม่
        sharedPreferences = getSharedPreferences("MeowFocusPrefs", Context.MODE_PRIVATE);
        String existingCatId = sharedPreferences.getString("selected_cat_id", null);

        if (existingCatId != null && !existingCatId.isEmpty()) {
            // ✅ มีแมวอยู่แล้ว ไปหน้า Focus เลย (ไม่ต้องสุ่มใหม่)
            Intent intent = new Intent(ShakeActivity.this, FocusActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_shake);

        // Initialize views
        boxClosedImage = findViewById(R.id.boxClosedImage);
        boxOpenImage = findViewById(R.id.boxOpenImage);
        catImage = findViewById(R.id.catImage);
        titleText = findViewById(R.id.titleText);
        hintText = findViewById(R.id.hintText);
        catNameText = findViewById(R.id.catNameText);
        nameInputLayout = findViewById(R.id.nameInputLayout);
        catNameInput = findViewById(R.id.catNameInput);
        confirmNameButton = findViewById(R.id.confirmNameButton);

        // Initialize sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        // Confirm button
        confirmNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCatName();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;

        if (acceleration > 12 && !hasShaken) {
            hasShaken = true;
            onShakeDetected();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    private void onShakeDetected() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        boxClosedImage.startAnimation(shake);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openBox();
            }
        }, 600);
    }

    private void openBox() {
        titleText.setText("TA-DA!");
        hintText.setVisibility(View.GONE);

        Animation boxOpen = AnimationUtils.loadAnimation(this, R.anim.box_open);
        boxClosedImage.startAnimation(boxOpen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boxClosedImage.setVisibility(View.GONE);
                boxOpenImage.setVisibility(View.VISIBLE);
                showCat();
            }
        }, 500);
    }

    private void showCat() {
        // ✅ สุ่มแมวใหม่ (เฉพาะครั้งแรก)
        randomCat = CatManager.getOrCreateRandomCat(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boxOpenImage.setVisibility(View.GONE);
                catImage.setVisibility(View.VISIBLE);
                catNameText.setVisibility(View.VISIBLE);

                int catImageId = getResources().getIdentifier(randomCat.getBoxImage(), "drawable", getPackageName());
                if (catImageId != 0) {
                    catImage.setImageResource(catImageId);
                } else {
                    catImageId = getResources().getIdentifier(randomCat.getSitImage(), "drawable", getPackageName());
                    catImage.setImageResource(catImageId);
                }

                Animation catScaleUp = AnimationUtils.loadAnimation(ShakeActivity.this, R.anim.cat_scale_up);
                catImage.startAnimation(catScaleUp);

                showNameInput();
            }
        }, 600);
    }

    private void showNameInput() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                nameInputLayout.setVisibility(View.VISIBLE);
                Animation fadeIn = AnimationUtils.loadAnimation(ShakeActivity.this, android.R.anim.fade_in);
                nameInputLayout.startAnimation(fadeIn);
            }
        }, 2000);
    }

    private void saveCatName() {
        String catName = catNameInput.getText().toString().trim();

        if (catName.isEmpty()) {
            Toast.makeText(this, "Please enter a name!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ บันทึกข้อมูลแมว
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cat_name", catName);
        editor.putString("selected_cat_id", randomCat.getCatId());
        editor.apply();

        // Navigate to Focus Activity
        Intent intent = new Intent(ShakeActivity.this, FocusActivity.class);
        startActivity(intent);
        finish();
    }
}

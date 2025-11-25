package com.example.meowfocus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ ตรวจสอบว่ามีแมวอยู่แล้วหรือไม่
        SharedPreferences prefs = getSharedPreferences("MeowFocusPrefs", Context.MODE_PRIVATE);
        String existingCatId = prefs.getString("selected_cat_id", null);

        if (existingCatId != null && !existingCatId.isEmpty()) {
            // ✅ มีแมวอยู่แล้ว ไปหน้า Focus เลย (ไม่ต้อง Login ใหม่)
            Intent intent = new Intent(MainActivity.this, FocusActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ✅ ไม่ลบข้อมูล - ไปหน้า Shake เพื่อสุ่มแมว
                Intent intent = new Intent(MainActivity.this, ShakeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

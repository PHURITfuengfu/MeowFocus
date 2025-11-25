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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Locale;

public class ShopActivity extends AppCompatActivity {

    private ImageView btnBackShop;
    private TextView tvCoinsShop;
    private CardView cardCatTree, cardLamp;
    private Button btnPurchase;
    private SharedPreferences sharedPreferences;

    private String selectedItem = "";
    private int selectedItemPrice = 0;
    private int coins = 0;

    private String currentLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLanguagePreference();
        setAppLocale(currentLanguage);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Initialize views
        btnBackShop = findViewById(R.id.btnBackShop);
        tvCoinsShop = findViewById(R.id.tvCoinsShop);
        cardCatTree = findViewById(R.id.cardCatTree);
        cardLamp = findViewById(R.id.cardLamp);
        btnPurchase = findViewById(R.id.btnPurchase);

        sharedPreferences = getSharedPreferences("MeowFocusPrefs", MODE_PRIVATE);

        // โหลด coin จาก SharedPreferences (ค่าเริ่มต้น 5)
        coins = sharedPreferences.getInt("coins", 5);
        tvCoinsShop.setText(String.valueOf(coins));

        updateItemsStatus();

        // ปุ่มกลับ
        btnBackShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // คลิกที่ Cat Tree
        cardCatTree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem("cat_tree", 5, cardCatTree);
            }
        });

        // คลิกโคมไฟ
        cardLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem("lamp", 5, cardLamp);
            }
        });

        // ปุ่มซื้อ
        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseItem();
            }
        });
    }

    // ✅ เพิ่ม onResume เพื่อรีเฟรชข้อมูลเหรียญเมื่อกลับมาหน้านี้
    @Override
    protected void onResume() {
        super.onResume();
        coins = sharedPreferences.getInt("coins", 5);
        tvCoinsShop.setText(String.valueOf(coins));
    }

    private void loadLanguagePreference() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        currentLanguage = prefs.getString("My_Lang", "en");
    }

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

    private void updateItemsStatus() {
        // ตรวจสอบ Cat Tree
        boolean hasCatTree = sharedPreferences.getBoolean("purchased_cat_tree", false);
        if (hasCatTree) {
            cardCatTree.setCardBackgroundColor(Color.parseColor("#A0A0A0"));
            cardCatTree.setAlpha(0.5f);
            cardCatTree.setEnabled(false);
        }

        // ตรวจสอบโคมไฟ
        boolean hasLamp = sharedPreferences.getBoolean("purchased_lamp", false);
        if (hasLamp) {
            cardLamp.setCardBackgroundColor(Color.parseColor("#A0A0A0"));
            cardLamp.setAlpha(0.5f);
            cardLamp.setEnabled(false);
        }
    }

    private void selectItem(String itemName, int price, CardView selectedCard) {
        boolean isPurchased = sharedPreferences.getBoolean("purchased_" + itemName, false);
        if (isPurchased) {
            Toast.makeText(this, R.string.already_purchased, Toast.LENGTH_SHORT).show();
            return;
        }

        // รีเซ็ตสีเดิม (เฉพาะของที่ยังไม่ได้ซื้อ)
        if (!sharedPreferences.getBoolean("purchased_cat_tree", false)) {
            cardCatTree.setCardBackgroundColor(Color.parseColor("#E8D5C4"));
        }

        if (!sharedPreferences.getBoolean("purchased_lamp", false)) {
            cardLamp.setCardBackgroundColor(Color.parseColor("#E8D5C4"));
        }

        // ไฮไลต์ที่เลือก
        selectedCard.setCardBackgroundColor(Color.parseColor("#CA9158"));
        selectedItem = itemName;
        selectedItemPrice = price;
        btnPurchase.setEnabled(true);
    }

    private void purchaseItem() {
        if (selectedItem.isEmpty()) {
            Toast.makeText(this, R.string.select_item_first, Toast.LENGTH_SHORT).show();
            return;
        }

        if (coins < selectedItemPrice) {
            Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_SHORT).show();
            return;
        }

        // หักเหรียญ
        coins -= selectedItemPrice;

        // บันทึกการซื้อ
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("coins", coins);
        editor.putBoolean("purchased_" + selectedItem, true);

        // ตรวจสอบว่ามีของอะไรบ้าง
        boolean hasCatTree = sharedPreferences.getBoolean("purchased_cat_tree", false)
                || selectedItem.equals("cat_tree");
        boolean hasLamp = sharedPreferences.getBoolean("purchased_lamp", false)
                || selectedItem.equals("lamp");

        // เลือกภาพบ้านตามของที่มี
        String homeDrawable;
        if (hasCatTree && hasLamp) {
            homeDrawable = "ic_home_all";
        } else if (hasCatTree) {
            homeDrawable = "ic_home_cat_tree";
        } else if (hasLamp) {
            homeDrawable = "ic_home_lamp";
        } else {
            homeDrawable = "ic_home";
        }

        // บันทึกภาพบ้านที่เลือก
        editor.putString("selected_home", homeDrawable);
        editor.apply();

        // อัปเดต UI
        tvCoinsShop.setText(String.valueOf(coins));

        Toast.makeText(this, R.string.purchase_success, Toast.LENGTH_SHORT).show();

        // รีเซ็ต
        selectedItem = "";
        selectedItemPrice = 0;
        btnPurchase.setEnabled(false);

        // อัพเดทสถานะสินค้าหลังซื้อ
        updateItemsStatus();

        // ปิดหน้าและกลับไป FocusActivity
        finish();
    }
}

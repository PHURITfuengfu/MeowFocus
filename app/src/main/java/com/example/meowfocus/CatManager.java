package com.example.meowfocus;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CatManager {
    private static final String PREF_NAME = "MeowFocusPrefs";
    private static final String KEY_SELECTED_CAT_ID = "selected_cat_id";
    private static final String KEY_CAT_NAME = "cat_name";

    private static List<CatModel> getAllCats() {
        List<CatModel> cats = new ArrayList<>();

        // แมวสีขาวดำ
        cats.add(new CatModel(
                "catdora",
                "Dora Cat",
                "cat_walk_dora",
                "sit_catdora_frame",
                "ic_hungry_catdora",
                "ic_catdora_in_box",
                "catdora_frame1"
        ));

        // แมวสีดำ
        cats.add(new CatModel(
                "catblack",
                "Black Cat",
                "cat_walk_black",
                "sit_catblack_frame",
                "ic_hungry_catblack",
                "ic_catblack_in_box",
                "catblack_frame1"
        ));

        // แมวสีเทา
        cats.add(new CatModel(
                "catgray",
                "Gray Cat",
                "cat_walk_gray",
                "sit_catgray_frame",
                "ic_hungry_catgray",
                "ic_catgray_in_box",
                "catgray_frame1"
        ));

        // แมวสีส้ม
        cats.add(new CatModel(
                "catorange",
                "Orange Cat",
                "cat_walk_orange",
                "sit_catorange_frame",
                "ic_hungry_catorange",
                "ic_catorange_in_box",
                "catorange_frame1"
        ));

        // แมวสีขาว
        cats.add(new CatModel(
                "catwhite",
                "White Cat",
                "cat_walk_white",
                "sit_catwhite_frame",
                "ic_hungry_catwhite",
                "ic_catwhite_in_box",
                "catwhite_frame1"
        ));

        return cats;
    }


    public static CatModel getOrCreateRandomCat(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedCatId = prefs.getString(KEY_SELECTED_CAT_ID, null);

        // ✅ Debug: ตรวจสอบว่ามีแมวเก่าหรือไม่
        android.util.Log.d("CatManager", "Saved Cat ID: " + (savedCatId != null ? savedCatId : "NULL (will random)"));

        // ถ้ายังไม่เคยสุ่ม ให้สุ่มแมวใหม่
        if (savedCatId == null) {
            List<CatModel> allCats = getAllCats();

            // ✅ Debug: แสดงจำนวนแมวทั้งหมด
            android.util.Log.d("CatManager", "Total cats available: " + allCats.size());

            Random random = new Random();
            int randomIndex = random.nextInt(allCats.size());
            CatModel randomCat = allCats.get(randomIndex);

            // ✅ Debug: แสดงแมวที่สุ่มได้
            android.util.Log.d("CatManager", "Randomly selected index: " + randomIndex + " -> Cat ID: " + randomCat.getCatId());

            // บันทึก ID ของแมวที่สุ่มได้
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_SELECTED_CAT_ID, randomCat.getCatId());
            editor.putString(KEY_CAT_NAME, randomCat.getCatName());
            editor.apply();

            android.util.Log.d("CatManager", "✅ Saved new cat to SharedPreferences");

            return randomCat;
        }

        // ✅ Debug: ใช้แมวเก่า
        android.util.Log.d("CatManager", "Using existing cat: " + savedCatId);

        // ถ้ามีแล้ว ดึงแมวตัวเดิมมา
        return getCatById(context, savedCatId);
    }

    public static CatModel getCatById(Context context, String catId) {
        List<CatModel> allCats = getAllCats();

        for (CatModel cat : allCats) {
            if (cat.getCatId().equals(catId)) {
                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String savedCatName = prefs.getString(KEY_CAT_NAME, cat.getCatName());
                cat.setCatName(savedCatName);
                return cat;
            }
        }

        return allCats.get(4);
    }

    public static CatModel getCurrentCat(Context context) {
        return getOrCreateRandomCat(context);
    }

    public static void resetCat(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_SELECTED_CAT_ID);
        editor.remove(KEY_CAT_NAME);
        editor.apply();
    }
}

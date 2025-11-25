package com.example.meowfocus;

public class CatModel {
    private String catId;
    private String catName;
    private String walkAnimation;
    private String sitImage;
    private String hungryImage;
    private String boxImage;
    private String walkFrame1;  // ✅ เพิ่มภาพเฟรม 1 ของการเดิน

    // Constructor
    public CatModel(String catId, String catName, String walkAnimation,
                    String sitImage, String hungryImage, String boxImage, String walkFrame1) {
        this.catId = catId;
        this.catName = catName;
        this.walkAnimation = walkAnimation;
        this.sitImage = sitImage;
        this.hungryImage = hungryImage;
        this.boxImage = boxImage;
        this.walkFrame1 = walkFrame1;
    }

    // Getters
    public String getCatId() { return catId; }
    public String getCatName() { return catName; }
    public String getWalkAnimation() { return walkAnimation; }
    public String getSitImage() { return sitImage; }
    public String getHungryImage() { return hungryImage; }
    public String getBoxImage() { return boxImage; }
    public String getWalkFrame1() { return walkFrame1; }  // ✅ เพิ่ม getter

    // Setters
    public void setCatName(String catName) { this.catName = catName; }
}

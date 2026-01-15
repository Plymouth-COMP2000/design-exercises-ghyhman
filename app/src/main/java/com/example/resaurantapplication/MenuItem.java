package com.example.resaurantapplication;

public class MenuItem {
    public long itemId;
    public String itemName;
    public double itemPrice;
    public String itemDesc;
    public String imgPath;
    public MenuItem() {
    }
    public MenuItem(long itemId, String itemName, double itemPrice, String itemDesc, String imgPath) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemDesc = itemDesc;
        this.imgPath = imgPath;
    }
}

package com.rituraj.shivamkiranastore.Models;

public class CartItem {
    private String id;
    private String name;
    private String brand;
    private String weight;
    private double price;
    private int quantity;
    private String imageUrl;
    private int discount;

    public CartItem() {
    }

    public CartItem(String id, String name, String brand, String weight,
                    double price, int quantity, String imageUrl, int discount) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.weight = weight;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.discount = discount;
    }

    public CartItem(String id, String name, String img, double price, int quantity, String weight) {
        this.id = id;
        this.name = name;
        this.imageUrl = img;
        this.price = price;
        this.quantity = quantity;
        this.weight = weight;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}

package com.rituraj.shivamkiranastore.Models;

public class Product {
    private String productId;
    private String productName;
    private String productNameHindi;
    private String productDescription;
    private String category;
    // Pricing
    private double price;
    private double mrp;
    private double discount;
    // Image
    private String productImageUrl;
    // Product Details
    private String weight;
    private String weightUnit;
    private String brand;
    private String shelfLife;
    private String offerType;

    public Product() {
    }

    public Product(String productName, String productNameHindi, String productDescription, String category, double price, double mrp, double discount, String productImageUrl, String weight, String weightUnit, String brand, String shelfLife) {
        this.productName = productName;
        this.productNameHindi = productNameHindi;
        this.productDescription = productDescription;
        this.category = category;
        this.price = price;
        this.mrp = mrp;
        this.discount = discount;
        this.productImageUrl = productImageUrl;
        this.weight = weight;
        this.weightUnit = weightUnit;
        this.brand = brand;
        this.shelfLife = shelfLife;
    }

    public Product(String productName, String productNameHindi, String productDescription, String category, double price, double mrp, double discount, String productImageUrl, String weight, String weightUnit, String brand, String shelfLife, String offerType) {
        this.productName = productName;
        this.productNameHindi = productNameHindi;
        this.productDescription = productDescription;
        this.category = category;
        this.price = price;
        this.mrp = mrp;
        this.discount = discount;
        this.productImageUrl = productImageUrl;
        this.weight = weight;
        this.weightUnit = weightUnit;
        this.brand = brand;
        this.shelfLife = shelfLife;
        this.offerType = offerType;
    }

    // Calculate discount based on MRP and selling price
    private void calculateDiscount() {
        if (mrp > 0 && price > 0 && mrp > price) {
            this.discount = ((mrp - price) / mrp) * 100;
            // Round to 1 decimal place
            this.discount = Math.round(discount * 10) / 10.0;
        } else {
            this.discount = 0;
        }
    }

    // Get formatted weight (e.g., "500 g")
    public String getFormattedWeight() {
        if (weight != null && weightUnit != null) {
            return weight + " " + weightUnit;
        }
        return "";
    }

    // Get discount percentage as string
    public String getDiscountString() {
        if (discount > 0) {
            return (int) discount + "% off";
        }
        return "";
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        calculateDiscount();
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
        calculateDiscount();
    }

    public double getDiscount() {
        return discount;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getProductNameHindi() {
        return productNameHindi;
    }

    public void setProductNameHindi(String productNameHindi) {
        this.productNameHindi = productNameHindi;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(String shelfLife) {
        this.shelfLife = shelfLife;
    }

    public String getOfferType() {
        return offerType;
    }

    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    @Override
    public String toString() {
        return "Product{" + "productId='" + productId + '\'' + ", productName='" + productName + '\'' + ", price=" + price + ", category=" + category + '}';
    }
}

package com.rituraj.shivamkiranastore.Models;

public class HeroImage {
    private String imageId, imageUrl;
    private int image;

    public HeroImage() {
    }

    public HeroImage(String imageId, String imageUrl) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
    }

    public HeroImage(int image){
        this.image = image;
    }
    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getImage() {
        return image;
    }

    public void setImage(Integer image) {
        this.image = image;
    }
}

package com.rituraj.shivamkiranastore.Models;

public class AddressModel {
    public String id;
    public String type;
    public String fullName;
    public String phone;
    public String alternatePhone;
    public String pincode;
    public String addressLine1;
    public String addressLine2;
    public String city;
    public String state;

    public AddressModel() {
    }

    public AddressModel(String type, String fullName, String phone, String alternatePhone,
                        String pincode, String addressLine1, String addressLine2,
                        String city, String state) {
        this.type = type;
        this.fullName = fullName;
        this.phone = phone;
        this.alternatePhone = alternatePhone;
        this.pincode = pincode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAlternatePhone() {
        return alternatePhone;
    }

    public void setAlternatePhone(String alternatePhone) {
        this.alternatePhone = alternatePhone;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return fullName + "\n" +
                addressLine1 + ", " + addressLine2 + "\n" +
                city + ", " + state + " - " + pincode + "\n" +
                "Phone: " + phone;
    }
}

package com.rituraj.shivamkiranastore.Models;

import java.util.ArrayList;
import java.util.List;

public class OrderModel {
    private String orderId;
    private String orderDate;
    private String orderStatus;
    private String orderStatusColor;
    private double subtotal;
    private double deliveryCharge;
    private double itemSize;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private AddressModel deliveryAddress;
    private CartItem items;
    private String estimatedDelivery;
    private String trackingNumber;

    public OrderModel() {
    }

    public enum OrderStatus {
        PENDING("Pending"), CONFIRMED("Confirmed"), PROCESSING("Processing"), SHIPPED("Shipped"), OUT_FOR_DELIVERY("Out for Delivery"), DELIVERED("Delivered"), CANCELLED("Cancelled"), RETURNED("Returned");

        private final String status;

        OrderStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    public enum PaymentMethod {
        CASH_ON_DELIVERY("Cash on Delivery"), CREDIT_CARD("Credit Card"), DEBIT_CARD("Debit Card"), UPI("UPI"), NET_BANKING("Net Banking"), QR_PAYMENT("QR Payment"), WALLET("Wallet");

        private final String method;

        PaymentMethod(String method) {
            this.method = method;
        }

        public String getMethod() {
            return method;
        }
    }

    public OrderModel(String orderId, String orderDate, String orderStatus, double subtotal, double deliveryCharge, double size, String paymentMethod, String paymentStatus, AddressModel deliveryAddress, CartItem items) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.subtotal = subtotal;
        this.deliveryCharge = deliveryCharge;
        this.itemSize = size;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.deliveryAddress = deliveryAddress;
        this.items = items;
    }

    public OrderModel(String orderId, String orderDate, String orderStatus, double totalAmount, double deliveryCharge, String paymentMethod, String image, String productName) {

        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.deliveryCharge = deliveryCharge;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatusColor() {
        return orderStatusColor;
    }

    public void setOrderStatusColor(String orderStatusColor) {
        this.orderStatusColor = orderStatusColor;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(double deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public double getItemSize() {
        return itemSize;
    }

    public void setItemSize(double itemSize) {
        this.itemSize = itemSize;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public AddressModel getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(AddressModel deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public CartItem getItems() {
        return items;
    }

    public void setItems(CartItem items) {
        this.items = items;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}

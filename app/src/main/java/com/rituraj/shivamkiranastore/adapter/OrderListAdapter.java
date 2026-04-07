package com.rituraj.shivamkiranastore.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.Models.OrderModel;
import com.rituraj.shivamkiranastore.ui.admin.AdminDashboardActivity;
import com.rituraj.shivamkiranastore.ui.product.ProductActivity;

import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {
    public Context context;
    private List<OrderModel> orderList;
    private OnOrderActionListener listener;
    private String email;

    public interface OnOrderActionListener {
        void onOrderClick(OrderModel order, int position);

        void onTrackOrder(OrderModel order, int position);

        void onReturnOrder(OrderModel order, int position);

        void onReorder(OrderModel order, int position);

        void onCancelOrder(OrderModel order, int position);

        void onDeliveredOrder(OrderModel order, int position);
    }

    public OrderListAdapter(Context context, List<OrderModel> orderList, OnOrderActionListener listener, String email) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
        this.email = email;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_list, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.id = order.getItems().getId();
        // Order details
        holder.tvOrderId.setText("Order #" + order.getOrderId());
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvTotalAmount.setText(String.format("₹%.2f", order.getItems().getTotalPrice()));
        int itemCount = (int) ((order.getItems() != null) ? order.getItemSize() : 0);
        holder.tvItemCount.setText(itemCount + " items");
        holder.tvPaymentMethod.setText(order.getPaymentMethod());
        holder.tvDeliveryAddress.setText(order.getDeliveryAddress().toString());

        // Product preview
        holder.tvProductName.setText(order.getItems().getName());
        Glide.with(context).load(decodeBase64ToImage(order.getItems().getImageUrl())).placeholder(R.drawable.product_placeholder).error(R.drawable.product_placeholder).into(holder.ivProductImage);

        // Order status with color
        holder.tvOrderStatus.setText(order.getOrderStatus());
        if (order.getOrderStatusColor() != null) {
            holder.tvOrderStatus.setTextColor(Color.parseColor(order.getOrderStatusColor()));
        }

        // Delivery date if available
        if (order.getEstimatedDelivery() != null && !order.getEstimatedDelivery().isEmpty()) {
            holder.tvDeliveryDate.setVisibility(View.VISIBLE);
            holder.tvDeliveryDate.setText(order.getEstimatedDelivery());
        } else {
            holder.tvDeliveryDate.setVisibility(View.GONE);
        }

        // Action buttons based on order status
        setupActionButtons(holder, order, position);

        // Item click
        holder.cardOrder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order, position);
            }
        });
    }

    private Bitmap decodeBase64ToImage(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void setupActionButtons(OrderViewHolder holder, OrderModel order, int position) {
        // Track button
//        if (order.isTrackable()) {
//            holder.btnTrack.setVisibility(View.VISIBLE);
//            holder.btnTrack.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onTrackOrder(order, position);
//                }
//            });
//        } else {
//            holder.btnTrack.setVisibility(View.GONE);
//        }
//
//         Return button
//        if (order.isReturnable()) {
//            holder.btnReturn.setVisibility(View.VISIBLE);
//            holder.btnReturn.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onReturnOrder(order, position);
//                }
//            });
//        } else {
//            holder.btnReturn.setVisibility(View.GONE);
//        }

        if (email.equals("riturajkumar1105@gmail.com") || email.equals("shivamkumarjkt@gmail.com") || email.equals("shubham756291@gmail.com"))
            holder.btnDelivered.setVisibility(View.VISIBLE);
        else
            holder.btnDelivered.setVisibility(View.GONE);

        if (!order.getOrderStatus().equals("Delivered")) {
            holder.btnDelivered.setVisibility(View.VISIBLE);
            holder.btnDelivered.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeliveredOrder(order, position);
                }
            });
        } else
            holder.btnDelivered.setVisibility(View.GONE);

        if (!order.getOrderStatus().equals("Cancelled")) {
            holder.btnReorder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReorder(order, position);
                }
            });
        } else
            holder.btnDelivered.setVisibility(View.GONE);

        // Cancel button (only for active orders)
        if (order.getOrderStatus().equals("Processing") || order.getOrderStatus().equals("Confirmed")) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelOrder(order, position);
                }
            });
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        public String id;
        CardView cardOrder;
        ImageView ivProductImage;
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvTotalAmount;
        TextView tvItemCount, tvPaymentMethod, tvProductName, tvDeliveryDate;
        TextView btnDelivered, btnTrack, btnReturn, btnReorder, btnCancel;
        TextView tvDeliveryAddress;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            cardOrder = itemView.findViewById(R.id.cardOrder);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvDeliveryDate = itemView.findViewById(R.id.tvDeliveryDate);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            btnTrack = itemView.findViewById(R.id.btnTrack);
            btnReturn = itemView.findViewById(R.id.btnReturn);
            btnDelivered = itemView.findViewById(R.id.btnDelivered);
            btnReorder = itemView.findViewById(R.id.btnReorder);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}

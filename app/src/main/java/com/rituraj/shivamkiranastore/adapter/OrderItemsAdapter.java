package com.rituraj.shivamkiranastore.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.Models.CartItem;
import com.rituraj.shivamkiranastore.ui.product.ProductActivity;

import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {
    private Context context;
    private List<CartItem> itemList;

    public OrderItemsAdapter(Context context, List<CartItem> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        holder.tvProductName.setText(item.getName());
        holder.tvQuantity.setText(String.format("Qty: %d %s", item.getQuantity(), item.getWeight()));
        holder.tvPrice.setText(String.format("₹%.2f", item.getPrice()));
        holder.tvTotal.setText(String.format("₹%.2f", item.getTotalPrice()));

        Glide.with(context)
                .load(decodeBase64ToImage(item.getImageUrl()))
                .placeholder(R.drawable.product_placeholder)
                .error(R.drawable.product_placeholder)
                .into(holder.ivProductImage);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private Bitmap decodeBase64ToImage(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvQuantity, tvPrice, tvTotal;

        OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
}

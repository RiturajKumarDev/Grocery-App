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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.shivamkiranastore.Models.CartItem;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.ui.product.ProductActivity;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemClickListener listener;
    private DecimalFormat df = new DecimalFormat("#.##");
    private FirebaseDatabase database;
    private String userId;

    public interface OnCartItemClickListener {
        void onQuantityChanged();

        void onItemRemoved(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemClickListener listener, String userId) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
        this.userId = userId;
        database = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    private Bitmap decodeBase64ToImage(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public String id;
        private TextView tvProductName, tvProductBrand, tvProductWeight;
        private TextView tvProductPrice, tvQuantity;
        private ImageView ivRemoveItem, ivDecreaseQty, ivIncreaseQty, ivProductImage;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductBrand = itemView.findViewById(R.id.tvProductBrand);
            tvProductWeight = itemView.findViewById(R.id.tvProductWeight);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            ivRemoveItem = itemView.findViewById(R.id.ivRemoveItem);
            ivDecreaseQty = itemView.findViewById(R.id.ivDecreaseQty);
            ivIncreaseQty = itemView.findViewById(R.id.ivIncreaseQty);
        }

        public void bind(CartItem item) {
            id = item.getId();
            tvProductName.setText(item.getName());
            tvProductBrand.setText(item.getBrand());
            tvProductWeight.setText(item.getWeight());
            tvProductPrice.setText(String.valueOf("₹" + df.format(item.getPrice())));
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            Glide.with(context)
                    .load(decodeBase64ToImage(item.getImageUrl()))
                    .placeholder(R.drawable.product_placeholder)
                    .error(R.drawable.product_placeholder)
                    .into(ivProductImage);

            // Handle quantity decrease
            ivDecreaseQty.setOnClickListener(v -> {
                int currentQty = item.getQuantity();
                if (currentQty > 1) {
                    item.setQuantity(currentQty - 1);
                    tvQuantity.setText(String.valueOf(item.getQuantity()));
                    database.getReference("Carts").child(userId).child(item.getId()).child("quantity").setValue(item.getQuantity());
                    listener.onQuantityChanged();
                } else {
                    Toast.makeText(v.getContext(),
                            "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            });

            // Handle quantity increase
            ivIncreaseQty.setOnClickListener(v -> {
                int currentQty = item.getQuantity();
                if (currentQty < 10) {
                    item.setQuantity(currentQty + 1);
                    tvQuantity.setText(String.valueOf(item.getQuantity()));
                    database.getReference("Carts").child(userId).child(item.getId()).child("quantity").setValue(item.getQuantity());
                    listener.onQuantityChanged();
                } else {
                    Toast.makeText(v.getContext(),
                            "Maximum quantity is 10", Toast.LENGTH_SHORT).show();
                }
            });

            // Handle item removal
            ivRemoveItem.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CartItem removedItem = cartItems.get(position);
                    cartItems.remove(position);
                    notifyItemRemoved(position);
                    database.getReference("Carts").child(userId).child(item.getId()).removeValue();
                    listener.onItemRemoved(removedItem);
                    Toast.makeText(v.getContext(),
                            item.getName() + " removed from cart", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ProductActivity.class);
            intent.putExtra("productId", id);
            context.startActivity(intent);
        }
    }
}

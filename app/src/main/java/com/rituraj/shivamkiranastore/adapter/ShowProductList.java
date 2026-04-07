package com.rituraj.shivamkiranastore.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.shivamkiranastore.MainActivity;
import com.rituraj.shivamkiranastore.Models.CartItem;
import com.rituraj.shivamkiranastore.Models.Product;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.ui.product.ProductActivity;

import java.util.ArrayList;

public class ShowProductList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<Product> products;
    private FirebaseDatabase database;
    private String userId;

    public ShowProductList(Context context, ArrayList<Product> products, String userId) {
        this.context = context;
        this.products = products;
        database = FirebaseDatabase.getInstance();
        this.userId = userId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Product product = products.get(position);
        ViewHolder vh = (ViewHolder) holder;

        vh.productId = product.getProductId();
        vh.productOfferType = product.getOfferType();
        vh.productName.setText(product.getProductName());
        vh.productNameHindi.setText(product.getProductNameHindi());
        vh.cardProductWeight.setText(product.getFormattedWeight());
        vh.cardProductSellingPrice.setText(String.valueOf("₹" + product.getPrice()));
        vh.cardProductCategory.setText(product.getCategory());
        vh.cardProductDiscountOffer.setText(String.valueOf(product.getDiscount() + "\n%OFF"));
        vh.cardProductOriginalPrice.setText(String.valueOf("₹" + product.getMrp()));

        Glide.with(context).load(decodeBase64ToImage(product.getProductImageUrl())).placeholder(R.drawable.product_placeholder).error(R.drawable.product_placeholder).into(vh.cardProductImage);

        vh.cardAddToCartBtn.setOnClickListener((v) -> {
            Toast.makeText(context, "Add To Cart", Toast.LENGTH_SHORT).show();
            CartItem cartItem = new CartItem(product.getProductId(), product.getProductName(),
                    product.getBrand(), product.getFormattedWeight(), product.getPrice(), 1, product.getProductImageUrl(), (int) product.getDiscount());
            database.getReference().child("Carts").child(userId).child(product.getProductId()).setValue(cartItem);
        });
        vh.cardBuyNowBtn.setOnClickListener((v) -> {
            Toast.makeText(context, "Buy Now", Toast.LENGTH_SHORT).show();
        });
    }

    private Bitmap decodeBase64ToImage(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public String productId, productOfferType;
        public TextView productName, productNameHindi, cardProductWeight, cardProductSellingPrice, cardProductCategory;
        public TextView cardProductDiscountOffer, cardProductOriginalPrice;
        public Button cardBuyNowBtn;
        public ImageButton cardAddToCartBtn;
        public ImageView cardProductImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            productName = itemView.findViewById(R.id.productSmallName);
            productNameHindi = itemView.findViewById(R.id.productSmallHindiName);
            cardProductWeight = itemView.findViewById(R.id.productSmallWeight);
            cardProductSellingPrice = itemView.findViewById(R.id.priceSmallCurrent);
            cardProductCategory = itemView.findViewById(R.id.categorySmallIndicator);
            cardProductDiscountOffer = itemView.findViewById(R.id.offerSmallBadge);
            cardProductOriginalPrice = itemView.findViewById(R.id.priceSmallOriginal);
            cardAddToCartBtn = itemView.findViewById(R.id.addToCartSmall);
            cardBuyNowBtn = itemView.findViewById(R.id.quickBuySmall);
            cardProductImage = itemView.findViewById(R.id.productSmallImage);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ProductActivity.class);
            intent.putExtra("productId", productId);
            context.startActivity(intent);
        }
    }
}

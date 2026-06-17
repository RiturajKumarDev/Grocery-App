package com.rituraj.shivamkiranastore.ui.product;

import static android.view.View.VISIBLE;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.MainActivity;
import com.rituraj.shivamkiranastore.Models.CartItem;
import com.rituraj.shivamkiranastore.Models.LanguageModel;
import com.rituraj.shivamkiranastore.Models.Product;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.adapter.ShowProductList;
import com.rituraj.shivamkiranastore.translationLanguage.LanguageManager;
import com.rituraj.shivamkiranastore.ui.address.ManageAddressActivity;
import com.rituraj.shivamkiranastore.ui.admin.AdminDashboardActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ProductActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private String productId, offer, email, userId;
    private ImageView productImage;
    private TextView productName, productHindiName;
    private TextView productSellingPrice, productSellingPrice2, productOriginalPrice, productSaveMoney, productOfferTextLarge;
    private TextView productBrand, productWeight, productWeight2, productType, productShelfLife, productDescription;
    private TextView productBottomAddToCart, productBottomBuyNow;
    private RecyclerView similarProductsRecyclerView;
    private ShowProductList similarProductsListAdapter;
    private ArrayList<Product> similarProducts = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_product);

        setupToolbar();
        init();
        fun();
        translationViews();
    }

    private void init() {
        productId = getIntent().getStringExtra("productId");
        offer = getIntent().getStringExtra("OFFER");
        database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        email = auth.getCurrentUser().getEmail();
        userId = auth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(ProductActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Product Details"); // Ensure title is set
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private LanguageModel getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String name = prefs.getString("language_name", "English");
        String code = prefs.getString("language_code", "en");

        return new LanguageModel(name, code);
    }

    private void translationViews() {
        LanguageManager.init(getSavedLanguage(ProductActivity.this).code, () -> LanguageManager.translateView(getWindow().getDecorView()));
    }

    private void fun() {
        initViews();
        if (offer == null)
            getProductDetails();
        else
            getOfferProductDetails();
        getSimilarProducts();

        MaterialCardView productAdminDeleteBtn = findViewById(R.id.productAdminDeleteBtn);
        if (email.equals("riturajkumar1105@gmail.com") || email.equals("shivamkumarjkt@gmail.com") || email.equals("shubham756291@gmail.com")) {
            productAdminDeleteBtn.setVisibility(VISIBLE);
            productAdminDeleteBtn.setOnClickListener((v) -> {
                Toast.makeText(ProductActivity.this, "Delete Product", Toast.LENGTH_SHORT).show();
                DatabaseReference productRef = database.getReference("Products").child(productId);
                productRef.removeValue().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DatabaseReference productRef = database.getReference("SpecialOffer").child(productId);
                        productRef.removeValue();
                        Toast.makeText(ProductActivity.this, "Product Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                finish();
            });
        }
    }

    private void initViews() {
        productImage = findViewById(R.id.productDetailImage);
        productOfferTextLarge = findViewById(R.id.productOfferTextLarge);

        productName = findViewById(R.id.productDetailName);
        productHindiName = findViewById(R.id.productHindiName);

        productSellingPrice = findViewById(R.id.productSellingPrice);
        productSellingPrice2 = findViewById(R.id.productSellingPrice2);
        productOriginalPrice = findViewById(R.id.productOriginalPrice);
        productSaveMoney = findViewById(R.id.productSaveMoney);

        productBrand = findViewById(R.id.productBrand);
        productWeight = findViewById(R.id.productWeight);
        productWeight2 = findViewById(R.id.productWeight2);
        productType = findViewById(R.id.productType);
        productShelfLife = findViewById(R.id.productShelfLife);
        productDescription = findViewById(R.id.productDescription);

        productBottomAddToCart = findViewById(R.id.productBottomAddToCart);
        productBottomBuyNow = findViewById(R.id.productBottomBuyNow);
    }

    private void getOfferProductDetails() {
        DatabaseReference productRef = database.getReference("SpecialOffer").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                if (!snapshot.exists()) {
                    Toast.makeText(ProductActivity.this, "Offer ended!!!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Product product = snapshot.getValue(Product.class);
                if (product == null) {
                    Toast.makeText(ProductActivity.this, "Offer ended!!!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                product.setProductId(snapshot.getKey());
                updateViews(product);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(ProductActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProductDetails() {
        DatabaseReference productRef = database.getReference("Products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                if (!snapshot.exists()) {
                    Toast.makeText(ProductActivity.this, "Offer ended!!!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Product product = snapshot.getValue(Product.class);
                if (product == null) {
                    Toast.makeText(ProductActivity.this, "Offer ended!!!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                product.setProductId(snapshot.getKey());
                updateViews(product);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(ProductActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateViews(Product product) {
        Glide.with(ProductActivity.this).load(decodeBase64ToImage(product.getProductImageUrl())).placeholder(R.drawable.product_placeholder).error(R.drawable.product_placeholder).into(productImage);

        productOfferTextLarge.setText(String.valueOf(product.getDiscount() + "%\nOFF"));
        productName.setText(product.getProductName());
        productHindiName.setText(product.getProductNameHindi());
        productSellingPrice.setText(String.valueOf("₹" + product.getPrice()));
        productSellingPrice2.setText(String.valueOf("₹" + product.getPrice()));
        productOriginalPrice.setText(String.valueOf("₹" + product.getMrp()));
        productWeight.setText(product.getFormattedWeight());
        productWeight2.setText(product.getFormattedWeight());
        productSaveMoney.setText(String.valueOf("Save ₹" + (product.getMrp() - product.getPrice())));
        productBrand.setText(product.getBrand());
        productType.setText(product.getCategory());
        productShelfLife.setText(product.getShelfLife());
        productDescription.setText(product.getProductDescription());
        productBottomAddToCart.setOnClickListener((v) -> {
            Toast.makeText(ProductActivity.this, "Add To Cart", Toast.LENGTH_SHORT).show();
            CartItem cartItem = new CartItem(product.getProductId(), product.getProductName(),
                    product.getBrand(), product.getFormattedWeight(), product.getPrice(), 1, product.getProductImageUrl(), (int) product.getDiscount());
            database.getReference("Carts").child(userId).child(product.getProductId()).setValue(cartItem);
        });

        productBottomBuyNow.setOnClickListener((v) -> {
            Toast.makeText(ProductActivity.this, "Buy Now", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProductActivity.this, ManageAddressActivity.class);
            intent.putExtra("productId", product.getProductId());
            startActivity(intent);
        });
    }

    private void getSimilarProducts() {
        showSimilarProducts();
        similarProducts.clear();
        DatabaseReference myReference = database.getReference().child("Products");
        myReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    product.setProductId(snapshot.getKey());
                    similarProducts.add(product);
                    Collections.shuffle(similarProducts);
                    similarProductsListAdapter.notifyItemInserted(similarProducts.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSimilarProducts() {
        similarProductsRecyclerView = findViewById(R.id.similarProductsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ProductActivity.this, LinearLayoutManager.HORIZONTAL, false);
        similarProductsRecyclerView.setLayoutManager(layoutManager);

        similarProductsListAdapter = new ShowProductList(ProductActivity.this, similarProducts, userId);
        similarProductsRecyclerView.setAdapter(similarProductsListAdapter);
    }

    private Bitmap decodeBase64ToImage(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
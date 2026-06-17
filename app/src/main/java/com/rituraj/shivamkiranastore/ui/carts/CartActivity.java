package com.rituraj.shivamkiranastore.ui.carts;

import static java.security.AccessController.getContext;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.Models.CartItem;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.adapter.CartAdapter;
import com.rituraj.shivamkiranastore.ui.address.ManageAddressActivity;
import com.rituraj.shivamkiranastore.ui.product.ProductActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemClickListener {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private Toolbar toolbar;
    private RecyclerView recyclerViewCart;
    private LinearLayout emptyCartLayout;
    private CardView checkoutBar;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private Button btnCheckout, btnShopNow;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private DecimalFormat df = new DecimalFormat("#.##");
    private String userId, cartId;

    // Constants
    private static final double DELIVERY_FEE = 40.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupStatusBar();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCartItems(); // Load sample data or get from intent
        setupClickListeners();
        updateCartVisibility();
        calculateTotals();
    }

    private void setupStatusBar() {
        Window window = getWindow();

        // Make status bar transparent (required for Android 15+)
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Create a view to act as the colored status bar background
        View statusBarBackground = new View(this);
        statusBarBackground.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight()
        ));
        statusBarBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.logo_blue));

        // Add the view to your root layout
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        decorView.addView(statusBarBackground);

        // Set status bar icons color (light or dark based on your blue)
        setStatusBarIconColor(isColorDark(ContextCompat.getColor(this, R.color.logo_blue)));
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return (int) Math.ceil(24 * getResources().getDisplayMetrics().density);
    }

    private void setStatusBarIconColor(boolean isDarkBackground) {
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        if (isDarkBackground) {
            // Dark background - use light icons
            controller.setAppearanceLightStatusBars(false);
        } else {
            // Light background - use dark icons
            controller.setAppearanceLightStatusBars(true);
        }
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * android.graphics.Color.red(color) +
                0.587 * android.graphics.Color.green(color) +
                0.114 * android.graphics.Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
    }

    private void initViews() {
        toolbar = findViewById(R.id.cartToolbar);
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        checkoutBar = findViewById(R.id.checkoutBar);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnShopNow = findViewById(R.id.btnShopNow);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(CartActivity.this, cartItems, this, userId);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        reference = database.getReference("Carts").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && getContext() != null) {
                    cartId = snapshot.getKey();
                } else
                    cartId = null;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists() && getContext() != null) {
                    CartItem cartItem = snapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItem.setId(snapshot.getKey());
                        cartItems.add(cartItem);
                        cartAdapter.notifyItemInserted(cartItems.size() - 1);
                        updateCartVisibility();
                        calculateTotals();
                    }
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
                Toast.makeText(CartActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnCheckout.setOnClickListener(v -> {
            if (!cartItems.isEmpty()) {
                Intent intent = new Intent(CartActivity.this, ManageAddressActivity.class);
                if (cartId != null) {
                    intent.putExtra("cartId", cartId);
                    startActivity(intent);
                } else
                    Toast.makeText(CartActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });

        btnShopNow.setOnClickListener(v -> {
            // Navigate to shopping page
            Toast.makeText(this, "Start shopping!", Toast.LENGTH_SHORT).show();
            finish(); // Go back to previous activity
        });
    }

    private void updateCartVisibility() {
        if (cartItems.isEmpty()) {
            emptyCartLayout.setVisibility(View.VISIBLE);
            recyclerViewCart.setVisibility(View.GONE);
            checkoutBar.setVisibility(View.GONE);
        } else {
            emptyCartLayout.setVisibility(View.GONE);
            recyclerViewCart.setVisibility(View.VISIBLE);
            checkoutBar.setVisibility(View.VISIBLE);
        }
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }

        double deliveryFee = DELIVERY_FEE;
        double total;
        if (subtotal >= 500) {
            deliveryFee = 0;
            total = subtotal;
        } else
            total = subtotal + DELIVERY_FEE;

        tvSubtotal.setText(String.valueOf("₹" + df.format(subtotal)));
        tvDeliveryFee.setText(String.valueOf("₹" + df.format(deliveryFee)));
        tvTotal.setText(String.valueOf("₹" + df.format(total)));

        // Update toolbar title with item count
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Cart (" + cartItems.size() + ")");
        }
    }

    // Method to add item to cart (called from product detail page)
    public void addItemToCart(CartItem newItem) {
        // Check if item already exists in cart
        for (CartItem item : cartItems) {
            if (item.getId().equals(newItem.getId())) {
                // Increase quantity if item exists
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                cartAdapter.notifyDataSetChanged();
                calculateTotals();
                updateCartVisibility();
                Toast.makeText(this, "Quantity updated in cart", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Add new item if not exists
        cartItems.add(newItem);
        cartAdapter.notifyItemInserted(cartItems.size() - 1);
        calculateTotals();
        updateCartVisibility();
        Toast.makeText(this, newItem.getName() + " added to cart", Toast.LENGTH_SHORT).show();
    }

    // Method to clear cart
    public void clearCart() {
        cartItems.clear();
        cartAdapter.notifyDataSetChanged();
        calculateTotals();
        updateCartVisibility();
    }

    @Override
    public void onQuantityChanged() {
        calculateTotals();
    }

    @Override
    public void onItemRemoved(CartItem item) {
        calculateTotals();
        updateCartVisibility();
    }
}
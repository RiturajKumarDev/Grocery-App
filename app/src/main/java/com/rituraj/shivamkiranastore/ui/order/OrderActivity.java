package com.rituraj.shivamkiranastore.ui.order;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.MainActivity;
import com.rituraj.shivamkiranastore.Models.CartItem;
import com.rituraj.shivamkiranastore.Models.Product;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.Models.AddressModel;
import com.rituraj.shivamkiranastore.Models.OrderModel;
import com.rituraj.shivamkiranastore.adapter.OrderItemsAdapter;
import com.rituraj.shivamkiranastore.ui.payment.PaymentActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class OrderActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    // Address Section
    private LinearLayout addressContainer;
    private TextView tvDeliveryAddress;
    private AddressModel selectedAddress;

    // Order Items Section
    private RecyclerView recyclerOrderItems;
    private OrderItemsAdapter orderItemsAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();

    // Payment Section
    private RadioGroup paymentMethodGroup;
    private RadioButton rbCod, rbQR, rbCard, rbUpi, rbNetbanking;
    private LinearLayout cardDetailsLayout, upiDetailsLayout;

    // Price Details
    private TextView tvSubtotal, tvDeliveryCharge, tvTax, tvTotalAmount;
    private double subtotal = 0.0;
    private double deliveryCharge = 40.0;
    private double tax = 0.0;
    private double totalAmount = 0.0;

    // Buttons
    private Button btnPlaceOrder;

    // Order Summary
    private LinearLayout orderSummaryLayout;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus;
    private Button btnTrackOrder, btnContinueShopping;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String productId, cartId, addressId;
    private String userId;
    private boolean isProduct = false, isCartItems = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.logo_blue));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initDatabase();
        initViews();
        setupToolbar();
        setupPaymentMethods();
        setupClickListeners();
        loadAddress();
    }

    private void initDatabase() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        productId = getIntent().getStringExtra("productId");
        cartId = getIntent().getStringExtra("cartId");
        addressId = getIntent().getStringExtra("addressId");
        if (productId != null && !productId.isEmpty()) {
            isProduct = true;
            getSingleProduct();
        } else if (cartId != null && !cartId.isEmpty()) {
            isCartItems = true;
            loadCartData();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        scrollView = findViewById(R.id.scrollView);
        progressBar = findViewById(R.id.progressBar);

        addressContainer = findViewById(R.id.addressContainer);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);

        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));

        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        rbCod = findViewById(R.id.rbCod);
        rbQR = findViewById(R.id.rbQR);
        rbCard = findViewById(R.id.rbCard);
        rbUpi = findViewById(R.id.rbUpi);
        rbNetbanking = findViewById(R.id.rbNetbanking);

        cardDetailsLayout = findViewById(R.id.cardDetailsLayout);
        upiDetailsLayout = findViewById(R.id.upiDetailsLayout);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryCharge = findViewById(R.id.tvDeliveryCharge);
        tvTax = findViewById(R.id.tvTax);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        orderSummaryLayout = findViewById(R.id.orderSummaryLayout);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        btnTrackOrder = findViewById(R.id.btnTrackOrder);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Review Order");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void getSingleProduct() {
        reference = database.getReference("Products").child(productId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = null;
                if (!snapshot.exists()) {
                    getSpecialProduct();
                    return;
                } else product = snapshot.getValue(Product.class);
                if (product == null) {
                    finish();
                    return;
                }
                product.setProductId(snapshot.getKey());
                cartItemList.add(new CartItem(product.getProductId(), product.getProductName(), product.getProductImageUrl(), product.getPrice(), 1, product.getFormattedWeight()));
                showProducts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSpecialProduct() {
        reference = database.getReference("SpecialOffer").child(productId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    finish();
                    return;
                }
                Product product = snapshot.getValue(Product.class);
                if (product == null) {
                    finish();
                    return;
                }
                product.setProductId(snapshot.getKey());
                cartItemList.add(new CartItem(product.getProductId(), product.getProductName(), product.getProductImageUrl(), product.getPrice(), 1, product.getFormattedWeight()));
                showProducts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCartData() {
        reference = database.getReference("Carts").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    finish();
                    return;
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = dataSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItem.setId(dataSnapshot.getKey());
                        cartItemList.add(cartItem);
                    }
                }
                showProducts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showProducts() {
        orderItemsAdapter = new OrderItemsAdapter(OrderActivity.this, cartItemList);
        recyclerOrderItems.setAdapter(orderItemsAdapter);
        calculatePrices();
    }

    private void loadAddress() {
        reference = database.getReference("Address").child(userId).child(addressId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    finish();
                    return;
                }
                AddressModel addressModel = snapshot.getValue(AddressModel.class);
                if (addressModel == null) {
                    finish();
                    return;
                }
                addressModel.setId(snapshot.getKey());
                selectedAddress = addressModel;
                tvDeliveryAddress.setText(addressModel.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupPaymentMethods() {
        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            cardDetailsLayout.setVisibility(View.GONE);
            upiDetailsLayout.setVisibility(View.GONE);

            if (checkedId == R.id.rbCard) {
                cardDetailsLayout.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbUpi) {
                upiDetailsLayout.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbQR) {
                showQrDialog();
            }
        });
    }

    private void calculatePrices() {
        subtotal = 0;
        for (CartItem item : cartItemList) {
            subtotal += item.getTotalPrice();
        }
        // Calculate tax (5% of subtotal for example)
//        tax = subtotal * 0.05;
        if (subtotal >= 500) {
            deliveryCharge = 0;
        }

        totalAmount = subtotal + deliveryCharge + tax;

        // Update UI
        tvSubtotal.setText(String.format("₹%.2f", subtotal));
        tvDeliveryCharge.setText(String.format("₹%.2f", deliveryCharge));
        tvTax.setText(String.format("₹%.2f", tax));
        tvTotalAmount.setText(String.format("₹%.2f", totalAmount));
    }

    private void setupClickListeners() {
        btnPlaceOrder.setOnClickListener(v -> {
            if (validateOrder()) {
                placeOrder();
            }
        });

        btnTrackOrder.setOnClickListener(v -> {
            Toast.makeText(this, "Track order", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(OrderActivity.this, OrderListActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });

        btnContinueShopping.setOnClickListener((v) -> {
            Intent intent = new Intent(OrderActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void showQrDialog() {
        new AlertDialog.Builder(this).setTitle("QR Payment").setMessage("Are you sure you want to pay now?").setPositiveButton("Yes", (dialog, which) -> {
            rbCod.setEnabled(false);
            rbQR.setEnabled(false);
            rbCard.setEnabled(false);
            rbUpi.setEnabled(false);
            rbNetbanking.setEnabled(false);
            Intent intent = new Intent(OrderActivity.this, PaymentActivity.class);
            intent.putExtra("Amount", String.valueOf(totalAmount));
            startActivity(intent);
        }).setNegativeButton("No", null).show();
    }

    private boolean validateOrder() {
        if (selectedAddress == null) {
            Toast.makeText(this, "Please select delivery address", Toast.LENGTH_SHORT).show();
            return false;
        }

        int selectedPaymentId = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Please select payment method", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void placeOrder() {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);

            ArrayList<OrderModel> orderModels = new ArrayList<>();
            cartItemList.size();
            for (int i = 0; i < cartItemList.size(); i++)
                orderModels.add(createOrder(cartItemList.get(i)));
            showOrderSummary(orderModels.get(0));
            if (isCartItems)
                database.getReference("Carts").child(userId).removeValue();
            // Hide loading
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            Toast.makeText(OrderActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();
        }, 2000);
    }

    private OrderModel createOrder(CartItem cartItem) {
        String orderId = "ORD" + System.currentTimeMillis();
        String orderDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        String orderStatus = OrderModel.OrderStatus.CONFIRMED.getStatus();

        String paymentMethod = getSelectedPaymentMethod();
        String paymentStatus = paymentMethod.equals("Cash on Delivery") ? "Pending" : "Paid";

        OrderModel orderModel = new OrderModel(orderId, orderDate, orderStatus, cartItem.getTotalPrice(), deliveryCharge, cartItem.getQuantity(), paymentMethod, paymentStatus, selectedAddress, cartItem);
        orderModel.setEstimatedDelivery(getFutureDate(2));
        database.getReference("Orders").child(userId).child(orderId).setValue(orderModel);
        return orderModel;
    }

    private String getFutureDate(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    private String getSelectedPaymentMethod() {
        int selectedId = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.rbCod) return "Cash on Delivery";
        if (selectedId == R.id.rbQR) return "QR Code";
        if (selectedId == R.id.rbCard) return "Credit/Debit Card";
        if (selectedId == R.id.rbUpi) return "UPI";
        if (selectedId == R.id.rbNetbanking) return "Net Banking";
        return "Unknown";
    }

    private void showOrderSummary(OrderModel order) {
        orderSummaryLayout.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        tvOrderId.setText("Order ID: " + order.getOrderId());
        tvOrderDate.setText("Placed on: " + order.getOrderDate());
        tvOrderStatus.setText("Status: " + order.getOrderStatus());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
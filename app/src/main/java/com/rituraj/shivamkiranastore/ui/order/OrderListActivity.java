package com.rituraj.shivamkiranastore.ui.order;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.Models.OrderModel;
import com.rituraj.shivamkiranastore.adapter.OrderListAdapter;
import com.rituraj.shivamkiranastore.ui.payment.PaymentActivity;
import com.rituraj.shivamkiranastore.ui.product.ProductActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderListActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private OrderListAdapter adapter;
    private List<OrderModel> orderList = new ArrayList<>();
    private List<OrderModel> filteredOrderList = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipActive, chipDelivered, chipCancelled;
    private LinearLayout filterSortLayout;
    private Spinner spinnerSort;
    private Button btnApplyFilter, btnClearFilter;
    private EditText etSearch;
    private FloatingActionButton fabSupport;

    // Filter variables
    private String currentFilter = "ALL";
    private String currentSort = "Recent";
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String userId, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        setupStatusBar();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.logo_blue));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initDatabase();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        loadSampleOrders();
        setupListeners();
    }

    private void setupStatusBar() {
        Window window = getWindow();

        // Make status bar transparent (required for Android 15+)
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Create a view to act as the colored status bar background
        View statusBarBackground = new View(this);
        statusBarBackground.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight()));
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
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView());
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

    private void initDatabase() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("orders");
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userId = getIntent().getStringExtra("userId");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipAll = findViewById(R.id.chipAll);
        chipActive = findViewById(R.id.chipActive);
        chipDelivered = findViewById(R.id.chipDelivered);
        chipCancelled = findViewById(R.id.chipCancelled);
        filterSortLayout = findViewById(R.id.filterSortLayout);
        spinnerSort = findViewById(R.id.spinnerSort);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        etSearch = findViewById(R.id.etSearch);
        fabSupport = findViewById(R.id.fabSupport);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Orders");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        adapter = new OrderListAdapter(OrderListActivity.this, filteredOrderList,
                new OrderListAdapter.OnOrderActionListener() {
                    @Override
                    public void onOrderClick(OrderModel order, int position) {
                        showOrderDetails(order);
                    }

                    @Override
                    public void onTrackOrder(OrderModel order, int position) {
                        trackOrder(order);
                    }

                    @Override
                    public void onReturnOrder(OrderModel order, int position) {
                        showReturnDialog(order);
                    }

                    @Override
                    public void onReorder(OrderModel order, int position) {
                        reorderItems(order);
                    }

                    @Override
                    public void onCancelOrder(OrderModel order, int position) {
                        showCancelDialog(order, position);
                    }

                    @Override
                    public void onDeliveredOrder(OrderModel order, int position) {
                        showDeliveredDialog(order, position);
                    }
                }, email);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    private void setupFilters() {
        // Setup sort spinner
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this, R.array.sort_options,
                android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        // Setup chip group listener
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipAll.setChecked(true);
                currentFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipAll) {
                    currentFilter = "ALL";
                } else if (checkedId == R.id.chipActive) {
                    currentFilter = "ACTIVE";
                } else if (checkedId == R.id.chipDelivered) {
                    currentFilter = "DELIVERED";
                } else if (checkedId == R.id.chipCancelled) {
                    currentFilter = "CANCELLED";
                }
            }
            filterOrders();
        });
    }

    private void loadSampleOrders() {
        progressBar.setVisibility(View.VISIBLE);

        reference = FirebaseDatabase.getInstance().getReference("Orders").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && getContext() != null) {
                    orderList.clear();
                    filteredOrderList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel order = dataSnapshot.getValue(OrderModel.class);
                        if (order != null) {
                            order.setOrderId(dataSnapshot.getKey());
                            orderList.add(order);
                        }
                    }
                    Collections.reverse(orderList);
                    filteredOrderList.addAll(orderList);
                    adapter.notifyDataSetChanged();
                    checkEmptyState();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshOrders();
        });

        btnApplyFilter.setOnClickListener(v -> {
            applyFilters();
        });

        btnClearFilter.setOnClickListener(v -> {
            clearFilters();
        });

        fabSupport.setOnClickListener(v -> {
            showSupportDialog();
        });

        // Search on text change
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void filterOrders() {
        String searchQuery = etSearch.getText().toString().toLowerCase();

        filteredOrderList.clear();

        for (OrderModel order : orderList) {
            boolean matchesFilter = true;

            // Apply status filter
            if (!currentFilter.equals("ALL")) {
                switch (currentFilter) {
                    case "ACTIVE":
                        matchesFilter = !order.getOrderStatus().equals("Delivered")
                                && !order.getOrderStatus().equals("Cancelled");
                        break;
                    case "DELIVERED":
                        matchesFilter = order.getOrderStatus().equals("Delivered");
                        break;
                    case "CANCELLED":
                        matchesFilter = order.getOrderStatus().equals("Cancelled");
                        break;
                }
            }

            // Apply search filter
            if (matchesFilter && !searchQuery.isEmpty()) {
                matchesFilter = order.getOrderId().toLowerCase().contains(searchQuery)
                        || order.getItems().getName().toLowerCase().contains(searchQuery);
            }

            if (matchesFilter) {
                filteredOrderList.add(order);
            }
        }
        sortOrders();
        adapter.notifyDataSetChanged();
        checkEmptyState();
    }

    private void sortOrders() {
        // Get selected sort option
        String sortOption = spinnerSort.getSelectedItem().toString();

        switch (sortOption) {
            case "Recent First":
                // Assuming orders are already in recent order from sample
                break;
            case "Oldest First":
                // Reverse the list
                java.util.Collections.reverse(filteredOrderList);
                break;
            case "Highest Amount":
                filteredOrderList.sort((o1, o2) -> Double.compare(o2.getTotalAmount(), o1.getTotalAmount()));
                break;
            case "Lowest Amount":
                filteredOrderList.sort((o1, o2) -> Double.compare(o1.getTotalAmount(), o2.getTotalAmount()));
                break;
        }
    }

    private void applyFilters() {
        filterOrders();
        Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show();
    }

    private void clearFilters() {
        chipAll.setChecked(true);
        spinnerSort.setSelection(0);
        etSearch.setText("");
        currentFilter = "ALL";
        filteredOrderList.clear();
        filteredOrderList.addAll(orderList);
        adapter.notifyDataSetChanged();
        checkEmptyState();
    }

    private void refreshOrders() {
        // Simulate refresh
        swipeRefreshLayout.setRefreshing(true);

        recyclerView.postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "Orders updated", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void showOrderDetails(OrderModel order) {
        Intent intent = new Intent(OrderListActivity.this, ProductActivity.class);
        intent.putExtra("productId", order.getItems().getId());
        startActivity(intent);
    }

    private void trackOrder(OrderModel order) {
        Toast.makeText(this, "Tracking order: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
        // Navigate to tracking activity
    }

    private void showReturnDialog(OrderModel order) {
        new AlertDialog.Builder(this).setTitle("Return Order").setMessage("Are you sure you want to return this order?")
                .setPositiveButton("Yes, Return", (dialog, which) -> {
                    Toast.makeText(this, "Return request submitted for " + order.getOrderId(), Toast.LENGTH_SHORT)
                            .show();
                }).setNegativeButton("Cancel", null).show();
    }

    private void showCancelDialog(OrderModel order, int position) {
        new AlertDialog.Builder(this).setTitle("Cancel Order").setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    order.setOrderStatus("Cancelled");
                    order.setOrderStatusColor("#F44336");
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show();
                    filterOrders();
                    database.getReference("Orders").child(userId).child(order.getOrderId()).child("orderStatus")
                            .setValue("Cancelled");
                }).setNegativeButton("No", null).show();
    }

    private void showDeliveredDialog(OrderModel order, int position) {
        new AlertDialog.Builder(this).setTitle("Deliver Order")
                .setMessage("Are you sure you want to deliver this order?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    order.setOrderStatus("Delivered");
                    order.setOrderStatusColor("#4CAF50");
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Delivered Order", Toast.LENGTH_SHORT).show();
                    filterOrders();
                    database.getReference("Orders").child(userId).child(order.getOrderId()).child("orderStatus")
                            .setValue("Delivered");
                }).setNegativeButton("No", null).show();
    }

    private void reorderItems(OrderModel order) {
        Intent intent = new Intent(OrderListActivity.this, ProductActivity.class);
        intent.putExtra("productId", order.getItems().getId());
        startActivity(intent);
    }

    private void showSupportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_support, null);
        builder.setView(dialogView);

        TextView tvCall = dialogView.findViewById(R.id.tvCall);
        TextView tvChat = dialogView.findViewById(R.id.tvChat);
        TextView tvEmail = dialogView.findViewById(R.id.tvEmail);

        AlertDialog dialog = builder.create();

        tvCall.setOnClickListener(v -> {
            Toast.makeText(this, "Calling ...", Toast.LENGTH_SHORT).show();
            String phoneNumber = "tel:9876543210";
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(phoneNumber));
            startActivity(intent);
            dialog.dismiss();
        });

        tvChat.setOnClickListener(v -> {
            Toast.makeText(this, "Opening live chat...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        tvEmail.setOnClickListener(v -> {
            Toast.makeText(this, "Opening email...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:shivamkiranastore@example.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
            intent.putExtra(Intent.EXTRA_TEXT, "Hello Support Team,");
            startActivity(Intent.createChooser(intent, "Send Email"));
            dialog.dismiss();
        });
        dialog.show();
    }

    private void checkEmptyState() {
        if (filteredOrderList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            // Toggle search bar
            filterSortLayout.setVisibility(filterSortLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return true;
        } else if (id == R.id.action_filter) {
            // Show filter dialog
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        // Implement advanced filter dialog if needed
        Toast.makeText(this, "Advanced filters", Toast.LENGTH_SHORT).show();
    }
}

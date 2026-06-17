package com.rituraj.shivamkiranastore.ui.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.Models.Product;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.adapter.ShowProductList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchProductActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private EditText etSearchQuery;
    private ImageView ivClearSearch;
    private TextView tvCancel, tvResultCount, tvClearRecent;
    private LinearLayout layoutRecentSearches, layoutPopularCategories, layoutNoResults;
    private LinearProgressIndicator progressBar;
    private RecyclerView recyclerViewResults;
    private LinearLayout layoutFilter;
    private LinearLayout chipContainer;
    private LinearLayout chipCategoryContainer;  // ADD THIS

    // Firebase
    private DatabaseReference productsReference;
    private ArrayList<Product> allProductsList;
    private ArrayList<Product> searchResultsList;

    // Adapter
    private ShowProductList searchAdapter;

    // Recent searches
    private SharedPreferences sharedPreferences;
    private Set<String> recentSearchesSet;
    private ArrayList<String> recentSearchesList;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;

    // Categories
    private String[] categories = {"Masala", "Rice & Grains", "Dal & Pulses",
            "Oil & Ghee", "Snacks", "Beverages",
            "Personal Care", "Household"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setupStatusBar();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupToolbar();
        setupFirebase();
        setupSearchListeners();
        loadRecentSearches();
        loadPopularCategories();
        loadAllProducts();
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

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        ivClearSearch = findViewById(R.id.ivClearSearch);
        tvCancel = findViewById(R.id.tvCancel);
        tvResultCount = findViewById(R.id.tvResultCount);
        layoutRecentSearches = findViewById(R.id.layoutRecentSearches);
        layoutPopularCategories = findViewById(R.id.layoutPopularCategories);
        layoutNoResults = findViewById(R.id.layoutNoResults);
        progressBar = findViewById(R.id.progressBar);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        layoutFilter = findViewById(R.id.layoutFilter);
        chipContainer = findViewById(R.id.chipContainer);
        chipCategoryContainer = findViewById(R.id.chipCategoryContainer);  // INITIALIZE THIS
        tvClearRecent = findViewById(R.id.tvClearRecent);

        etSearchQuery.requestFocus();

        allProductsList = new ArrayList<>();
        searchResultsList = new ArrayList<>();

        sharedPreferences = getSharedPreferences("search_prefs", MODE_PRIVATE);
        recentSearchesSet = new HashSet<>();
        recentSearchesList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        productsReference = database.getReference().child("Products");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        userId = user.getUid();
    }

    private void setupSearchListeners() {
        // Search on text change
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    ivClearSearch.setVisibility(View.VISIBLE);
                    performSearch(s.toString());
                } else {
                    ivClearSearch.setVisibility(View.GONE);
                    showRecentAndCategories();
                    clearSearchResults();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Search on keyboard action
        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearchQuery.getText().toString().trim();
                if (!TextUtils.isEmpty(query)) {
                    performSearch(query);
                    saveRecentSearch(query);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        // Clear search text
        ivClearSearch.setOnClickListener(v -> {
            etSearchQuery.setText("");
            showRecentAndCategories();
            clearSearchResults();
        });

        // Cancel button
        tvCancel.setOnClickListener(v -> finish());

        // Clear recent searches
        tvClearRecent.setOnClickListener(v -> clearRecentSearches());

        // Filter button
        layoutFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);

        productsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProductsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setProductId(dataSnapshot.getKey());
                        allProductsList.add(product);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchProductActivity.this,
                        "Error loading products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);

        // Hide recent searches and categories
        layoutRecentSearches.setVisibility(View.GONE);
        layoutPopularCategories.setVisibility(View.GONE);

        searchResultsList.clear();

        String searchLower = query.toLowerCase().trim();

        for (Product product : allProductsList) {
            if (product.getProductName().toLowerCase().contains(searchLower) ||
                    (product.getProductNameHindi() != null &&
                            product.getProductNameHindi().toLowerCase().contains(searchLower)) ||
                    (product.getCategory() != null &&
                            product.getCategory().toLowerCase().contains(searchLower)) ||
                    (product.getBrand() != null &&
                            product.getBrand().toLowerCase().contains(searchLower))) {
                searchResultsList.add(product);
            }
        }

        progressBar.setVisibility(View.GONE);

        if (searchResultsList.isEmpty()) {
            showNoResults();
        } else {
            showResults();
        }

        // Update result count
        tvResultCount.setText(searchResultsList.size() + " products found");
    }

    private void showResults() {
        layoutNoResults.setVisibility(View.GONE);
        recyclerViewResults.setVisibility(View.VISIBLE);
        recyclerViewResults.setLayoutManager(new GridLayoutManager(SearchProductActivity.this, 2));

        searchAdapter = new ShowProductList(SearchProductActivity.this, searchResultsList, userId);
        recyclerViewResults.setAdapter(searchAdapter);
    }

    private void showNoResults() {
        layoutNoResults.setVisibility(View.VISIBLE);
        recyclerViewResults.setVisibility(View.GONE);
    }

    private void showRecentAndCategories() {
        layoutRecentSearches.setVisibility(View.VISIBLE);
        layoutPopularCategories.setVisibility(View.VISIBLE);
        layoutNoResults.setVisibility(View.GONE);
        recyclerViewResults.setVisibility(View.GONE);
        tvResultCount.setText("0 products found");
    }

    private void clearSearchResults() {
        searchResultsList.clear();
        if (searchAdapter != null) {
            showResults();
        }
    }

    private void loadRecentSearches() {
        recentSearchesSet = sharedPreferences.getStringSet("recent_searches", new HashSet<>());
        recentSearchesList.clear();
        recentSearchesList.addAll(recentSearchesSet);

        // Show only last 5 searches
        int size = Math.min(recentSearchesList.size(), 5);

        chipContainer.removeAllViews();

        for (int i = 0; i < size; i++) {
            String search = recentSearchesList.get(i);
            addRecentSearchChip(search);
        }
    }

    private void addRecentSearchChip(String searchText) {
        View chip = getLayoutInflater().inflate(R.layout.chip_recent_search, chipContainer, false);
        TextView tvChipText = chip.findViewById(R.id.tvChipText);
        ImageView ivChipClose = chip.findViewById(R.id.ivChipClose);

        tvChipText.setText(searchText);

        chip.setOnClickListener(v -> {
            etSearchQuery.setText(searchText);
            performSearch(searchText);
        });

        ivChipClose.setOnClickListener(v -> {
            recentSearchesSet.remove(searchText);
            saveRecentSearches();
            loadRecentSearches();
        });

        chipContainer.addView(chip);
    }

    private void loadPopularCategories() {
        if (chipCategoryContainer == null) return;

        chipCategoryContainer.removeAllViews();

        for (String category : categories) {
            addCategoryChip(category);
        }
    }

    private void addCategoryChip(String categoryName) {
        MaterialCardView chip = (MaterialCardView) getLayoutInflater()
                .inflate(R.layout.chip_category, chipCategoryContainer, false);
        TextView tvChipText = chip.findViewById(R.id.tvCategoryChipText);

        tvChipText.setText(categoryName);

        chip.setOnClickListener(v -> {
            etSearchQuery.setText(categoryName);
            performSearch(categoryName);
            saveRecentSearch(categoryName);
        });

        chipCategoryContainer.addView(chip);
    }

    private void saveRecentSearch(String query) {
        recentSearchesSet.add(query);
        saveRecentSearches();
        loadRecentSearches();
    }

    private void saveRecentSearches() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("recent_searches", recentSearchesSet);
        editor.apply();
    }

    private void clearRecentSearches() {
        recentSearchesSet.clear();
        saveRecentSearches();
        loadRecentSearches();
        Toast.makeText(this, "Recent searches cleared", Toast.LENGTH_SHORT).show();
    }

    private void showFilterDialog() {
//        Toast.makeText(this, "Filter dialog (to be implemented)", Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
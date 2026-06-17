package com.rituraj.shivamkiranastore.ui.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.adapter.ViewPagerAdapter;
import com.rituraj.shivamkiranastore.ui.admin.ui.OrdersFragment;
import com.rituraj.shivamkiranastore.ui.admin.ui.ProductsFragment;
import com.rituraj.shivamkiranastore.ui.admin.ui.dasboard.DashboardFragment;

public class AdminDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivNotifications, ivProfile;
    private TextView tvAdminName;

    // Tab Layout and ViewPager
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;

    // Adapter
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable vector drawables
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupToolbar();
        setupViewPager();
        setupTabs();
        setupBottomNavigation();
        setupClickListeners();
        loadAdminData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivNotifications = findViewById(R.id.ivNotifications);
        ivProfile = findViewById(R.id.ivProfile);
        tvAdminName = findViewById(R.id.tvAdminName);

        // Tab Layout and ViewPager
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }
    }

    private void setupViewPager() {
        // Create adapter
        viewPagerAdapter = new ViewPagerAdapter(this); // Using FragmentActivity constructor

        // Add fragments
        viewPagerAdapter.addFragment(new DashboardFragment(), "Dashboard");
        viewPagerAdapter.addFragment(new ProductsFragment(), "Products");
        viewPagerAdapter.addFragment(new OrdersFragment(), "Orders");
//        viewPagerAdapter.addFragment(new InventoryFragment(), "Inventory");
//        viewPagerAdapter.addFragment(new ReportsFragment(), "Reports");

        // Set adapter
        viewPager.setAdapter(viewPagerAdapter);
    }

    private void setupTabs() {
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(viewPagerAdapter.getFragmentTitle(position));
                }).attach();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.nav_products) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.nav_orders) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.nav_more) {
                showMoreOptions();
                return true;
            }

            return false;
        });

        // Sync bottom navigation with viewpager
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
                        break;
                    case 1:
                        bottomNavigation.setSelectedItemId(R.id.nav_products);
                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.nav_orders);
                        break;
                }
            }
        });
    }

    private void showMoreOptions() {
        // Show a dialog or navigate to more options
        Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        ivNotifications.setOnClickListener(v -> {
            Toast.makeText(AdminDashboardActivity.this, "Notifications", Toast.LENGTH_SHORT).show();
        });

        ivProfile.setOnClickListener(v -> {
            Toast.makeText(AdminDashboardActivity.this, "Profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAdminData() {
        // Load from SharedPreferences or intent
        tvAdminName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "");
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }
}
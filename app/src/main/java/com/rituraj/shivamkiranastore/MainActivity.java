package com.rituraj.shivamkiranastore;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.shivamkiranastore.databinding.ActivityMainBinding;
import com.rituraj.shivamkiranastore.locations.LocationManager;
import com.rituraj.shivamkiranastore.locations.PermissionManager;
import com.rituraj.shivamkiranastore.ui.address.AddAddressActivity;
import com.rituraj.shivamkiranastore.ui.admin.AdminDashboardActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String[] foregroundLocationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;
    private ActivityMainBinding binding;
    private Menu topMenu;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser user;
    public String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setupStatusBar();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.logo_blue));
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        TopToolBarSetting();
        fun();
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
        user = auth.getCurrentUser();
        userId = user.getUid();
        setOnline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);
        topMenu = menu;
        MenuItem item = menu.findItem(R.id.top_no_of_coins);
        View actionView = item.getActionView();
        TextView coinsText = actionView.findViewById(R.id.coins_text);
        setCoins(coinsText);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.top_favorites) {
            Toast.makeText(this, "Favorites", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.top_no_of_coins) {
//            Toast.makeText(this, "No. of coins", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCoins(TextView coinsText) {
        init();
        getPlayer(coinsText);
    }

    private void TopToolBarSetting() {
        Toolbar mainToolBar = findViewById(R.id.main_top_toolbar);
        setSupportActionBar(mainToolBar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle("Express delivery in 30 mins");
        mainToolBar.setOnClickListener((v) -> {
            String email = user.getEmail();
            if (email.equals("riturajkumar1105@gmail.com") || email.equals("shivamkumarjkt@gmail.com") || email.equals("shubham756291@gmail.com") || email.equals("asah92373@gmail.com"))
                startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
            else {
                try {
                    double lat = Double.parseDouble("26.7352680");
                    double lng = Double.parseDouble("84.6291790");

                    String uri = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps"); // force open in Google Maps
                    startActivity(intent);

                    Toast.makeText(MainActivity.this, "Find Best Route..", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getPlayer(TextView coinsText) {
        coinsText.setText("120");
    }

    private void fun() {
        takeUserLocation();
        fetchUserLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager.handlePermissionResult(MainActivity.this, 100, permissions, grantResults)) {
            locationManager.createLocationRequest();
        } else
            Toast.makeText(MainActivity.this, "Permission denied.", Toast.LENGTH_SHORT).show();
    }

    public void takeUserLocation() {
        permissionManager = PermissionManager.getInstance(MainActivity.this);
        locationManager = LocationManager.getInstance(MainActivity.this);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("foreground_location");
    }

    public String getDistance(String currLat, String currLng) {
        double lat1, lon1, lat2, lon2;
        try {
            lat1 = Double.parseDouble(currLat);
            lon1 = Double.parseDouble(currLng);
            lat2 = Double.parseDouble("26.7352680");
            lon2 = Double.parseDouble("84.6291790");
        } catch (NumberFormatException e) {
            return "N/A";
        }
        final int R = 6371000; // meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceMeters = R * c;
        if (distanceMeters < 1000) {
            return Math.round(distanceMeters) + " m";
        } else {
            double km = distanceMeters / 1000;
            return String.format(Locale.getDefault(), "%.2f km", km);
        }
    }

    public void fetchUserLocation() {
        if (!permissionManager.checkPermissions(foregroundLocationPermissions)) {
            permissionManager.askPermissions(MainActivity.this, foregroundLocationPermissions, 100);
        } else if (!locationManager.isLocationEnabled())
            locationManager.createLocationRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.startLocationUpdates();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(foregroundLocationBroadCastReceiver, localBroadcastIntentFilter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(foregroundLocationBroadCastReceiver);
        locationManager.stopLocationUpdates();
        super.onPause();
    }

    BroadcastReceiver foregroundLocationBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String lat = intent.getStringExtra("Lat");
            String lng = intent.getStringExtra("Lng");
            if (lat == null || lng == null || lat.isEmpty() || lng.isEmpty()) return;
            if (getSupportActionBar() != null)
                getSupportActionBar().setSubtitle(getDistance(lat, lng) + " • Express delivery in 30 mins");
        }
    };

    private void setOnline() {
        if (user != null) {
            DatabaseReference userStatusRef =
                    database.getReference("Users")
                            .child(user.getUid())
                            .child("isOnline");
            userStatusRef.setValue(true);
            userStatusRef.onDisconnect().setValue(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOnline();
    }

    @Override
    protected void onDestroy() {
        if (user != null)
            database.getReference("Users").child(user.getUid()).child("isOnline").setValue(false);
        super.onDestroy();
    }

}

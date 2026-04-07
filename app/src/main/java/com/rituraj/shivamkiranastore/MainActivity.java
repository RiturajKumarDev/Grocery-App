package com.rituraj.shivamkiranastore;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class MainActivity extends AppCompatActivity {
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
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.logo_blue));
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        Toolbar toolbar = findViewById(R.id.main_top_toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home,
                R.id.navigation_carts, R.id.navigation_profile).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        TopToolBarSetting();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
//        fun();
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

        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_coins_foreground);
            getSupportActionBar().setSubtitle("Express delivery in 30 mins");
        }
    }

    private void getPlayer(TextView coinsText) {
        coinsText.setText("120");
    }

}

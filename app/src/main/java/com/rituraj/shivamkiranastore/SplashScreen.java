package com.rituraj.shivamkiranastore;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rituraj.shivamkiranastore.ui.auth.LoginActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        startMain();
    }

    public void startMain() {
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (Exception ignored) {
                } finally {
                    startLoginPage();
                }
            }
        };
        t.start();
    }

    private void startLoginPage() {
        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
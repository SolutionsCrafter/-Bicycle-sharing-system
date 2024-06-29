package com.example.bicycleapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TextView tvWelcome;
    ImageView imgWelcome;
    Button btnLogin, btnRegister;
    FirebaseAuth fAuth;
    private int backPressedCount = 0; // Added for back press handling
    private android.widget.Toast Toast;
    private SharedPreferences rideData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fAuth = FirebaseAuth.getInstance();
        tvWelcome = findViewById(R.id.tvWelcome);
        imgWelcome = findViewById(R.id.imgWelcome);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        rideData = getSharedPreferences("RideData", MODE_PRIVATE);

        btnLogin.setOnClickListener(v -> {
            FirebaseUser currentUser = fAuth.getCurrentUser();
            if (currentUser != null) {
                Toast.makeText(MainActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                if (shouldOpenOnRidePage()) {
                    Intent intent = new Intent(MainActivity.this, OnRide.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, Payment.class);
                    startActivity(intent);
                }
                finish();
            } else {
                Intent intent = new Intent(MainActivity.this, LoginPage.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpPage.class);
            startActivity(intent);
        });
    }

    private boolean shouldOpenOnRidePage() {
        return rideData.contains("startStation") && rideData.contains("formattedTime");
    }

    @Override
    public void onBackPressed() {
        if (backPressedCount == 0) {
            showExitConfirmationToast();
            backPressedCount++;
        } else {
            super.onBackPressed();
        }
    }

    private void showExitConfirmationToast() {
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> backPressedCount = 0, 2000); // Reset counter after 2 seconds
    }
}

package com.example.bicycleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class paymentDetails extends AppCompatActivity {

    ImageView btnHome,btnProfile,btnExit;
    TextView tvCardType,tvCardNumber,tvBankName,tvBranchName;
    LinearLayout btnBack,btnSettings,btnSignOut;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Implement OnBackPressedDispatcher for back button navigation
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to the previous activity
                Intent loginIntent = new Intent(paymentDetails.this, Profile.class);
                // Optionally, clear the activity stack to prevent back button from returning to HomePage
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish(); // Close HomePage
            }
        });

        btnBack = findViewById(R.id.btnBack);
        btnSignOut = findViewById(R.id.btnSignOut);
        tvCardType = findViewById(R.id.tvCardType);
        tvCardNumber = findViewById(R.id.tvCardNumber);
        tvBankName = findViewById(R.id.tvBankName);
        tvBranchName = findViewById(R.id.tvBranchName);
        btnHome = findViewById(R.id.imgHome);
        btnProfile = findViewById(R.id.imgProfile);
        btnExit = findViewById(R.id.imgExit);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        paymentDataFromFirebase();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(paymentDetails.this,com.example.bicycleapp.Profile.class);
                startActivity(intent1);
                finish();
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(paymentDetails.this,"Successfully Signed Out",Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(paymentDetails.this,com.example.bicycleapp.MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(paymentDetails.this,com.example.bicycleapp.Payment.class);
                startActivity(intent1);
                finish();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(paymentDetails.this,com.example.bicycleapp.Profile.class);
                startActivity(intent1);
                finish();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(paymentDetails.this, "Successfully Exited", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(paymentDetails.this,com.example.bicycleapp.MainActivity.class);
                startActivity(intent1);
                finish();
            }
        });

    }

    void paymentDataFromFirebase(){

        DocumentReference docRef = fStore.collection("Payment details").document(userID);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                tvCardType.setText(value.getString("Card type"));
                tvCardNumber.setText(value.getString("Card number"));
                tvBankName.setText(value.getString("Bank name"));
                tvBranchName.setText(value.getString("Branch"));

            }
        });

    }



}
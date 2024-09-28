package com.example.bicycleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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


public class Profile extends AppCompatActivity {

    // Declare variables
    ImageView imgProPic,img_home_inprofile,imgLogout_inprofile;
    TextView tvUserName,tvEmail;
    LinearLayout btnPay,btnSettings,btnNotifi;
    TextView tvProfileName,tvProfileNIC,tvProfileTel,tvProfileAddress;
    Button btnEditProfile;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to LoginPage
                Intent loginIntent = new Intent(Profile.this, Payment.class);
                // Optionally, clear the activity stack to prevent back button from returning to HomePage
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish(); // Close HomePage
            }
        });

        fAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        btnNotifi = findViewById(R.id.btnSignOut);
        imgProPic = findViewById(R.id.imgProPic);
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        btnPay = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        btnNotifi = findViewById(R.id.btnSignOut);
        tvProfileName = findViewById(R.id.tvCardNumber);
        tvProfileNIC = findViewById(R.id.tvProfileNIC);
        tvProfileTel = findViewById(R.id.tvProfileTel);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);
        img_home_inprofile = findViewById(R.id.imgHome);
        imgLogout_inprofile = findViewById(R.id.imgExit);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DocumentReference docRef = fStore.collection("Users").document(userID);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                tvUserName.setText(value.getString("User Name"));
                tvEmail.setText(value.getString("Email"));
                tvProfileName.setText(value.getString("User Name"));
                tvProfileNIC.setText(value.getString("NIC"));
                tvProfileTel.setText(value.getString("Telephone number"));
                tvProfileAddress.setText(value.getString("Address"));
                //tvProfileAddress.setText();


            }
        });

    img_home_inprofile.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Profile.this,com.example.bicycleapp.Payment.class);
            startActivity(intent);
            finish();
        }
    });

    imgLogout_inprofile.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(Profile.this, "Successfully Exited", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Profile.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    });
    btnPay.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Profile.this,com.example.bicycleapp.paymentDetails.class);
            startActivity(intent);
            finish();
        }
    });

    btnSettings.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent20 = new Intent(com.example.bicycleapp.Profile.this,com.example.bicycleapp.ContactUs.class);
            startActivity(intent20);
            finish();
        }
    });

    btnNotifi.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(Profile.this,"Successfully Signed Out",Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Profile.this,com.example.bicycleapp.MainActivity.class);
            startActivity(intent);
            finish();
        }
    });

    }
}
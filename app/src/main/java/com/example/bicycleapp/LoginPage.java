package com.example.bicycleapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginPage extends AppCompatActivity {

    // Declare Variables
    EditText etEmail,etPassword;
    TextView tvForgotPass;
    ImageView imgGoogle,imgFB,imgX;
    Button btnLog;
    String userName;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    GoogleSignInAccount googleSignInAccount;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to LoginPage
                Intent loginIntent = new Intent(LoginPage.this, MainActivity.class);
                // Optionally, clear the activity stack to prevent back button from returning to HomePage
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish(); // Close HomePage
            }
        });

        // Initialize Firebase Auth
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        // Link java & XML elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvForgotPass = findViewById(R.id.tvForgotPass);
        imgGoogle = findViewById(R.id.imgGoogle);
        imgFB = findViewById(R.id.imgFB);
        imgX = findViewById(R.id.imgX);
        btnLog = findViewById(R.id.btnLog);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // If login button clicked
        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email,password;
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(LoginPage.this,"Please enter user name",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(LoginPage.this,"Please enter password",Toast.LENGTH_SHORT).show();
                    return;
                }


                fAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success
                                    Toast.makeText(LoginPage.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginPage.this,com.example.bicycleapp.Payment.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails
                                    Toast.makeText(LoginPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });




            }
        });

        /*
        imgGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInAccount gsio = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
                googleSignInAccount1 = GoogleSignIn

            }
        });
        */

    }
}
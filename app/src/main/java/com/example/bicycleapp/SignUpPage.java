package com.example.bicycleapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

//SingUp page

public class SignUpPage extends AppCompatActivity {

    // Declare variables
    EditText etEnterUserName,etEnterEmail,etEnterPass,etEnterNIC,etAddress,etEnterTel;
    TextView tvClickLog;
    Button btnSignUp;
    FirebaseAuth fAuth;
    // Initialize Firebase
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_page);

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to LoginPage
                Intent loginIntent = new Intent(SignUpPage.this, MainActivity.class);
                // Optionally, clear the activity stack to prevent back button from returning to HomePage
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish(); // Close HomePage
            }
        });

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        // Link java & XML
        etEnterUserName = findViewById(R.id.etEnterUserName);
        etEnterEmail = findViewById(R.id.etEnterEmail);
        etEnterPass = findViewById(R.id.etEnterPass);
        etEnterNIC = findViewById(R.id.etEnterNIC);
        etEnterTel = findViewById(R.id.etEnterTel);
        etAddress = findViewById(R.id.etAddress);
        //tvClickLog = findViewById(R.id.tvClickLog);
        btnSignUp = findViewById(R.id.btnSingIn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // If SignUp button clicked
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName,email,password,NIC,address,tel;

                userName = etEnterUserName.getText().toString();
                email = etEnterEmail.getText().toString().trim();
                password = etEnterPass.getText().toString().trim();
                NIC = etEnterNIC.getText().toString().trim();
                address = etAddress.getText().toString().trim();
                tel = etEnterTel.getText().toString().trim();


                if (TextUtils.isEmpty(userName)){
                    Toast.makeText(SignUpPage.this,"Please enter user name",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(SignUpPage.this,"Please enter email",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(SignUpPage.this,"Please enter password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(NIC)){
                    Toast.makeText(SignUpPage.this,"Please enter NIC number",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(address)){
                    Toast.makeText(SignUpPage.this,"Please enter address",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(tel)){
                    Toast.makeText(SignUpPage.this,"Please enter telephone number",Toast.LENGTH_SHORT).show();
                    return;
                }


                // pass data to firebase
                fAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success
                                    Toast.makeText(SignUpPage.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                                    userID = fAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = fStore.collection("Users").document(userID);
                                    //create user data hash map
                                    Map<String,Object> user = new HashMap<>();
                                    user.put("User Name",userName);
                                    user.put("Email",email);
                                    user.put("Password",password);
                                    user.put("NIC",NIC);
                                    user.put("Address",address);
                                    user.put("Telephone number",tel);
                                    user.put("user ID",userID);
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d(TAG,"User profile created for"+userID);
                                        }
                                    });
                                    Intent intent = new Intent(SignUpPage.this,com.example.bicycleapp.PaymentGateway.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    // If sign in fails
                                    Toast.makeText(SignUpPage.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure
                            }
                        });
            }
        });

    }
}
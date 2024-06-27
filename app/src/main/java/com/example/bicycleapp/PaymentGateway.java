package com.example.bicycleapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentGateway extends AppCompatActivity {

    RadioGroup radioGroup;
    RadioButton radioVisa,radioMasterCard;
    EditText etCardNumber,etCVN,etBankName,etBranch;
    Button btnSaveCardDetails;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    String cardType,cardNumber,CVN,bankName,branch;
    int checkedRadioButtonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_gateway);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Implement OnBackPressedDispatcher for back button navigation
            OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
            dispatcher.addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // Navigate back to the previous activity
                    Intent loginIntent = new Intent(PaymentGateway.this, SignUpPage.class);
                    // Optionally, clear the activity stack to prevent back button from returning to HomePage
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish(); // Close HomePage
                }
            });
            return insets;
        });

        radioGroup = findViewById(R.id.radioGroup1);
        radioVisa = findViewById(R.id.radioVisa_1);
        radioMasterCard = findViewById(R.id.radioMasterCard_2);
        etCardNumber = findViewById(R.id.etCardNumber);
        etCVN = findViewById(R.id.etCvn);
        etBankName = findViewById(R.id.etBankName);
        etBranch = findViewById(R.id.etBranch);
        btnSaveCardDetails = findViewById(R.id.btnSaveCardDetails);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        btnSaveCardDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFirebase();
            }
        });
    }

    //Update firebase
    private void updateFirebase() {

        //Get the ID of the checked radio button
        checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        if (checkedRadioButtonId == radioVisa.getId()) {
            cardType = "Visa card";
        } else if (checkedRadioButtonId == radioMasterCard.getId()){
            cardType = "Master card";
        }
        cardNumber = etCardNumber.getText().toString();
        CVN = etCVN.getText().toString().trim();
        bankName = etBankName.getText().toString();
        branch = etBranch.getText().toString();

        //Check is data fields empty
        if (TextUtils.isEmpty(cardType)){
            Toast.makeText(PaymentGateway.this,"Please select card type",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(cardNumber)){
            Toast.makeText(PaymentGateway.this,"Please enter Card number",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(CVN)){
            Toast.makeText(PaymentGateway.this,"Please enter CVN code",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(bankName)){
            Toast.makeText(PaymentGateway.this,"Please enter Bank name",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(branch)){
            Toast.makeText(PaymentGateway.this,"Please enter Branch name",Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if all fields are not empty using negation (all must be NOT empty)
        if (!(TextUtils.isEmpty(cardType) && TextUtils.isEmpty(cardNumber) && TextUtils.isEmpty(CVN) &&
                TextUtils.isEmpty(bankName) && TextUtils.isEmpty(branch))) {

            DocumentReference docRef7 = fStore.collection("Payment details").document(userID);

            // Create a new station object with fields
            Map<String, Object> stationData = new HashMap<>();
            stationData.put("Card type", cardType);
            stationData.put("Card number", cardNumber);
            stationData.put("CVN code", CVN);
            stationData.put("Bank name", bankName);
            stationData.put("Branch", branch);

            // Create a reference to the document in the relevant collection using currentUserId


            // Update the document in Firestore based on station number
            docRef7.set(stationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(PaymentGateway.this,"Payment details saved",Toast.LENGTH_SHORT).show();
                    goBackToPaymentPage();
                }
            });

        } else {
            Toast.makeText(PaymentGateway.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
        }
    }

    void goBackToPaymentPage(){
        Intent intent1 = new Intent(PaymentGateway.this,com.example.bicycleapp.LoginPage.class);
        startActivity(intent1);
        finish();
    }

}
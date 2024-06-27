package com.example.bicycleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

// This is the Home page

public class Payment extends AppCompatActivity {
    Button PaymentBtn,ScannerBtn;
    TextView Hello_Text,station1,station2;
    ImageView profile_pic;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID ;
    boolean paymentInfoSaved;
    private String QR_Value;
    private boolean state;
    private FirebaseDatabase fDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        station1 = findViewById(R.id.BikeCountStation1);
        station2 = findViewById(R.id.BikeCountStation2);
       //PaymentBtn = findViewById(R.id.PaymentBtn);
        ScannerBtn = findViewById(R.id.PaymentBtn);
        Hello_Text = findViewById(R.id.Hello_Text);
        profile_pic = findViewById(R.id.profile_pic);
        String fullName = UserDataManager.getInstance().getFullName();

        // Implement OnBackPressedDispatcher for back button navigation
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to the previous activity
                Intent loginIntent = new Intent(Payment.this, MainActivity.class);
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
        fDatabase = FirebaseDatabase.getInstance();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });



        DocumentReference docRef1 = fStore.collection("Users").document(userID);
        docRef1.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                String fullName = value.getString("User Name");
                String firstName = fullName.substring(0,fullName.indexOf(' '));
                Hello_Text.setText("Hello "+firstName+"!");

            }
        });

        /*//Check payment details is saved
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = fStore.collection("Payment details").document(currentUserId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        boolean hasCardType = document.contains("Card type");  // Check for field existence
                        boolean hasCardNumber = document.get("Card number") != null;  // Check for non-null value

                        if (hasCardType && hasCardNumber) {
                            // All required fields are filled
                            PaymentBtn.setText("Payment details saved");
                            paymentInfoSaved = true;
                        } else {
                            // Some fields are missing
                            Log.d(TAG, "Missing fields: " + (hasCardType ? "" : "Card type") +
                                    (hasCardNumber ? "" : ", Card number"));
                            paymentInfoSaved = false;
                        }
                    } else {
                        // Document doesn't exist
                        Log.d(TAG, "Document does not exist");
                        paymentInfoSaved = false;
                    }
                } else {
                    // Handle errors during document retrieval
                    Log.w(TAG, "Error getting document:", task.getException());
                    paymentInfoSaved = false;
                }
            }
        });*/




        DocumentReference docRef2 = fStore.collection("Stations").document("Station1");
        docRef2.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null) {
                    // Retrieve integer field
                    Long bicycleCount = value.getLong("Bicycle count");
                    String bicycle_count = String.valueOf(bicycleCount != null ? bicycleCount.intValue() : 0);
                    station1.setText(bicycle_count);
                }
            }
        });

        DocumentReference docRef3 = fStore.collection("Stations").document("Station2");
        docRef3.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null) {
                    // Retrieve integer field
                    Long bicycleCount = value.getLong("Bicycle count");
                    String bicycle_count = String.valueOf(bicycleCount != null ? bicycleCount.intValue() : 0);
                    station2.setText(bicycle_count);
                }
            }
        });


        /*PaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Payment.this,com.example.bicycleapp.PaymentGateway.class);
                startActivity(intent);
                finish();
            }
        });*/

        ScannerBtn.setOnClickListener(new View.OnClickListener() {  //Scan button
            @Override
            public void onClick(View v) {
                //if (paymentInfoSaved){
                    IntentIntegrator intentIntegrator = new IntentIntegrator(Payment.this);
                    intentIntegrator.setOrientationLocked(true); // This should lock the orientation
                    intentIntegrator.setPrompt("Scanning");
                    intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                    intentIntegrator.initiateScan();


               // }
               // else {
                //    Toast.makeText(Payment.this,"Please save payment details!",Toast.LENGTH_SHORT).show();
               // }
            }
        });

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Payment.this, Profile.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
                QR_Value = contents;
                // Check QR code validity
                state = QR_Validity(QR_Value);
                if (state) {    // If QR code is valid
                    // Send data into firebase
                    //updateFirebaseStations(QR_Value);   //Update station
                    updateFirebaseRealtimeDatabaseFromApp(QR_Value);   //Update realtime database
                    //updateFirebaseCurrentStates(QR_Value);  //Update current states
                    //startLocationUpdates(); // Start location updates
                    //getCurrentTime(); // Update start time
                } else {
                    Toast.makeText(Payment.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Scan failed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Check QR value validity
    private boolean QR_Validity(String input) {
        boolean state = false;
        // Check if the string has at least 2 characters
        if (input.length() == 4) {
            // Extract the first 2 characters
            String firstThreeChars = input.substring(0, 3);
            // Compare with "1CS"
            if (firstThreeChars.equals("1CS")) {
                state = true;
            } else {
                state = false;
            }
        } else {
            state = false;
        }
        return state;
    }

    // Update realtime database from mobile app
    private void updateFirebaseRealtimeDatabaseFromApp(String value) {
        // Extract station number from the 4th character of QR code data
        int stationNumber = Character.getNumericValue(value.charAt(3)); // Assuming 4th character

        // Update the station value based on station number (directly update Station1 or Station2)
        String stationId = "Door" + stationNumber;

        int newValue = 0; // Replace with your logic to determine the new value (e.g., 0 for no bicycles)

        // Update the Firebase Realtime Database
        fDatabase.getReference().child(stationId).setValue(newValue)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully updated the database
                        Toast.makeText(getApplicationContext(), "Update Door" + stationId + " successful", Toast.LENGTH_SHORT).show();
                        // Add any further actions upon success, if needed
                        Intent intent2 = new Intent(Payment.this,com.example.bicycleapp.HomePage.class);
                        startActivity(intent2);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update the database
                        Toast.makeText(getApplicationContext(), "Failed to update Door" + stationId, Toast.LENGTH_SHORT).show();
                        // Add any error handling code here
                    }
                });
    }


}
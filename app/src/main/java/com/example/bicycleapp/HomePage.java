package com.example.bicycleapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class HomePage extends AppCompatActivity {
    String QR_Value;
    boolean state;
    Button btnTakeRide;
    ImageView imgHome, imgProfile, imgLogout;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    Location userLocation;
    double Lon, Lat;
    FusedLocationProviderClient fusedLocationProviderClient;
    private final static int REQUEST_CODE = 100;
    private FirebaseDatabase fDatabase;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private int stationNumber;
    private int bicycleNumber;
    private int passInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Initialize Firebase Realtime Database instance
        fDatabase = FirebaseDatabase.getInstance();
        passInt = getIntent().getIntExtra("StartStation", 0);

        // Implement OnBackPressedDispatcher for back button navigation
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to the previous activity
                Intent loginIntent = new Intent(HomePage.this, Payment.class);
                // Optionally, clear the activity stack to prevent back button from returning to HomePage
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish(); // Close HomePage
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnTakeRide = findViewById(R.id.btnTakeRide);
        imgHome = findViewById(R.id.imgHome);
        imgProfile = findViewById(R.id.imgProfile);
        imgLogout = findViewById(R.id.imgExit);

        imgHome.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Payment.class);
            startActivity(intent);
            finish();
        });

        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Profile.class);
            startActivity(intent);
            finish();
        });

        imgLogout.setOnClickListener(v -> {
            Toast.makeText(HomePage.this, "Successfully Exit", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePage.this, MainActivity.class);
            startActivity(intent);
            finish();
        });



        // QR scanner
        btnTakeRide.setOnClickListener(v -> {
            // Check if location services are enabled
            if (isLocationEnabled()) {  // If location services are enabled, start the task
                // QR scanner
                IntentIntegrator intentIntegrator = new IntentIntegrator(HomePage.this);
                intentIntegrator.setOrientationLocked(true); // This should lock the orientation
                intentIntegrator.setPrompt("Scanning");
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.initiateScan();


            } else {    // If location services are not enabled, prompt the user to enable them
                // You can prompt the user to enable location services, but do not initiate scan or tasks
                Toast.makeText(HomePage.this, "Location unavailable!\nPlease enable location", Toast.LENGTH_SHORT).show();
                // Optionally, you can implement logic to wait for location services to be enabled
                // and handle the button click again when they are enabled.
            }
        });

    }   //End of onCreate method

    // Handle result from QR code scan
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
                    updateFirebaseStations(QR_Value);   //Update station
                    updateFirebaseRealtimeDatabaseFromApp(QR_Value);   //Update realtime database
                    updateFirebaseCurrentStates(QR_Value);  //Update current states
                    //startLocationUpdates(); // Start location updates
                    getCurrentTime(); // Update start time

                    // Delayed execution after 5 seconds
                    new Handler().postDelayed(() -> {
                        // Call your method here after 5 seconds
                        goToOnRide();
                    }, 5000); // 5000 milliseconds = 5 seconds
                } else {
                    Toast.makeText(HomePage.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Scan failed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Check lock QR value validity
    private boolean QR_Validity(String input) {
        boolean state = false;
        // Check if the string has at least 4 characters
        if (input.length() == 4) {
            // Extract the first 2 characters
            String firstTwoChars = input.substring(0, 3);
            // Compare with "CSB"
            if (firstTwoChars.equals("CSB")) {
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

        // Update the station value based on station number (directly update Station1 or Station2)
        String stationId = "Station" + passInt;

        int newValue = 0; // Replace with your logic to determine the new value (e.g., 0 for no bicycles)
        fDatabase.getReference().child(stationId).setValue(newValue);  // No "Stations" node
    }

    // Update firebase Station
    private void updateFirebaseStations(String value) {

        // Create a new station object with fields
        Map<String, Object> stationData = new HashMap<>();
        stationData.put("Availability", false);
        stationData.put("Bicycle count", 0);

        // Update the document in Firestore based on station number
        fStore.collection("Stations")
                .document("Station" + passInt)
                .update(stationData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(HomePage.this, "Station " + passInt + " updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "Error updating station: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Update firebase Current states (Station number and userID)
    private void updateFirebaseCurrentStates(String value) {

        bicycleNumber = Character.getNumericValue(value.charAt(3)); // Extract bicycle number from QR code

        // Create a new object with fields
        Map<String, Object> currentStatesData = new HashMap<>();
        currentStatesData.put("Start station", "Station"+passInt);
        currentStatesData.put("User ID", userID);

        // Update the document in Firestore
        fStore.collection("Current states")
                .document("Bicycle"+bicycleNumber)
                .update(currentStatesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(HomePage.this, "Current states updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "Error updating current states: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to start location updates
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Toast.makeText(HomePage.this, "Location updates started", Toast.LENGTH_SHORT).show();
        } else {
            askPermission();
        }
    }

    // Method to stop location updates
    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    // Method to update location in Firestore
    private void updateLocationInFirestore(double latitude, double longitude) {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        fStore.collection("Current states").document("Bicycle1")
                .update("Location", geoPoint)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(HomePage.this, "Location updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "Failed to update location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Check if location services are enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) /*|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)*/;
    }

    // Request location permissions
    private void askPermission() {
        ActivityCompat.requestPermissions(HomePage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startLocationUpdates();
            } else {
                Toast.makeText(HomePage.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Clean up resources when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopLocationUpdates(); // Stop location updates to prevent memory leaks
    }

    // Get current timestamp
    private void getCurrentTime() {
        Timestamp timestamp = Timestamp.now();    // Get current timestamp
        Map<String, Object> timeData = new HashMap<>();
        timeData.put("Ride start time", timestamp);
        fStore.collection("Current states")
                .document("Bicycle"+bicycleNumber)
                .update(timeData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(HomePage.this, "Current time updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "Error updating current time: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Dummy task method
    private void startTask() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Perform your task here
                Toast.makeText(HomePage.this, "Location update in progress", Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, 10000); // Repeat task every 10 seconds
            }
        };
        handler.postDelayed(runnable, 10000); // Start task after 10 seconds
    }
    private void goToOnRide(){
        Intent intent10 = new Intent(HomePage.this,com.example.bicycleapp.OnRide.class);
        startActivity(intent10);
    }


}
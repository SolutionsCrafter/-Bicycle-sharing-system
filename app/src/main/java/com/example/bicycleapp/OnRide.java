package com.example.bicycleapp;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class OnRide extends AppCompatActivity {

    // Variable declarations
    private TextView tvStart, tvEnd, tvStartTime, tvEndTime, tvDocCharge, tvRideCost, tvTotalCost,tvRideOn;
    private Button btnParkScan;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseDatabase fDatabase;
    private String userID;
    private String QR_Value;
    private boolean state;

    // SharedPreferences variables
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    FusedLocationProviderClient fusedLocationProviderClient;
    private final static int REQUEST_CODE = 100;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private String formattedTime;
    private String currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_ride);

        initializeUI();
        initializeFirebase();
        retrieveCurrentStatesData();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("RideData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Initialize location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Initialize LocationRequest and LocationCallback for periodic updates
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocationProcess();

        // Schedule the method to run after 1 minute
        scheduleMethodWithDelay(60000); // 60000 milliseconds = 1 minute

    }   //End of onCreate method

    // Initialize UI elements
    private void initializeUI() {
        tvStart = findViewById(R.id.tvStart);
        tvEnd = findViewById(R.id.tvEnd);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvRideCost = findViewById(R.id.tvRideCost);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvRideOn = findViewById(R.id.tvRideOn);
        btnParkScan = findViewById(R.id.parkStationScan);

        // Set up button click listeners
        btnParkScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator intentIntegrator = new IntentIntegrator(OnRide.this);
                intentIntegrator.setOrientationLocked(true); // This should lock the orientation
                intentIntegrator.setPrompt("Scanning");
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.initiateScan();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Firebase initialization
    private void initializeFirebase() {
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        userID = fAuth.getCurrentUser().getUid();
    }

    // Retrieve current states data from Firebase Firestore
    private void retrieveCurrentStatesData() {
        DocumentReference docRef1 = fStore.collection("Current states").document("Bicycle1");
        docRef1.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Error retrieving document: ", error);
                    return;
                }

                // Get start station and time
                if (value != null && value.exists()) {
                    // Handle String fields
                    String startStation = value.getString("Start station");
                    tvStart.setText(startStation);

                    // Store start station in SharedPreferences
                    editor.putString("startStation", startStation);

                    // Handle Timestamp fields
                    Timestamp rideStartTimeStamp = value.getTimestamp("Ride start time");
                    if (rideStartTimeStamp != null) {
                        Date rideStartTime = rideStartTimeStamp.toDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        formattedTime = sdf.format(rideStartTime);
                        tvStartTime.setText(formattedTime);

                        // Store formatted time in SharedPreferences
                        editor.putString("formattedTime", formattedTime);
                    }
                    editor.apply();
                } else {
                    Log.d(TAG, "Document does not exist or is empty");
                }
            }
        });
    }

    //handle QR code result
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
                    Toast.makeText(OnRide.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
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

        // Update the Firebase Realtime Database for parking station
        fDatabase.getReference().child(stationId).setValue(newValue)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully updated the database
                        Toast.makeText(getApplicationContext(), "Update Door" + stationId + " successful", Toast.LENGTH_SHORT).show();
                        endRideAndClearData();
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


    // Method to end the ride and clear SharedPreferences data
    private void endRideAndClearData() {
        // Clear SharedPreferences data
        editor.clear();
        editor.apply();

        clearSharedPreferences();
        stopLocationUpdates();
        ClearCurrentStatesData();
        deleteFromMissingRiders();

        // Notify the user
        Toast.makeText(OnRide.this, "Ride ended and data cleared", Toast.LENGTH_SHORT).show();

        // Redirect to HomePage or another appropriate activity
        //Intent intent = new Intent(OnRide.this, Payment.class);
        //startActivity(intent);
        //finish();
    }

    private void clearSharedPreferences() {
        // Initialize SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("RideData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Clear all data in SharedPreferences
        editor.clear();
        editor.apply();

        // Notify the user or handle further operations if needed
        Toast.makeText(OnRide.this, "SharedPreferences cleared", Toast.LENGTH_SHORT).show();
    }


    //Clear current states data
    private void ClearCurrentStatesData(){
        // Reference to the document
        DocumentReference docRef2 = FirebaseFirestore.getInstance().collection("Current states").document("Bicycle1");

        // Create a batch operation
        WriteBatch batch = FirebaseFirestore.getInstance().batch();

        // Set each field to null
        batch.update(docRef2, "Location", null);
        batch.update(docRef2, "Ride Duration", null);
        batch.update(docRef2, "Ride end time", null);
        batch.update(docRef2, "Ride start time", null);
        batch.update(docRef2, "Start station", null);
        batch.update(docRef2, "User ID", null);

        // Commit the batch
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(OnRide.this,"Current states data cleared successfully",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OnRide.this,"Current states data clear unsuccessful",Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Location update process
    private void startLocationProcess() {

        // Check if location services are enabled
        if (isLocationEnabled()) {  // If location services are enabled, start the task

            //startTask();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        updateLocationInFirestore(location.getLatitude(), location.getLongitude());
                    }
                }
            };

            // Start location updates if permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                askPermission();
            }

        } else {    // If location services are not enabled, prompt the user to enable them
            // You can prompt the user to enable location services, but do not initiate scan or tasks
            Toast.makeText(OnRide.this, "Location unavailable!\nPlease enable location", Toast.LENGTH_SHORT).show();
            // Optionally, you can implement logic to wait for location services to be enabled
            // and handle the button click again when they are enabled.
        }
    }

    // Check if location services are enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) /*|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)*/;
    }

    // Method to update location in Firestore
    private void updateLocationInFirestore(double latitude, double longitude) {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        fStore.collection("Current states").document("Bicycle1")
                .update("Location", geoPoint)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(OnRide.this, "Location updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OnRide.this, "Failed to update location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to start location updates
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Toast.makeText(OnRide.this, "Location updates started", Toast.LENGTH_SHORT).show();
        } else {
            askPermission();
        }
    }

    // Request location permissions
    private void askPermission() {
        ActivityCompat.requestPermissions(OnRide.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    // update location every 10 seconds
    /*private void startTask() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Perform your task here
                Toast.makeText(OnRide.this, "Location update in progress", Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, 10000); // Repeat task every 10 seconds
            }
        };
        handler.postDelayed(runnable, 10000); // Start task after 10 seconds
    }*/

    // Method to stop location updates
    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    //getCurrentTime
    private void getCurrentTime(){
        LocalDateTime localDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        currentTime = localDateTime.format(formatter);
    }

    //delay run method to check missing riders
    private void scheduleMethodWithDelay(long delayMillis) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkIsRiderMissing();
            }
        }, delayMillis);
    }

    //Check ride end time is update or not after 4 hours
    private void checkIsRiderMissing() {
        DocumentReference docRef3 = fStore.collection("Current states").document("Bicycle1");

        docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot2 = task.getResult();
                    if (documentSnapshot2.exists()){
                        if (documentSnapshot2.contains("Ride end time")){
                            Timestamp timestamp2 = documentSnapshot2.getTimestamp("Ride end time");
                            if (timestamp2 == null){
                                updateMissingRiders();
                            }
                        }
                    }
                }
            }
        });

    }

    //Update missing riders
    private void updateMissingRiders(){
        DocumentReference docRef4 = fStore.collection("Missing riders").document("Bicycle1");

        docRef4.update("User ID",userID);
    }

    //Clear from missing riders data
    private void deleteFromMissingRiders(){

        // Reference to the document and collection
        DocumentReference docRef = fStore.collection("Missing riders").document("Bicycle1");

        // Create a batch operation
        WriteBatch batch = fStore.batch();

        // Set "User ID" field to null or delete it
        batch.update(docRef, "User ID", null);

        // Commit the batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Field deleted successfully");
                    Toast.makeText(OnRide.this, "Missing riders deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Error deleting field", task.getException());
                    Toast.makeText(OnRide.this, "Error deleting Missing riders", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Cost calculate
    private void costCalculate(){

    }


}

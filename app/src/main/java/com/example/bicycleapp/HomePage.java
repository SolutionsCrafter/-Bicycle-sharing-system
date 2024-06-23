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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
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
    private Handler handler;
    private Runnable locationRunnable;
    private FirebaseDatabase fDatabase;

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

        fAuth = FirebaseAuth.getInstance();
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
                // intentIntegrator.setCaptureActivity(CustomCaptureActivity.class);
                intentIntegrator.initiateScan();
                startTask();
            } else {    // If location services are not enabled, prompt the user to enable them
                //checkLocationSettings();
                Toast.makeText(HomePage.this, "Location unavailable!\nPlease enable location", Toast.LENGTH_SHORT).show();
            }
        });


    }

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
                    startLocationUpdates(); //Update current states
                    getCurrentTime(); //Update current states

                    //remove this method later
                    updateFirebaseRealtimeDatabaseFromESP(QR_Value);

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

    //Check QR value validity
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
            //Toast.makeText(HomePage.this,"Invalid QR code!",Toast.LENGTH_SHORT).show();
            state = false;
        }
        return state;
    }


    // Update realtime database from mobile app
    private void updateFirebaseRealtimeDatabaseFromApp(String value) {

        // Extract station number from the 4th character of QR code data
        int stationNumber = Character.getNumericValue(value.charAt(3)); // Assuming 4th character

        // Update the station value based on station number (directly update Station1 or Station2)
        String stationId = "Station" + stationNumber;

        int newValue = 0; // Replace with your logic to determine the new value (e.g., 0 for no bicycles)
        fDatabase.getReference().child(stationId).setValue(newValue);  // No "Stations" node
    }

    //Update realtime database from ESP-32
    private void updateFirebaseRealtimeDatabaseFromESP(String value) {

        // Extract station number from the 4th character of QR code data
        int stationNumber = Character.getNumericValue(value.charAt(3)); // Assuming 4th character

        // Update the station value based on station number (directly update Station1 or Station2)
        String stationId = "Station2";

        int newValue = 1; // Replace with your logic to determine the new value (e.g., 0 for no bicycles)
        fDatabase.getReference().child(stationId).setValue(newValue);  // No "Stations" node
    }





    //Update firebase Station
    private void updateFirebaseStations(String value) {
        int station_NO = Character.getNumericValue(value.charAt(3)); // Extract station number from QR code

        // Create a new station object with fields
        Map<String, Object> stationData = new HashMap<>();

        // Check if userLocation is available before adding it


        stationData.put("Availability", false);
        stationData.put("Bicycle count", 0);

        // Update the document in Firestore based on station number
        fStore.collection("Stations")
                .document("Station" + station_NO)
                .update(stationData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(HomePage.this, "Station " + station_NO + " updated successfully", Toast.LENGTH_SHORT).show();
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
        int station_NO = Character.getNumericValue(value.charAt(3)); // Extract station number from QR code


        // Create a new station object with fields
        Map<String, Object> currentSatesData = new HashMap<>();
        //currentSatesData.put("Location", userLocation);
        currentSatesData.put("Start Station", "Station " + station_NO);
        //currentSatesData.put("Time", 1);
        currentSatesData.put("User ID", userID);

        //Get userID
        DocumentReference docRef = fStore.collection("Users").document(userID);

        // Update the document in Firestore based on station number
        fStore.collection("Current states")
                .document("Bicycle1")
                .update(currentSatesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(HomePage.this, "Bicycle " + station_NO + " updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "Error updating current state: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
    }


    // Method to check if location services are enabled
    private boolean isLocationEnabled() {
        // Get the LocationManager from the system services
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if either GPS or network provider is enabled
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // Method to start the specific task
    private void startTask() {
        // Your specific task code here
        Toast.makeText(this, "Scan a QR code", Toast.LENGTH_SHORT).show();
    }


    // Update firebase Current states (Feed Location every 10 second)
    private void startLocationUpdates() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Initialize handler and runnable for periodic updates
            handler = new Handler(Looper.getMainLooper());
            locationRunnable = new Runnable() {
                @Override
                public void run() {
                    getLastLocation(); // Get the last known location
                    handler.postDelayed(this, 10000); // Repeat every 10 seconds
                }
            };
            handler.post(locationRunnable); // Start the runnable
            Toast.makeText(HomePage.this, "Location updates started", Toast.LENGTH_SHORT).show();
        } else {
            askPermission(); // Request location permission if not granted
        }
    }

    // Method to get the last known location
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                updateLocationInFirestore(location.getLatitude(), location.getLongitude());
                            }
                        }
                    });
        }
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


    // Method to request location permission
    private void askPermission() {
        ActivityCompat.requestPermissions(HomePage.this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        }, REQUEST_CODE);
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Check if the request code matches
        if (requestCode == REQUEST_CODE) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates(); // Start location updates if permission was granted
            } else {
                // Notify the user that location service is required
                Toast.makeText(HomePage.this, "Location service required", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Clean up resources when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the location updates runnable to prevent memory leaks
        if (handler != null && locationRunnable != null) {
            handler.removeCallbacks(locationRunnable);
        }
    }

    //Get current time
    private void getCurrentTime(){
        Timestamp timestamp = Timestamp.now();
        updateFirebaseWithTimestamp(timestamp);
    }

    // Update firebase Current states (Start time)
    private void updateFirebaseWithTimestamp(Timestamp timestamp) {
        Map<String, Object> timestampData = new HashMap<>();
        timestampData.put("Ride Start", timestamp);

        fStore.collection("Current states").document("Bicycle1")
                .update(timestampData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(HomePage.this, "Timestamp updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, "Error updating timestamp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

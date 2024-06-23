package com.example.bicycleapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class location extends AppCompatActivity {
    TextView tvLon, tvLat;
    Button getLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private final static int REQUEST_CODE = 100;
    private Handler handler;
    private Runnable locationRunnable;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        tvLon = findViewById(R.id.tvLon);
        tvLat = findViewById(R.id.tvLat);
        getLocation = findViewById(R.id.btn);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();
            }
        });
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            handler = new Handler(Looper.getMainLooper());
            locationRunnable = new Runnable() {
                @Override
                public void run() {
                    getLastLocation();
                    handler.postDelayed(this, 10000); // 10 seconds
                }
            };
            handler.post(locationRunnable);
        } else {
            askPermission();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(location.this, Locale.getDefault());
                            List<Address> addresses;
                            try {
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    double latitude = addresses.get(0).getLatitude();
                                    double longitude = addresses.get(0).getLongitude();

                                    tvLat.setText("Latitude :" + latitude);
                                    tvLon.setText("Longitude :" + longitude);

                                    // Update Firestore with new location
                                    updateLocationInFirestore(latitude, longitude);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void updateLocationInFirestore(double latitude, double longitude) {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        firestore.collection("locations").document("yourDocumentId")
                .update("geoPointField", geoPoint)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Location updated successfully
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed to update location
                    Toast.makeText(location.this, "Failed to update location", Toast.LENGTH_SHORT).show();
                });
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(location.this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        }, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(location.this, "Location service required", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && locationRunnable != null) {
            handler.removeCallbacks(locationRunnable);
        }
    }
}

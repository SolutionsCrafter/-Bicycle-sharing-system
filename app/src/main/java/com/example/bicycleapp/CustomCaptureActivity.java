package com.example.bicycleapp;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import com.journeyapps.barcodescanner.CaptureActivity;


public class CustomCaptureActivity extends CaptureActivity {
        // Override methods if necessary
        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            // Handle orientation change
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            

        }
}

package com.example.phonehelper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import android.net.T;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
//import android.net.TetheringManager;
import android.net.wifi.WifiManager;
import android.widget.Toast;

//import androidx.core.accessibilityservice.AccessibilityServiceConnection;
//import android.accessibilityservice.AccessibilityServiceConnection;





public class OpenPersonalHotspot extends AppCompatActivity {


    private static final String TAG = "OpenPersonalHotspot";
    private static final int PERMISSIONS_REQUEST_CODE = 123;

    private WifiManager.LocalOnlyHotspotReservation mReservation;


//    private AccessibilityServiceConnection connection;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_personal_hotspot);


        requestLocationPermission();


        Button btnWifi = findViewById(R.id.button);
        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MyApplication.getContext(), "111", Toast.LENGTH_SHORT).show();


                startHotspot("aaaaaa","123456789");
            }
        });

        Button btnWifiClose = findViewById(R.id.buttonclose);
        btnWifiClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopHotspot();
            }
        });
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "申请授权定位权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权了定位权限，执行需要定位权限的操作
                Toast.makeText(this, "授权了定位权限",
                        Toast.LENGTH_SHORT).show();
            } else {
                // 用户拒绝了定位权限，可以给出相应提示或者执行其他操作
                Toast.makeText(this, "Location permission is required to start hotspot",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }



    private void startHotspot(String ssid, String password) {
        // Check if the device supports hotspot
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            // Hotspot is not supported
            return;
        }

        // Get a WifiManager instance
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            // WifiManager is not available
            return;
        }

//        if (wifiManager.isWifiEnabled()) {
//            wifiManager.setWifiEnabled(false);
//        }

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        config.preSharedKey = password;
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        // Start the local-only hotspot
        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);

                // Save the reservation for later use
                mReservation = reservation;
                Log.d("Hotspot", "SSID: " + ssid + ", Password: " + password);


                // Get the hotspot configuration
                WifiConfiguration config = reservation.getWifiConfiguration();
                if (config == null) {
                    // Configuration is not available
                    return;
                }

                // Set the SSID and password
                config.SSID = ssid;
                config.preSharedKey = password;

//                String ssid = reservation.getWifiConfiguration().SSID;
//                String password = reservation.getWifiConfiguration().preSharedKey;

                // Update the hotspot configuration
                try {


                    Log.d("Hotspot", "SSID1110: " + ssid + ", Password111: " + password);

//                    Method setWifiApConfiguration = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                    Method setWifiApConfiguration = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class, boolean.class);

                    Log.d("Hotspot", "SSID11101: " + ssid + ", Password111: " + password);

                    setWifiApConfiguration.invoke(wifiManager, config,true);
                    Log.d("Hotspot", "SSID111: " + ssid + ", Password111: " + password);

                } catch (Exception e) {
                    // Failed to update the configuration
                    Log.e("Hotspot", "Failed to set hotspot configuration: " + e.getMessage());

                }
            }

            @Override
            public void onStopped() {
                super.onStopped();

                // Release the reservation
                mReservation = null;
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);

                // Hotspot failed to start
            }
        }, new Handler());
    }

    private void stopHotspot() {
        if (mReservation != null) {
            mReservation.close();
            mReservation = null;
        }
    }

}















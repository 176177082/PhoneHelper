package com.example.phonehelper.BLE;

import android.os.Build;

public class BLE {
    public static String getCpuVersion() {
        return "CPU Version: " + Build.VERSION.RELEASE;
    }

}

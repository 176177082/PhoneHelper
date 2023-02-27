package com.example.phonehelper.cpu;

import android.os.Build;

public class CpuInfo {

    public static String getCpuVersion() {
        return "CPU Version: " + Build.VERSION.RELEASE;
    }
}

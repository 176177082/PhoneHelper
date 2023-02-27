package com.example.phonehelper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.phonehelper.cpu.CpuInfo;

public class phoneinfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoneinfo);
//        CpuInfo.getCpuVersion();
    }
}
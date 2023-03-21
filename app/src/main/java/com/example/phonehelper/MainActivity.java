package com.example.phonehelper;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonehelper.controlyy.BaseAccessibilityService;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseAccessibilityService instance = BaseAccessibilityService.getInstance();
        instance.init(this);

        Button btnYytest = findViewById(R.id.buttonyytest);
        btnYytest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!instance.checkAccessibilityEnabled(getResources().getString(R.string.access_name))) {
                    instance.goAccess();
                }
            }
        });



        Button btnVolume = findViewById(R.id.buttongosystemcontrol);
        btnVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyApplication.getContext(), ControlSystemVoiceActivity.class);
                startActivity(intent);
            }
        });


        Button btnwifitest = findViewById(R.id.buttonwifitest);
        btnwifitest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyApplication.getContext(), OpenPersonalHotspot.class);
                startActivity(intent);
            }
        });




    }



}
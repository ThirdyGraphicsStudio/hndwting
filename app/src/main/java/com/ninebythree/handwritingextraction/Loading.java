package com.ninebythree.handwritingextraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;


public class Loading extends AppCompatActivity {

    private static final int DELAY_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);



        // Delay for 2 seconds and start the new activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                startActivity(new Intent(getApplicationContext(), Welcome_page.class));

                finish(); // Optional, if you want to close the current activity
            }
        }, DELAY_DURATION);


    }
}

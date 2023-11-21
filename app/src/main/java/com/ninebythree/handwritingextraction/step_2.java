package com.ninebythree.handwritingextraction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class step_2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2);

        ImageView backIcon = findViewById(R.id.backIcon1);
        Button skipButton = findViewById(R.id.button5);
        Button nextButton = findViewById(R.id.button6);

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToStep1();
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMainActivity();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToStep3();
            }
        });
    }

    private void navigateToStep1() {
        Intent intent = new Intent(this, step_1.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToStep3() {
        Intent intent = new Intent(this, step_3.class); // Assuming you have a step_3 activity
        startActivity(intent);
        finish();
    }
}

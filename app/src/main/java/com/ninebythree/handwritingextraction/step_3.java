package com.ninebythree.handwritingextraction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class step_3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step3);

        ImageView backIcon2 = findViewById(R.id.backIcon2);
        Button skipButton = findViewById(R.id.button7);
        Button nextButton = findViewById(R.id.button8);

        backIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToStep2();
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
                navigateToStep4();
            }
        });
    }

    private void navigateToStep2() {
        Intent intent = new Intent(this, step_2.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToStep4() {
        Intent intent = new Intent(this, step_4.class);
        startActivity(intent);
        finish();
    }
}

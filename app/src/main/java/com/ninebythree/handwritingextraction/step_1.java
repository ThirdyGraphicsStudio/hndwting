package com.ninebythree.handwritingextraction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class step_1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step1);

        ImageView backIcon = findViewById(R.id.backIcon);
        Button skipButton = findViewById(R.id.button3);
        Button nextButton = findViewById(R.id.button4);

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToWelcomePage();
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
                navigateToStep2();
            }
        });
    }

    private void navigateToWelcomePage() {
        Intent intent = new Intent(this, Welcome_page.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToStep2() {
        Intent intent = new Intent(this, step_2.class);
        startActivity(intent);
        finish();
    }
}

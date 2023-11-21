package com.ninebythree.handwritingextraction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class step_4 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step4);

        ImageView backIcon3 = findViewById(R.id.backIcon3);
        Button nextButton = findViewById(R.id.button10);

        backIcon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToStep3();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMainActivity();
            }
        });
    }

    private void navigateToStep3() {
        Intent intent = new Intent(this, step_3.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

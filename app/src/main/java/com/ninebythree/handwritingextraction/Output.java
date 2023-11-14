package com.ninebythree.handwritingextraction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

public class Output extends AppCompatActivity {

    private  EditText et_output;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        et_output = findViewById(R.id.et_output);

        Intent intent = getIntent();
        String output = intent.getStringExtra("output");
        et_output.setText(output);



    }
}
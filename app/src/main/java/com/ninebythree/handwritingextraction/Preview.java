package com.ninebythree.handwritingextraction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class Preview extends AppCompatActivity {

    private ImageView imgPreview;
    private MaterialButton btnExtract;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        imgPreview = findViewById(R.id.imgPreview);
        btnExtract = findViewById(R.id.btnExtract);
        progressBar = findViewById(R.id.progressBar);
        Intent intent = getIntent();
        Uri imageUri = intent.getParcelableExtra("imageUri");
        imgPreview.setImageURI(imageUri);

        btnExtract.setOnClickListener(v -> {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            sendRequest(imageUri);
        });
    }

    private String encodeImageFromUri(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(byteBuffer.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    private void sendRequest(Uri imageUri) {
        String apiKey = "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";
        String base64Image = encodeImageFromUri(imageUri);

        new Thread(() -> {
            try {
                URL url = new URL("https://api.openai.com/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("OpenAI-Organization", "org-Se9r5Wt1g4BnyvcLVPi18qBi");
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("model", "gpt-4-vision-preview");

                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("role", "user");

                JSONArray contentArray = new JSONArray();

                JSONObject textContent = new JSONObject();
                textContent.put("type", "text");
                textContent.put("text", "Convert this to plain english text, if the image is not english and not handwritten text , please  reply ERROR");
                contentArray.put(textContent);

                JSONObject imageContent = new JSONObject();
                imageContent.put("type", "image_url");
                JSONObject imageUrlObject = new JSONObject();
                imageUrlObject.put("url", "data:image/jpeg;base64," + base64Image);
                imageContent.put("image_url", imageUrlObject);
                contentArray.put(imageContent);

                message.put("content", contentArray);
                messages.put(message);
                payload.put("messages", messages);
                payload.put("max_tokens", 300);

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (Scanner scanner = new Scanner(conn.getInputStream())) {
                        String jsonResponse = scanner.useDelimiter("\\A").next();
                        JSONObject obj = new JSONObject(jsonResponse);
                        JSONArray choices = obj.getJSONArray("choices");
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject messageses = firstChoice.getJSONObject("message");
                        String content = messageses.getString("content");

                        runOnUiThread(() -> {
                                    Intent intent = new Intent(Preview.this, Output.class);
                                    intent.putExtra("output", content);
                                    startActivity(intent);
                                }

                        );
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Preview.this, "Failed with response code: " + responseCode, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Preview.this, "Error: " , Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}

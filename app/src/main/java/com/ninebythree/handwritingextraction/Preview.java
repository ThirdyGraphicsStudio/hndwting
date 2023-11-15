package com.ninebythree.handwritingextraction;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;



import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Preview extends AppCompatActivity {

    private ImageView imgPreview;
    private MaterialButton btnExtract;
    private ProgressBar progressBar;
    String extractedText;

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
            try {
                sendapi(imageUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void sendapi(Uri imageUri) throws IOException {

        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // You can define the size of the buffer as you wish. This is just an example.
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        // Here we get the imageBytes that you need for the RequestBody
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Create the file part as before
        RequestBody filePart = RequestBody.create(
                MediaType.parse(getContentResolver().getType(imageUri)),
                imageBytes
        );
        MultipartBody.Part file = MultipartBody.Part.createFormData("file", "image.jpg", filePart);

// Create the MultipartBody
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(file)
                .build();

// Build the request with the MultipartBody
        Request request = new Request.Builder()
                .url("https://handwriting-vztw.onrender.com/upload")
                .post(requestBody) // pass the MultipartBody here, not the Part
                .build();


        // Initialize the OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // Enqueue the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the error
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Handle the response

                    // Parse the responseData string into a JSONObject
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        // Extract the text from the JSONObject
                         extractedText = jsonObject.getString("extracted_text");

                        if (extractedText.length() > 80) {
                            extractedText = extractedText.substring(0, 80);
                        }


                        // Log the extracted text
                        Log.d("extractedText", extractedText);

                        // If you need to update the UI, make sure you do so on the main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(ProgressBar.GONE);
                                Intent intent = new Intent(Preview.this, Output.class);
                                intent.putExtra("output", extractedText);
                                startActivity(intent);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle JSON parsing error
                    }
                }
            }
        });
    }




    // Helper function to convert Uri to Bitmap
    private Bitmap getBitmapFromUri(Uri uri) {
        Bitmap bitmap = null;
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    private void sendapi2(Uri imageUri) {
        String base64Image = encodeImageFromUri(imageUri);

        new Thread(() -> {
            try {

                URL url = new URL("https://handwriting-vztw.onrender.com/upload"); // Replace with your Flask app's URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Create JSON Payload
                JSONObject payload = new JSONObject();
                payload.put("file", base64Image);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Handle successful response
                    Log.d("extractedText", "successful");

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        String jsonResponse = response.toString();
                        // Process the response as needed
                        // For example, parse the JSON and extract the extracted text
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        String extractedText = responseObject.getString("extracted_text");
                        runOnUiThread(() -> {
                            // Update UI with extracted text
                            // Example: textView.setText(extractedText);
                            Log.d("extractedText", extractedText);
                        });
                    }
                } else {
                    Log.d("extractedText", "error");

                    // Handle error response
                    runOnUiThread(() -> {
                        // Show error message in UI
                         Toast.makeText(Preview.this, "Error: " + responseCode, Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Show error message in UI
                    Toast.makeText(Preview.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }


    // Helper function to get the file name from Uri


    private byte[] uriToByteArray(Uri uri, Context context) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteArrayOutputStream.toByteArray();
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

package com.ninebythree.handwritingextraction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import android.Manifest;

public class Output extends AppCompatActivity  {
    File myPath;
    int totalHeight, totalWidth;

    private  EditText et_output;
    String path,imageUri,file_name = "Download";
    Bitmap bitmap;
    private MaterialButton btnDownload, btnDocs,btnCheck;
    private LinearLayout linearLayout;
    NestedScrollView parentRelativeLayout;
    private ProgressBar progressBar;
    Display receiptDisplay;
    private File filePath = null;
    private static final int MY_PERMISSIONS_REQUEST_CODE = 123;
    private MaterialButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        et_output = findViewById(R.id.et_output);
        btnCheck = findViewById(R.id.btnCheck);
        Intent intent = getIntent();
        String output = intent.getStringExtra("output");
        et_output.setText(output);
        progressBar = findViewById(R.id.progressBar);
        btnDownload = findViewById(R.id.btnPdf);
        linearLayout = findViewById(R.id.ll_output);
        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);
        btnDocs = findViewById(R.id.btnDocs);
        btnBack = findViewById(R.id.btnBack);
        //for testing

        btnDownload.setOnClickListener(v -> {
            if(!et_output.getText().toString().isEmpty()) {
                takeScreenshot();
            }else{
                Toast.makeText(this, "Something error", Toast.LENGTH_SHORT).show();
            }
        });


        ActivityCompat.requestPermissions(Output.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);


        if (ContextCompat.checkSelfPermission(Output.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Output.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CODE);
        }


        btnBack.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(),MainActivity.class)));

        findViewById(R.id.btnTxt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);

                try {
                    File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "HandwritingApp");
                    if (!fileDir.exists()) {
                        fileDir.mkdirs(); // Create the directory if it does not exist
                    }
                    String fileName = "txt" + String.valueOf(System.currentTimeMillis());

                    File file = new File(fileDir, fileName + ".txt");
                    FileWriter writer = new FileWriter(file);
                    writer.append(et_output.getText().toString());
                    writer.flush();
                    writer.close();
                    Toast.makeText(Output.this, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle the exception
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Output.this, "Something error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Output.this);
                dialog.setContentView(R.layout.dialog_box);

                // Find views by ID
                EditText inputs = dialog.findViewById(R.id.et_grammar);
                Button btnOkay = dialog.findViewById(R.id.btnOkay);
                Button btnDisregard = dialog.findViewById(R.id.btnDisregard);
                ProgressBar progressBar2 = dialog.findViewById(R.id.progressBar);

                //Test s
                btnOkay.setEnabled(false);
                inputs.setText("");
                progressBar2.setVisibility(View.VISIBLE);

                String apiKey = Api.API;

                new Thread(() -> {
                    try {
                        URL url = new URL("https://textgears-textgears-v1.p.rapidapi.com/correct");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("X-RapidAPI-Key", "8a76e1df99msh0aceef082c25cd2p17a7c8jsn14a4f62c2770");
                        conn.setRequestProperty("X-RapidAPI-Host", "textgears-textgears-v1.p.rapidapi.com");
                        conn.setDoOutput(true);

                        String textToCheck = et_output.getText().toString();
                        String encodedText = URLEncoder.encode(textToCheck, "UTF-8");
                        String body = "text=" + encodedText;

                        try (OutputStream os = conn.getOutputStream()) {
                            byte[] input = body.getBytes("UTF-8");
                            os.write(input, 0, input.length);
                        }

                        int responseCode = conn.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                                String jsonResponse = scanner.useDelimiter("\\A").next();
                                try {
                                    JSONObject responseObj = new JSONObject(jsonResponse);
                                    Log.d("RESULT", "RESULT" + responseObj);

                                    // First get the "response" JSONObject
                                    JSONObject responseObject = responseObj.getJSONObject("response");

                                    // Now extract the "corrected" string from the responseObject
                                    String correctedText = responseObject.getString("corrected");

                                    runOnUiThread(() -> {
                                        inputs.setText(correctedText); // Display the corrected text
                                        btnOkay.setEnabled(true);
                                        progressBar2.setVisibility(View.GONE);
                                        progressBar.setVisibility(View.GONE);

                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    runOnUiThread(() -> Toast.makeText(Output.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                    dialog.hide();
                                }
                            }
                        } else {
                            progressBar2.setVisibility(View.GONE);
                            runOnUiThread(() -> Toast.makeText(Output.this, "Failed with response code: " + responseCode, Toast.LENGTH_LONG).show());
                            dialog.hide();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressBar2.setVisibility(View.GONE);
                        runOnUiThread(() -> Toast.makeText(Output.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }).start();




                // Get the Window object of the dialog
                Window window = dialog.getWindow();
                if (window != null) {
                    // Set the layout parameters to match the window size
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    // Optional: Remove dialog background to make it full screen
                    window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                }

                // Set up button click listener
                btnOkay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle button click
                        et_output.setText(inputs.getText().toString());
                        dialog.dismiss();
                    }
                });

                btnDisregard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle button click
                        inputs.setText("");
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                dialog.show();
            }
        });


        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HandwritingApp" + System.currentTimeMillis() + ".docx");


        try {
            if(!filePath.exists()){
                filePath.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        btnDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);

                try {
                    XWPFDocument document = new XWPFDocument();
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();

                    run.setText(et_output.getText().toString());
                    run.setFontSize(24);

                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    document.write(fileOutputStream);

                    if(fileOutputStream != null){
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }

                    document.close();

                    Toast.makeText(Output.this, "SAVED AS DOCS", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Output.this, "Something error", Toast.LENGTH_SHORT).show();

                }
            }
        });




    }





    private void createPdf() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        receiptDisplay = windowManager.getDefaultDisplay();
//        if(Build.VERSION.SDK_INT >= 23){
//            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED){
//            }else {
//                requestPermissions(new String []{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.READ_EXTERNAL_STORAGE},READ_PHONE);
//            }
    }


    private void createWordDocument(String text, File file) {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);

        try (FileOutputStream out = new FileOutputStream(file)) {
            document.write(out);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }


    private void createPdfAndShare() {
        PdfDocument pdfDocument = new PdfDocument();

        // Create a page info
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Create a canvas
        Canvas canvas = page.getCanvas();

        // Define the width and height for the bitmap
        int width = linearLayout.getWidth();
        int height = linearLayout.getHeight();

        // Create a bitmap and draw the view's content onto it
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas viewCanvas = new Canvas(bitmap);
        linearLayout.draw(viewCanvas);

        // Draw the bitmap on the PDF canvas
        canvas.drawBitmap(bitmap, 0, 0, null);

        // Finish the page
        pdfDocument.finishPage(page);

        // Save the PDF to a file
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "output.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
            // Close the document
            pdfDocument.close();
        } catch (IOException e) {
            //Toast.makeText(this, "Error creating PDF", Toast.LENGTH_SHORT).show();
            Log.e("Create PDF", "Error creating PDF", e);
        }
    }

    private  void takeScreenshot(){
        progressBar.setVisibility(View.VISIBLE);
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/");
        if(!folder.exists()){
            boolean success = folder.mkdir();
        }

        path = folder.getAbsolutePath();
        path = path + "/" + "handwriting" + System.currentTimeMillis() + ".pdf";

        totalHeight = parentRelativeLayout.getChildAt(0).getHeight();
        totalWidth = parentRelativeLayout.getChildAt(0).getWidth();
        String extr = Environment.getExternalStorageDirectory() + "/receipt/";
        File file  = new File(extr);

        //    boolean mkdir = file.mkdir();
        if(!file.exists()){

        }
        file.mkdir();
        String fileName = file_name + ".pdf";
        myPath = new File(extr, fileName);
        imageUri = myPath.getPath();
        bitmap = getBitmapFromView(linearLayout, totalHeight, totalWidth);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.v("PDF", "Message1: " + e.getMessage());
            Log.v("PDF", "Track1: " + e.getStackTrace());

        }
        downloadPdf();

    }
    //6-22
    public Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {
        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        }else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }


    private void downloadPdf() {
        //6-22
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawPaint(paint);
        Bitmap bitmap = Bitmap.createScaledBitmap(this.bitmap,this.bitmap.getWidth(),this.bitmap.getHeight(),true);
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(bitmap,0,0,null);
        document.finishPage(page);
        File filePath = new File(path);
        try{
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Saved Pdf Successfully! ", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }catch (Exception e){
            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.v("PDF","Message: "+ e.getMessage());
            Log.v("PDF","Track: "+ e.getStackTrace());
            progressBar.setVisibility(View.GONE);
        }
        document.close();
        if (myPath.exists())
            myPath.delete();
//        openPdf(path);
    }


}
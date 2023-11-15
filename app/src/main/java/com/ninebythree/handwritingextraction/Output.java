package com.ninebythree.handwritingextraction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Output extends AppCompatActivity {
    File myPath;
    int totalHeight, totalWidth;

    private  EditText et_output;
    String path,imageUri,file_name = "Download";
    Bitmap bitmap;
    private MaterialButton btnDownload;
    private LinearLayout linearLayout;
    NestedScrollView parentRelativeLayout;
    Display receiptDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        et_output = findViewById(R.id.et_output);

        Intent intent = getIntent();
        String output = intent.getStringExtra("output");
        et_output.setText(output);

        btnDownload = findViewById(R.id.btnPdf);
        linearLayout = findViewById(R.id.ll_output);
        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);


        btnDownload.setOnClickListener(v -> {
            if(!et_output.getText().toString().isEmpty()) {
                takeScreenshot();
            }else{
                Toast.makeText(this, "Something error", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void createPdf(){
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
        }catch (Exception e){
            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.v("PDF","Message: "+ e.getMessage());
            Log.v("PDF","Track: "+ e.getStackTrace());

        }
        document.close();
        if (myPath.exists())
            myPath.delete();
//        openPdf(path);
    }

}
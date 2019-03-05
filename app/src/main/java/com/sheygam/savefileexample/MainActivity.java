package com.sheygam.savefileexample;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button saveBtn;
    private View root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_btn) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public Bitmap loadBitmapFromView(View view) {
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(bitmap);
//        view.layout(0, 0, view.getLayoutParams().width, view.getLayoutParams().height);
//        view.draw(canvas);
//        return bitmap;
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    new SaveTask("TestFolder",sdf.format(new Date())).execute();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    class SaveTask extends AsyncTask<Void, Void, Boolean> {
        AlertDialog alertDialog;
        String folderName;
        String fileName;
        public SaveTask(String folderName, String fileName) {
            this.folderName = folderName;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            ProgressBar progressBar = new ProgressBar(MainActivity.this);
            alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Saving...")
                    .setView(progressBar)
                    .setCancelable(false)
                    .create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean status = true;

            try {
                File file;
                if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                    file = new File(Environment.getExternalStorageDirectory(), folderName);
                    if (!file.exists()) {
                        if (!file.mkdirs()) {
                            return false;
                        }

                    }
                    file = new File(file.getAbsolutePath(),fileName + ".png");

                    Bitmap bitmap = loadBitmapFromView(root);
                    FileOutputStream ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);
                    ostream.close();

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, fileName);
                    values.put(MediaStore.Images.Media.DESCRIPTION, fileName + " DESCRIPTION HERE");
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
                    values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
                    values.put("_data", file.getAbsolutePath());

                    ContentResolver cr = getContentResolver();
                    cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                }


            } catch (Exception e) {
                e.printStackTrace();
                status = false;
            }

            return status;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            alertDialog.dismiss();
            if (status) {
                Toast.makeText(MainActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error save!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

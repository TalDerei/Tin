package edu.lehigh.cse216.teamtin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    Camera cm;
    CameraPreview preview;
    private Button takePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager pm = this.getPackageManager();
        if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Log.w("Camera", "This device does not have a camera.");
            Toast.makeText(getApplicationContext(), "This device does not have a camera", Toast.LENGTH_LONG);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        //Ensure setContentView is called before doing anything else with the View
        setContentView(R.layout.activity_camera);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FrameLayout fl = findViewById(R.id.camera_frame_layout);
        SurfaceView sv = (SurfaceView) fl.getChildAt(0);
        preview = new CameraPreview(getApplicationContext(), sv);
        preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fl.addView(preview);

        try {
            int id = Camera.CameraInfo.CAMERA_FACING_BACK;

            if (cm != null) {
                cm.release();
                cm = null;
            }

            cm = Camera.open(id);
            //Say the camera is open
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), e.getMessage(), e);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        preview.setCamera(cm);

        takePicture = findViewById(R.id.takePicture);
        takePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cm.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        Log.d("Camera", "shutterCallback");
                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Log.d("Camera", "RawPicture callback");
                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Log.d("Camera", "A picture would be saved here");
                        File mediaFileDir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), "TheBuzzPictures");
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        File pictureFile = new File(mediaFileDir.getPath() + File.separator +
                                "IMG_"+ timeStamp + ".jpg");
                        if (pictureFile == null){
                            Log.w("Picture", "Error creating media file, check storage permissions");
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.e("Picture", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.e("Picture", "Error accessing file: " + e.getMessage());
                        }
                        Log.d("Camera", "JPG Picture saved");
                    }
                });
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void releaseCameraAndPreview() {
        preview.setCamera(null);
        if (cm != null) {
            cm.release();
            cm = null;
        }
    }
    WindowManager getActivityWindowManager() {
        return this.getWindowManager();
    }
}

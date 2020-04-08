package edu.lehigh.cse216.teamtin;

import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CameraActivity extends AppCompatActivity {

    Camera cm;
    CameraPreview preview;
    private Button takePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            int id = Camera.CameraInfo.CAMERA_FACING_BACK;

            if (cm != null) {
                cm.release();
                cm = null;
            }
            cm = Camera.open(id);
            //Say the camera is open
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
        preview = new CameraPreview(getApplicationContext());
        preview.setCamera(cm);



        setContentView(R.layout.activity_camera);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                        camera.startPreview();
                        preview.setCamera(camera);
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

}

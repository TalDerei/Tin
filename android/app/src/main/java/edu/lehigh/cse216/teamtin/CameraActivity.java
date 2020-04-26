package edu.lehigh.cse216.teamtin;

import android.content.Intent;
import android.content.pm.PackageManager;;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
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
                cm.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Log.d("Camera", "A picture would be saved here");
                        FileOutputStream outStream = null;

                        try {
                            File picDir = Environment.getExternalStorageDirectory();
                            Log.d("File", picDir.getAbsolutePath());
                            File dir = new File (picDir.getAbsolutePath() + "/TheBuzz");
                            dir.mkdirs();

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String fileName = String.format("%s.jpg", "IMG_" + timeStamp);
                            File outFile = new File(dir, fileName);

                            outStream = new FileOutputStream(outFile);
                            outStream.write(data);
                            outStream.flush();
                            outStream.close();

                            Log.d("Camera", "Wrote Picture to " + outFile.getAbsolutePath());

                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(Uri.fromFile(outFile));
                            sendBroadcast(mediaScanIntent);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent i;
        switch (id) {
            case R.id.action_home:
                i = new Intent(getApplicationContext(), MainActivity.class);
                setResult(790, i);
                finish();
                return true;
            case R.id.action_gallery:
                //Look at the photos
                i = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCameraAndPreview();
    }
}

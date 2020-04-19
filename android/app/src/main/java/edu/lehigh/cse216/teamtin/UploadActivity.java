package edu.lehigh.cse216.teamtin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    ArrayList<String> files;
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(files == null) {
            String[] temp = getIntent().getStringArrayExtra("files");
            files = new ArrayList<>();
            if(temp != null) {
                for(int i = 0; i < temp.length; i++) {
                    files.add(temp[i]);
                }
            }

        } else {
            files.clear();
        }

        setContentView(R.layout.activity_upload);

        uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(files != null && !files.isEmpty()) {
                    // returns to main activity to upload file
                    TextView t = findViewById(R.id.file_view);
                    Intent i = new Intent();
                    i.putExtra("file", t.getText().toString());
                    setResult(Activity.RESULT_OK, i);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Click 'Message Board' to leave without uploading a file.", Toast.LENGTH_SHORT);
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
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
                setResult(Activity.RESULT_CANCELED, new Intent());
                finish();
                return true;
            case R.id.action_gallery:
                // Select a picture from the gallery
                i = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivityForResult(i, 791);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 791) {
                String path = data.getStringExtra("image");
                if(path != null) {
                    files.add(path);
                    Log.d("UploadActivity", "inserting file name into field");
                    TextView fileList = findViewById(R.id.file_view);
                    fileList.setText(path);
                }
            }
        }
    }
}

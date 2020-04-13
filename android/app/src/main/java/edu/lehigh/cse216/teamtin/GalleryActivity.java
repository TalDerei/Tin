package edu.lehigh.cse216.teamtin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {
    ArrayList<PictureData> data;
    Intent img;
    File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        data = new ArrayList<>();
        img = new Intent();
        dir = Environment.getExternalStorageDirectory();
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        populateFromGallery(dir);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_close_gallery:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populateFromGallery(File dir) {
        data.clear();
        Log.d("File", dir.getAbsolutePath());
        if(dir == null || !dir.isDirectory()) {
            Log.e("Gallery", "A proper directory was not passed in");
            return;
        }

        final ArrayList<File> directories = new ArrayList<>();
        directories.add(dir);
        for (File f : directories) {
            //Log.d("File search", f.getName());
            // add in sub directories for future iteration
            File[] temp = f.listFiles(pathname ->  {
                return pathname.isDirectory();
            });
            if(temp != null) {
                directories.addAll(Arrays.asList(temp));
            }
        }

        for(int i = 0; i < directories.size(); i++) {
            // add in pictures from directory
            File[] temp = directories.get(i).listFiles(pathname ->  {
                return pathname.getAbsolutePath().contains(".jpg");
            });
            Log.d("File", Integer.toString(temp.length));
            if(temp != null) {
                for(int j = 0 ; j < temp.length; j++) {
                    data.add(new PictureData(temp[j]));
                }
            }
        }

        RecyclerView rv = findViewById(R.id.picture_list_view);
        rv.setLayoutManager(new GridLayoutManager(this, 5));
        PictureListAdapter adapter = new PictureListAdapter(this, data);
        rv.setAdapter(adapter);

        adapter.setClickListener((PictureListAdapter.ClickListener) (d) -> {
            img.putExtra("image", d.mPath);
            Log.d("setImageClickListener", "Image Selected");
            setResult(791, img);
            finish();
        });

        adapter.setImageClickListener( (PictureListAdapter.ClickListener) (d) -> {
            img.putExtra("image", d.mPath);
            Log.d("setImageClickListener", "Image Selected");
            setResult(791, img);
        });
        adapter.notifyDataSetChanged();
    }
}

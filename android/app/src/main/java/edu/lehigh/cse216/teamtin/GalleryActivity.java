package edu.lehigh.cse216.teamtin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    ArrayList<File> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new ArrayList<>();
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        Intent i;
        switch (id) {
            case R.id.action_home:
                i = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populateFromGallery(File dir) {
        data.clear();

        if(dir == null || !dir.isDirectory()) {
            Log.e("Gallery", "A proper directory was not passed in");
            return;
        }

        final LinkedList<File> directories = new LinkedList<File>();
        directories.add(dir);
        for (File f : directories) {
            // add in sub directories for future iteration
            directories.addAll(Arrays.asList(dir.listFiles(pathname ->  {
                return pathname.isDirectory();
            })));

            // add in pictures from directory
            data.addAll(Arrays.asList(dir.listFiles(pathname ->  {
                return pathname.getAbsolutePath().contains(".jpg");
            })));
        }

        RecyclerView rv = findViewById(R.id.picture_list_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        PictureListAdapter adapter = new PictureListAdapter(this, data);
        rv.setAdapter(adapter);

        adapter.setClickListener((PictureListAdapter.ClickListener) (d) -> {
            //this intent will bring us to a page where you can up and down vote
             Intent i = new Intent(getApplicationContext(), voteActivity.class);
            startActivityForResult(i, 791); // 791 is the number that will come back to us
        });

        adapter.setImageClickListener( (PictureListAdapter.ClickListener) (d) -> {
            Intent i = new Intent();
            i.putExtra("image", d.getAbsolutePath());
            Log.d("setImageClickListener", "Image Selected");
        });
        adapter.notifyDataSetChanged();
    }
}

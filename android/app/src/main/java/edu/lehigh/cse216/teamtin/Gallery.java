package edu.lehigh.cse216.teamtin;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Gallery extends AppCompatActivity {
    Map<String, String> map = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populateFromGallery(File dir) {

        RecyclerView rv = findViewById(R.id.picture_list_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        //placeholder
        ArrayList<Datum> mData = new ArrayList<>();
        PictureListAdapter adapter = new PictureListAdapter(this, mData);
        rv.setAdapter(adapter);

        adapter.setClickListener((PictureListAdapter.ClickListener) (d) -> {
            //this intent will bring us to a page where you can up and down vote
            Intent i = new Intent(getApplicationContext(), voteActivity.class);
            startActivityForResult(i, 791); // 791 is the number that will come back to us
        });

        /*
         click event on profile image
         */
        adapter.setImageClickListener( (PictureListAdapter.ClickListener) (d) -> {
            String messages = (String) map.get(d.mSubject);
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            Log.d("setImageClickListener", "message: " + messages);
            startActivityForResult(i, 791); // 791 is the number that will come back to us
        });
    }
}

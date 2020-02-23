package edu.lehigh.cse216.teamtin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class voteActivity extends AppCompatActivity {

    ImageButton LikeButton;
    ImageButton DislikeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        addListenerOnButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SecondActivity.class);
            i.putExtra("label_contents", "Type a message to post:");
            startActivityForResult(i, 789); // 789 is the number that will come back to us
            return true;
        } else if (id == R.id.action_home) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("label_contents", "HHHHHHHHHEYEYEYYE");
            startActivityForResult(i, 789); // 789 is the number that will come back to us
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void addListenerOnButton() {
        LikeButton = findViewById(R.id.buttonLike);
        LikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikeButton.setSelected(!LikeButton.isSelected());
                if (LikeButton.isSelected()) {
                    // Handle selected state change
                    Toast.makeText(voteActivity.this, "Liked!", Toast.LENGTH_LONG).show();
                } else {
                    // Handle de-select state change
                }
            }
        });
        DislikeButton = findViewById(R.id.buttonDislike);
        DislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DislikeButton.setSelected(!DislikeButton.isSelected());
                if (DislikeButton.isSelected()) {
                    // Handle selected state change
                    Toast.makeText(voteActivity.this, "Disliked!", Toast.LENGTH_LONG).show();
                } else {
                    // Handle de-select state change
                }
            }
        });
    }
}


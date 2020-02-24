package edu.lehigh.cse216.teamtin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /**
     * mData holds the data we get from Volley
     */
    ArrayList<Datum> mData = new ArrayList<>();
    String url = "https://limitless-ocean-62391.herokuapp.com/messages";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Creates our main app layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        populateListFromVolley(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("vld222", "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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

        if (id == R.id.action_post) {
            Intent i = new Intent(getApplicationContext(), PostActivity.class);
            i.putExtra("label_contents", "Type a message to post:");
            startActivityForResult(i, 789); // 789 is the number that will come back to us
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateListFromVolley(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray json = jsonObject.getJSONArray("mData");
            for (int i = 0; i < json.length(); ++i) {
                int num = json.getJSONObject(i).getInt("mId");
                String str = json.getJSONObject(i).getString("mSubject");
                mData.add(new Datum(num, str));
            }
        } catch (final JSONException e) {
            Log.d("vld222", "Error parsing JSON file: " + e.getMessage());
            return;
        }
        Log.d("vld222", "Successfully parsed JSON file.");
        RecyclerView rv = findViewById(R.id.datum_list_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ItemListAdapter adapter = new ItemListAdapter(this, mData);
        rv.setAdapter(adapter);

        adapter.setClickListener(new ItemListAdapter.ClickListener() {
            @Override
            public void onClick(Datum d) {
                //this intent will bring us to a page where you can up and down vote
                Intent i = new Intent(getApplicationContext(), voteActivity.class);
                i.putExtra("messageText", d.mText);
                i.putExtra("messageUser", Integer.toString(d.mIndex));
                startActivityForResult(i, 789); // 789 is the number that will come back to us


                /*
                setContentView(R.layout.activity_vote);
                TextView head = findViewById(R.id.messageHeading);
                head.setText("hey");
                TextView body = findViewById(R.id.fullMessage);
                body.setText("asdf");
                //Toast.makeText(MainActivity.this, d.mIndex + " --> " + d.mText,
                  //      Toast.LENGTH_LONG).show();*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 789) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the "extra" string of data
                //Intent i = new Intent(getApplicationContext(), MainActivity.class);
                //i.putExtra("label_contents", "Type a message to post:");
                //startActivityForResult(i, 789); // 789 is the number that will come back to us
                // Request a string response from the provided URL.


                Toast.makeText(MainActivity.this, "Message Posted!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Message Cancelled.", Toast.LENGTH_LONG).show();
            }
        }
    }
}

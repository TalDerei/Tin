package edu.lehigh.cse216.teamtin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.DefaultRetryPolicy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     * mData holds the data we get from Volley
     */
    ArrayList<Datum> mData = new ArrayList<>();
    String url = "https://limitless-ocean-62391.herokuapp.com/messages";

    private String profileName;
    private String profileEmail;

    /**
     *  map holds messages for given username (mTitle)
     */
    Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Creates our main app layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        profileName = intent.getStringExtra("givenName") + " " + intent.getStringExtra("familyName");
        profileEmail = intent.getStringExtra("email");
        getRequestBackend();
    }


    /**
     * GET method to see messages
     */
    public void getRequestBackend() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("GET", response);
                        populateListFromVolley(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("vld222", "That didn't work!");
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    /**
     *  deprecated function
     */
    public void postRequestBackend() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("mTitle", "Android Test");
                params.put("mMessage", "some message");

                return params;
            }
        };
        queue.add(postRequest);
    }

    /**
     *  Post a message with JSONObject
     */
    public void postJsonRequestBackend(String result) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject object = new JSONObject();
        Log.d("who", profileName);
        try {
            //input your API parameters
            object.put("mTitle", profileName);
            object.put("mMessage",result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Enter the correct url for your api service site
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("onResponse", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", error.toString());
            }

        });
        queue.add(jsonObjectRequest);
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
        mData.clear();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray json = jsonObject.getJSONArray("mData");
            for (int i = 0; i < json.length(); ++i) {
                String sub = json.getJSONObject(i).getString("mSubject");
                String str = json.getJSONObject(i).getString("mMessage");
                mData.add(new Datum(sub, str));
                if (!map.containsKey(sub))
                    map.put(sub, new ArrayList<String>());
                map.get(sub).add(str);
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

        /*
         click event on message
         */
        adapter.setClickListener(new ItemListAdapter.ClickListener() {
            @Override
            public void onClick(Datum d) {
                //this intent will bring us to a page where you can up and down vote
                Intent i = new Intent(getApplicationContext(), voteActivity.class);
                i.putExtra("messageText", d.mText);
                i.putExtra("messageUser", d.mSubject);
                startActivityForResult(i, 789); // 789 is the number that will come back to us
            }
        });

        /*
         click event on profile image
         */
        adapter.setImageClickListener(new ItemListAdapter.ClickListener() {
            @Override
            public void onClick(Datum d) {
                ArrayList<String> messages = (ArrayList<String>) map.get(d.mSubject);
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                i.putExtra("messageUser", d.mSubject);
                i.putStringArrayListExtra("messages", messages);
                Log.d("setImageClickListener", "size: " + messages.size());
                startActivityForResult(i, 789); // 789 is the number that will come back to us
            }
        });
        adapter.notifyDataSetChanged();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 789) {
                String result = data.getStringExtra("result");
                Log.d("onActivityResult", result);
                postJsonRequestBackend(result);
                SystemClock.sleep(1000);
                Toast.makeText(MainActivity.this, "Message Posted!", Toast.LENGTH_LONG).show();
                getRequestBackend();
            }
        }
    }


}

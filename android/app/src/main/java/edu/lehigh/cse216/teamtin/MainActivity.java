package edu.lehigh.cse216.teamtin;

import android.app.Activity;
import android.content.Intent;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.DefaultRetryPolicy;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CacheResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    /**
     * mData holds the data we get from Volley
     */
    ArrayList<Datum> mData = new ArrayList<>();

    Map<String, File> mFilesId = new HashMap<String, File>();
    String driveUrl = "https://drive.google.com/open?id=";//append fileId at the end
    String messagesUrl = "https://limitless-ocean-62391.herokuapp.com/messages";
    String pictureUrl = "https://limitless-ocean-62391.herokuapp.com/upload";
    String fileDownloadUrl = "https://limitless-ocean-62391.herokuapp.com/file";
    String allFileDownloadUrl = "https://limitless-ocean-62391.herokuapp.com/files";

    private String profileName;
    private String profileEmail;

    private String uid;
    private String jwt;

    private long lastUpdateTime;
    private long expires;

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

        enableHttpResponseCache();
        lastUpdateTime = 0;
        try {
            File httpCacheDir = new File(getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i("info:", "HTTP response cache installation failed:" + e);
        }

        Intent intent = getIntent();
        profileName = intent.getStringExtra("givenName") + " " + intent.getStringExtra("familyName");
        profileEmail = intent.getStringExtra("email");
        if(intent.getBooleanExtra("login", false) && uid == null && jwt == null){
            uid = intent.getStringExtra("user_id");
            jwt = intent.getStringExtra("jwt");
        }
        getRequestBackend();
        getFileIdsFromBackend();
    }

    @Override
    protected void onStop() {
        super.onStop();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    /**
     * GET method to see messages
     */
    public void getRequestBackend() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
        messagesUrl + "?user_id=" + uid + "&jwt=" + jwt,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("GET", "response received");
                        populateListFromVolley(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("hag322", "That didn't work!");
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getFileIdsFromBackend(){
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                allFileDownloadUrl + "?user_id=" + uid + "&jwt=" + jwt,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("GET", "response received");
                        downloadFiles(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("hag322", "That didn't work!");
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void downloadFiles(String response) {
        mFilesId.clear();

        try {
            JSONObject res = new JSONObject(response);
            JSONArray elements = res.getJSONArray("mData");
            for(int i = 0; i < elements.length(); i++) {
                JSONObject jo = elements.getJSONObject(i);
                URL u = new URL(jo.getString("url"));
                mFilesId.put(jo.getString("fileid"), new File(u.toURI()));
            }
        } catch (JSONException | MalformedURLException | URISyntaxException e) {
            Log.e("DownloadFiles", "something went wrong", e);
            e.printStackTrace();
        }
    }

    public void postJsonFileRequestBackend(String file) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject pic = new JSONObject();

        StringRequest stringRequest = null;

        if (file != null){
            try {
                File f = new File(file);
                byte[] content = FileUtils.readFileToByteArray(f);
                String encodedString = Base64.encodeToString(content, Base64.DEFAULT);
                // Only works for one file right now
                stringRequest = new StringRequest(Request.Method.POST,
                        pictureUrl + "?user_id=" + uid + "&jwt=" + jwt,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jo = new JSONObject(response);
                                    String fid = jo.getString("mData");
                                    mFilesId.put(fid, new File(f.getAbsolutePath()));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("onResponse", response.toString());
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("onErrorResponse", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "multipart/form-data";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return encodedString == null ? null : encodedString.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", encodedString, "utf-8");
                            return null;
                        }
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(stringRequest != null) {
            queue.add(stringRequest);
        }
    }

    /**
     *  Post a message with JSONObject
     */
    public void postJsonRequestBackend(String result, String[] files) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject object = new JSONObject();

        StringRequest stringRequest = null;

        Log.d("who", profileName);
        try {
            if (files != null && files.length > 0){
                File f = new File(files[0]);
                byte[] content = FileUtils.readFileToByteArray(f);
                final String encodedString = Base64.encodeToString(content, Base64.DEFAULT);

                // Only works for one file right now
                stringRequest = new StringRequest(Request.Method.POST,
                        pictureUrl + "?user_id=" + uid + "&jwt=" + jwt,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jo = new JSONObject(response);
                                    String fid = jo.getString("mData");
                                    mFilesId.put(fid, new File(files[0]));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("onResponse", response.toString());
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("onErrorResponse", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "multipart/form-data";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return encodedString == null ? null : encodedString.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", encodedString, "utf-8");
                            return null;
                        }
                    }
                };
            }
            //input your API parameters
            object.put("mTitle", profileName);
            object.put("mMessage", result);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        // Enter the correct url for your api service site
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, messagesUrl + "?user_id=" + uid + "&jwt=" + jwt, object,
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
        if(stringRequest != null) {
            queue.add(stringRequest);
        }
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

        Intent i;
        switch (id) {
            case R.id.action_post:
                i = new Intent(getApplicationContext(), PostActivity.class);
                i.putExtra("label_contents", "Type a message to post:");
                startActivityForResult(i, 789); // 789 is the number that will come back to us
                return true;
            case R.id.action_camera:
                i = new Intent(getApplicationContext(), CameraActivity.class);
                startActivityForResult(i, 790);
                return true;
            case R.id.action_gallery:
                i = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivityForResult(i, 791);
                return true;
            case R.id.action_test_cache:
                Log.d("Cache Test", getFileForMessage("file").getName());
                return true;
            case R.id.action_upload:
                i = new Intent(getApplicationContext(), UploadActivity.class);
                startActivityForResult(i, 792);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            Log.d("hag322", "Error parsing JSON file: " + e.getMessage());
            return;
        }
        Log.d("hag322", "Successfully parsed JSON file.");
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
                String[] files = data.getStringArrayExtra("files");
                Log.d("onActivityResult", result);
                postJsonRequestBackend(result, files);
                SystemClock.sleep(1000);
                Toast.makeText(MainActivity.this, "Message Posted!", Toast.LENGTH_LONG).show();
                getRequestBackend();
            } else if (requestCode == 792) {
                String file = data.getStringExtra("file");
                postJsonFileRequestBackend(file);
            }
        }
    }

    //enables the HttpResponseCache for supported devices
    private void enableHttpResponseCache() {
        try {
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            File httpCacheDir = new File(getCacheDir(), "http");
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
            Log.d("Error", "HTTP response cache is unavailable.");
        }
    }

    private void updateCache(URL url) {
        // url represents the website containing the content to place into the cache.
        try {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            long currentTime = System.currentTimeMillis();
            expires = conn.getHeaderFieldDate("Expires", currentTime);
            long lastModified = conn.getHeaderFieldDate("Last-Modified", currentTime);

            // lastUpdateTime represents when the cache was last updated.
            if (lastModified < lastUpdateTime) {
                // skip update
            } else {
                lastUpdateTime = lastModified;
            }
        } catch (IOException e) {
            Log.e("error", Log.getStackTraceString(e));
        }
    }

    private File getFileForMessage(String placeholder) {
        try {
            HttpResponseCache responseCache = HttpResponseCache.getInstalled();
            HttpsURLConnection huc = (HttpsURLConnection) new URL("https://limitless-ocean-62391.herokuapp.com/").openConnection();
            if(responseCache != null){
                URI uri = new URI("https://limitless-ocean-62391.herokuapp.com/messages");
                CacheResponse cacheResponse = responseCache.get(uri, "GET", huc.getRequestProperties());
                Scanner scanner = new Scanner(cacheResponse.getBody(), "UTF-8");
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()){
                    sb.append(scanner.nextLine());
                }
                Log.d("HTTP Cache Test", sb.toString());

                return null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
        // Search for file in cache
        // search for file in the device
        // pull files from the web
    }

    private void getFileFromId(String fileId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                fileDownloadUrl + "/" + fileId + "?user_id=" + uid + "&jwt=" + jwt,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("GET", "response received");
                        generateFile(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("hag322", "That didn't work!");
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void generateFile(String encodedFile) {
        String outputFileName = "/storage/emulated/0/TheBuzz/" + 1000 * Math.random();
        byte[] decodedBytes = Base64.decode(encodedFile, Base64.DEFAULT);
        try {
            FileUtils.writeByteArrayToFile(new File(outputFileName), decodedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package edu.lehigh.cse216.teamtin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    String url = "https://limitless-ocean-62391.herokuapp.com";
    final String apiKey = "AIzaSyBv4PsExxtQj4BTGJGpsiMdJVSFTPKxagQ";
    final String endPointURL = "https://www.googleapis.com/tasks/v1/users/@me/lists";
    private GoogleSignInClient googleSignInClient;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
    private Button signOut;
    private String email;
    private String familyName;
    private String givenName;
    private String idToken;
    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d("sap716", "onCreate!");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        clientId = "98587864938-h28665jsmboh5bb04qi153d2n2n3nd28.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestServerAuthCode(getString(R.string.server_client_id), false)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
                Log.e("sap716", "SignIn!");
            }
        });

        signOut = findViewById(R.id.button_sign_out);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
            }
        });
    }

    private void onLoggedIn(GoogleSignInAccount googleSignInAccount) {
        Intent intent = new Intent(this, MainActivity.class);
        Log.d("email", email);
        Log.d("familyName", familyName);
        Log.d("givenName", givenName);
        intent.putExtra("email", email);
        intent.putExtra("familyName", familyName);
        intent.putExtra("givenName", givenName);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("hag322", "entered onActivityResult with requestCode:" + requestCode);

        if (requestCode == 101) {
            try {
                Log.d("onActivityResult", "before getSignedInAccountFromIntent");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                Log.d("onActivityResult", "after getSignedInAccountFromIntent");
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("onActivityResult", "after get GoogleSignInAccount");

                email = account.getEmail();
                familyName = account.getFamilyName();
                givenName = account.getGivenName();
                idToken = account.getIdToken();
                String code = account.getServerAuthCode();
                Log.d("onActivityResult", code);
                Log.d("onActivityResult", "email is " + email);
                Log.d("onActivityResult", "ID Token is " + idToken);

                //TODO: send an ID token to backend server
                RequestQueue queue = Volley.newRequestQueue(this);
                String targetUrl = url + "/users/login?idToken=" + idToken + "&code=" + code;
                Log.d("POST URL", targetUrl);

                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        targetUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("onResponse", "JWT is " + response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String r = "";
                        for(Header h : error.networkResponse.allHeaders) {
                            r += h.getName() + " " + h.getValue() + "\n";
                        }
                        Log.d("Error", "Error in response!\n" + r);
                    }
                });
                // Add the request to the RequestQueue.
                queue.add(stringRequest);

                onLoggedIn(account);

            } catch (ApiException e) {
                Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


}

package edu.lehigh.cse216.teamtin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    public static final String GOOGLE_ACCOUNT = "google_account";
    private TextView profileName, profileEmail;
    private ImageView profileImage;
    private Button signOut;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        String messageUser;
        messageUser = intent.getStringExtra("messageUser");
        ArrayList<String> messages = intent.getStringArrayListExtra("messages");

        Log.d("messageUser", messageUser);
        Log.d("#messages", "size: " + messages.size());

        RecyclerView rv = findViewById(R.id.datum_list_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ItemListInProfileAdapter adapter = new ItemListInProfileAdapter(this, messages);
        rv.setAdapter(adapter);
    }
}

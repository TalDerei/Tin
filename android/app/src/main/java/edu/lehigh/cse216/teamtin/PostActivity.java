package edu.lehigh.cse216.teamtin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

/** This activity is where the user will add a message to post.
 * They can post a message or cancel and go back to the list of all the messages.
 */

public class PostActivity extends AppCompatActivity {

    ArrayList<String> files;

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
        setContentView(R.layout.activity_second);

        // Get the parameter from the calling activity, and put it in the TextView
        Intent input = getIntent();
        String label_contents = input.getStringExtra("label_contents");
        TextView tv = findViewById(R.id.specialMessage);
        tv.setText(label_contents);

        // The OK button gets the text from the input box and returns it to the calling activity
        final EditText et = findViewById(R.id.editText);
        Button bOk = findViewById(R.id.buttonOk);
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et.getText().toString().equals("")) {
                    String result = et.getText().toString();
                    Log.d("PostActivity", result);
                    Intent i = new Intent();
                    i.putExtra("result", result);
                    i.putStringArrayListExtra("files", files);
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
            }
        });

        // The Cancel button returns to the caller without sending any data
        Button bCancel = findViewById(R.id.buttonCancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        Button bAddFile = findViewById(R.id.fetchFile);
        bAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), GalleryActivity.class),791);
            }
        });
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
                    Log.d("PostActivity", "inserting file name into field");
                    EditText fileList = findViewById(R.id.fileList2);
                    fileList.append(path);
                }
            }
        }
    }
}

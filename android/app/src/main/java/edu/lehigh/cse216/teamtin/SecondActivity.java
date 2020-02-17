package edu.lehigh.cse216.teamtin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Get the parameter from the calling activity, and put it in the TextView
        Intent input = getIntent();
        String label_contents = input.getStringExtra("label_contents");
        TextView tv = (TextView) findViewById(R.id.specialMessage);
        tv.setText(label_contents);

        // The OK button gets the text from the input box and returns it to the calling activity
        final EditText et = (EditText) findViewById(R.id.editText);
        Button bOk = (Button) findViewById(R.id.buttonOk);
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et.getText().toString().equals("")) {
                    Intent i = new Intent();
                    i.putExtra("result", et.getText().toString());
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
            }
        });

        // The Cancel button returns to the caller without sending any data
        Button bCancel = (Button) findViewById(R.id.buttonCancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }
}

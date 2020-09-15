package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        int millisForTimer=intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 30000);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);
        final TextView timeRemaining = (TextView) findViewById(R.id.textView5);
        CountDownTimer timer = new CountDownTimer(millisForTimer, 1000) {

            public void onTick(long millisUntilFinished) {
                Date date = new Date((long) (millisUntilFinished));
                String formattedDate = new SimpleDateFormat("HH/mm/ss.SSS", Locale.GERMAN).format(date);
                timeRemaining.setText(formattedDate);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                timeRemaining.setText(getResources().getString(R.string.done));
            }

        }.start();
    }


}
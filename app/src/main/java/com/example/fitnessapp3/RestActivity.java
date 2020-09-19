package com.example.fitnessapp3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RestActivity extends AppCompatActivity {
    CountDownTimer timer;
    Intent nextIntent;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);


        Intent intent = getIntent();
        if ("WorkoutActivity".equals(Objects.requireNonNull(intent.getStringExtra(MainActivity.EXTRA_RETURN_DEST)))) {
            nextIntent = new Intent(this, WorkoutActivity.class);
        } else {
            nextIntent = new Intent(this, MainActivity.class);
        }

        int millisForTimer = intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 30000);
        CurrentWorkout.currentWorkout += millisForTimer + ";";
        final TextView timeRemaining = findViewById(R.id.textView5);
        timer = new CountDownTimer(millisForTimer, 1000) {

            public void onTick(long millisUntilFinished) {
                Date date = new Date(millisUntilFinished);
                String formattedDate = new SimpleDateFormat("mm:ss", Locale.GERMAN).format(date);
                timeRemaining.setText(formattedDate);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                if ("WorkoutActivity".equals(Objects.requireNonNull(getIntent().getStringExtra(MainActivity.EXTRA_RETURN_DEST)))) {
                    CurrentWorkout.position += 1;
                }
                timeRemaining.setText(getResources().getString(R.string.done));
                startActivity(nextIntent);
            }

        }.start();
    }

    public void skipTimer(View view) {
        timer.cancel();
        timer.onFinish();
    }


}
package com.example.fitnessapp3;

import androidx.annotation.NonNull;
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
    int millisForTimer;
    int timeElapsed = 0;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);
        if(savedInstanceState!=null){
            timeElapsed = savedInstanceState.getInt("timeElapsed");
        }

        Intent intent = getIntent();
        if ("WorkoutActivity".equals(Objects.requireNonNull(intent.getStringExtra(MainActivity.EXTRA_RETURN_DEST)))) {
            nextIntent = new Intent(this, WorkoutActivity.class);
        } else {
            nextIntent = new Intent(this, MainActivity.class);
        }

        millisForTimer = intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 30000) - timeElapsed;
        final TextView timeRemaining = findViewById(R.id.textView5);
        timer = new CountDownTimer(millisForTimer, 1000) {

            public void onTick(long millisUntilFinished) {
                Date date = new Date(millisUntilFinished);
                timeElapsed += 1000;
                String formattedDate = new SimpleDateFormat("mm:ss", Locale.GERMAN).format(date);
                timeRemaining.setText(formattedDate);
            }

            public void onFinish() {

                if ("WorkoutActivity".equals(Objects.requireNonNull(getIntent().getStringExtra(MainActivity.EXTRA_RETURN_DEST)))) {
                    CurrentWorkout.currentWorkout[CurrentWorkout.position] = millisForTimer + ";";
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

    public void onBackPressed() {
        CurrentWorkout.goBack();
        super.onBackPressed();
    }

    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt("timeElapsed", timeElapsed);

    }


}
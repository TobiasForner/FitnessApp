package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.fitnessapp3.MESSAGE";
    public static final String EXTRA_RETURN_DEST = "com.example.fitnessapp3.RETURN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startWorkout(View view){
        CurrentWorkout.initExercises();
        Intent intent = new Intent(this, WorkoutActivity.class);
        startActivity(intent);
    }

    public void startTimer(View view) {
        Intent intent = new Intent(this, RestActivity.class);
        int time = 180000;
        intent.putExtra(EXTRA_MESSAGE, time);
        intent.putExtra(EXTRA_RETURN_DEST, "MainActivity");
        startActivity(intent);
    }
}
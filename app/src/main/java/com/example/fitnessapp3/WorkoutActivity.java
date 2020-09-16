package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
    }

    public void logExercise(View view) {
        //TODO debug why second call bugs out
        CurrentWorkout.position += 1;
        if (CurrentWorkout.position < CurrentWorkout.exercises.length) {
            Intent intent = new Intent(this, RestActivity.class);
            int time = 180000;
            intent.putExtra(MainActivity.EXTRA_MESSAGE, time);
            intent.putExtra(MainActivity.EXTRA_RETURN_DEST, "WorkoutActivity");
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
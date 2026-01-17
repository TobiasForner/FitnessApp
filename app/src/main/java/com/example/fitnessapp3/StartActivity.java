package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (CurrentWorkout.workoutIsInProgress(this)) {
            Intent intent = new Intent(this, ResumeWorkoutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, MainActivity2.class);
            startActivity(intent);
            finish();
        }
    }
}
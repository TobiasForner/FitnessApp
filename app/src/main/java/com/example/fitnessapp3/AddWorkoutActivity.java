package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AddWorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);
    }

    public void addWorkout(View view) {
        TextView workoutName = findViewById(R.id.text_workout_name);
        if (workoutName.getText().equals("")) {
            return;
        }
        TextView workoutText = findViewById(R.id.text_workout_body);
        if (workoutText.getText().equals("")) {
            return;
        }
        if (WorkoutManager.checkWorkoutString(workoutText.getText().toString())) {
            //TODO check whether workout name already exists
            WorkoutManager.addWorkout(workoutName.getText().toString(), workoutText.getText().toString(), this);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }


}
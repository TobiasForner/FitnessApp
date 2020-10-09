package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
        if (parseWorkout(workoutText.getText().toString())) {
            //TODO check whether workout name already exists
            addWorkout(workoutName.getText().toString(), workoutText.getText().toString());

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }

    private boolean parseWorkout(String text) {
        String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
        for (String line : lines) {
            if (!parseLine(line)) {
                return false;
            }
        }
        return true;
    }

    private boolean parseLine(String line) {
        if (line.equals("")) {
            return false;
        }
        String[] parts = line.split("\\[");
        if (parts.length != 1) {
            return false;
        }
        String[] bodyAndTimes = parts[0].split("]");
        if (bodyAndTimes.length == 0) {
            return false;
        }
        String[] exerciseNames = bodyAndTimes[0].split(",");
        for (String exName : exerciseNames) {
            //TODO check whether exercise exists
        }
        if (bodyAndTimes.length == 2 && bodyAndTimes[1].length() >= 2) {
            if (bodyAndTimes[1].charAt(0) == 'x' | bodyAndTimes[1].charAt(0) == 'X') {
                String timesString = bodyAndTimes[1].substring(1);
                try {
                    int times = Integer.parseInt(timesString);
                } catch (NumberFormatException nfe) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addWorkout(String name, String workoutBody) {
        //TODO implement
    }
}
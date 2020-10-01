package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.fitnessapp3.MESSAGE";
    public static final String EXTRA_RETURN_DEST = "com.example.fitnessapp3.RETURN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.go_to_add_exercise_button);
        LinearLayout linear = findViewById(R.id.workout_linear_layout);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        List<String> workoutList = new ArrayList<>(Objects.requireNonNull(sharedPreferences.getStringSet("Workouts", new HashSet<String>())));
        linear.removeAllViews();
        for (String s : workoutList) {
            Button b = new Button(this);
            b.setText(s);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startWorkout(v);
                }
            });
            linear.addView(b);

        }
    }

    public void startWorkout(View view) {
        CurrentWorkout.init((String) ((Button) view).getText(), this);
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

    public void goToAddExercise(View view) {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        startActivity(intent);
    }
}
package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.fitnessapp3.MESSAGE";
    public static final String EXTRA_RETURN_DEST = "com.example.fitnessapp3.RETURN";
    private String[] workoutNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        int buttonWidth = width / 3 - 20;

        LinearLayout linear = findViewById(R.id.workout_linear_layout);
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        readWorkoutNames();
        //List<String> workoutList = new ArrayList<>(Objects.requireNonNull(sharedPreferences.getStringSet("Workouts", new HashSet<String>())));
        //linear.removeAllViews();
        for (String s : workoutNames) {
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

        Button addExercise = findViewById(R.id.go_to_add_exercise_button);
        Button timerButton = findViewById(R.id.timerButton);
        Button initWorkoutsButton = findViewById(R.id.initWorkoutsButton);
        addExercise.setWidth(buttonWidth);
        timerButton.setWidth(buttonWidth);
        initWorkoutsButton.setWidth(buttonWidth);
        /*
        int buttonHeight = Collections.max(Arrays.asList(addExercise.getHeight(), timerButton.getHeight(), initWorkoutsButton.getHeight()));
        addExercise.setHeight(buttonHeight);
        timerButton.setHeight(buttonHeight);
        initWorkoutsButton.setHeight(buttonHeight);
         */
    }

    private void readWorkoutNames() {
        try {
            FileInputStream fis = this.openFileInput("workout_names.txt");
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append(Objects.requireNonNull(System.getProperty("line.separator")));
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } finally {
                String contents = stringBuilder.toString();
                workoutNames = contents.split(Objects.requireNonNull(System.getProperty("line.separator")));
            }
        } catch (FileNotFoundException e) {
            Log.e("MainActivity", "Workout Names file workout_names not found.");
            workoutNames = new String[0];
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

    public void initWorkoutNamesFile(View view) {
        Startup.initWorkoutNamesFile(this);
    }
}
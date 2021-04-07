package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.fitnessapp3.MESSAGE";
    public static final String EXTRA_RETURN_DEST = "com.example.fitnessapp3.RETURN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout linear = findViewById(R.id.workout_linear_layout);
        for (String s : WorkoutManager.getWorkoutNames()) {
            Button b = new Button(this);
            b.setText(s);
            b.setOnClickListener(this::startWorkout);
            linear.addView(b);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int buttonWidth = width / 3 - 20;

        Button addExercise = findViewById(R.id.go_to_add_exercise_button);
        Button timerButton = findViewById(R.id.timerButton);
        Button initWorkoutsButton = findViewById(R.id.initWorkoutsButton);
        addExercise.setWidth(buttonWidth);
        timerButton.setWidth(buttonWidth);
        initWorkoutsButton.setWidth(buttonWidth);
    }

    public void startWorkout(View view) {
        CurrentWorkout.init(((Button) view).getText().toString(), this);
        startWorkout();
    }

    public void startTimer(View view) {
        Intent intent = new Intent(this, TimerActivity.class);
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

    private void startWorkout() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("workout_is_in_progress", true);
        editor.apply();
        startActivity(ActivityTransition.goToNextActivityInWorkout(this));
    }

    public void openEditWorkouts(View view) {
        Intent intent = new Intent(this, WorkoutEditActivity.class);
        startActivity(intent);
    }

    public void goToManageExercises(View v) {
        Intent intent = new Intent(this, ManageExercisesActivity.class);
        startActivity(intent);
    }
}
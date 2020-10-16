package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        workoutNames = WorkoutManager.getWorkoutNames();
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

    public void startWorkout(View view) {
        CurrentWorkout.init((String) ((Button) view).getText(), this);
        startWorkout();
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

    private void startWorkout() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("workout_is_in_progress", true);
        editor.apply();
        if (CurrentWorkout.getNextWorkoutComponent().isExercise()) {
            Intent intent = new Intent(this, WorkoutActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, RestActivity.class);
            intent.putExtra(MainActivity.EXTRA_RETURN_DEST, "WorkoutActivity");
            startActivity(intent);
        }
    }

    public void resumeLastWorkout() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("workout_is_in_progress", false)) {
            startWorkout();
        }
    }

    public void showPopupWindowClick(String text) {

        // inflate the popup_continue_previous_workout.xml of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup parent = findViewById(android.R.id.content);
        View view = findViewById(R.id.main_constraint_layout);
        View popupView = inflater.inflate(R.layout.popup_continue_previous_workout, (ViewGroup) view, false);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        // show the popup window

        // which view you pass in doesn't matter, it is only used for the window token

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        TextView popUpText = popupView.findViewById(R.id.text_workout_in_progress_exists);
        popUpText.setText(getString(R.string.workout_in_progress_exists, text));

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                popupWindow.dismiss();
                return true;
            }
        });

        Button continue_button = popupView.findViewById(R.id.button_continue);
        Button cancel_button = popupView.findViewById(R.id.button_overwrite);
        continue_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performClick();
                popupWindow.dismiss();
                resumeLastWorkout();
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performClick();
                popupWindow.dismiss();
            }
        });
    }
}
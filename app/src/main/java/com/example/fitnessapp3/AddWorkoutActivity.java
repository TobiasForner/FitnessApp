package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Objects;

public class AddWorkoutActivity extends AppCompatActivity {
    public static final String EXNAME = "com.example.fitnessapp3.EXNAME";
    public static final String EDIT = "com.example.fitnessapp3.EDIT";
    public static final String WORKOUT_NAME = "com.example.fitnessapp3.WORKOUT_NAME";
    private boolean edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);
        Intent intent = getIntent();
        edit = intent.getBooleanExtra(EDIT, false);
        if (edit) {
            switchToEdit(intent.getStringExtra(WORKOUT_NAME));
        }
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
        if (!edit && WorkoutManager.workoutExists(workoutName.getText().toString())) {
            openPopUpWorkoutNameExists(workoutName.getText().toString(), this.findViewById(android.R.id.content));
            return;
        }
        if (checkWorkoutString(workoutText.getText().toString())) {
            WorkoutManager.addWorkout(workoutName.getText().toString(), workoutText.getText().toString(), this);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }

    public boolean checkWorkoutString(String text) {
        String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
        for (String line : lines) {
            if (!checkWorkoutLine(line)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkWorkoutLine(String line) {
        if (line.equals("")) {
            return false;
        }
        String[] parts = line.split("\\[");
        if (parts.length != 1 && parts.length != 2) {
            return false;
        }
        if (parts.length == 2 && !parts[0].equals("")) {
            return false;
        }
        String[] bodyAndTimes;
        if (parts.length == 2) {
            bodyAndTimes = parts[1].split("]");
        } else {
            bodyAndTimes = parts[0].split("]");
        }
        if (bodyAndTimes.length == 0) {
            return false;
        }
        String[] exerciseNames = bodyAndTimes[0].split(",");
        for (String exName : exerciseNames) {

            if (!WorkoutManager.exerciseExists(exName)) {
                openPopUpAddExercise(exName, this.findViewById(android.R.id.content));
            }
        }
        if (bodyAndTimes.length == 2 && bodyAndTimes[1].length() >= 2) {
            if (bodyAndTimes[1].charAt(0) == 'x' | bodyAndTimes[1].charAt(0) == 'X') {
                String timesString = bodyAndTimes[1].substring(1);
                try {
                    Integer.parseInt(timesString);
                } catch (NumberFormatException nfe) {
                    return false;
                }
            }
        }
        return true;
    }

    private void openPopUpAddExercise(final String exName, View view) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_ask_to_create_exercise, (ViewGroup) findViewById(android.R.id.content));

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        TextView popUpText = popupView.findViewById(R.id.text_exercise_does_not_exist);
        popUpText.setText(getString(R.string.exercise_does_not_exists, exName));

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                popupWindow.dismiss();
                return true;
            }
        });

        Button add_button = popupView.findViewById(R.id.button_add);
        Button cancel_button = popupView.findViewById(R.id.button_overwrite);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performClick();
                popupWindow.dismiss();
                goToAddExercise(exName);
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

    public void goToAddExercise(String exName) {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        intent.putExtra(EXNAME, exName);
        startActivity(intent);
    }

    private void openPopUpWorkoutNameExists(final String workoutName, View view) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_ask_to_edit_existing_workout, (ViewGroup) findViewById(android.R.id.content));

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        TextView popUpText = popupView.findViewById(R.id.text_workout_exists);
        popUpText.setText(getString(R.string.workout_name_already_exists, workoutName));

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                popupWindow.dismiss();
                return true;
            }
        });

        Button yes_button = popupView.findViewById(R.id.button_add);
        Button overwrite_button = popupView.findViewById(R.id.button_overwrite);
        yes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performClick();
                popupWindow.dismiss();
                switchToEdit(workoutName);
            }
        });

        overwrite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performClick();
                popupWindow.dismiss();
            }
        });
    }

    private void switchToEdit(String workoutName){
        edit = true;
        TextView title = findViewById(R.id.text_add_title);
        title.setText(R.string.edit_workout);
        TextView workoutNameText = findViewById(R.id.text_workout_name);
        workoutNameText.setText(workoutName);
        TextView workoutText = findViewById(R.id.text_workout_body);
        String workoutBody = WorkoutManager.getWorkoutTextFromFile(workoutName, this);
        workoutText.setText(workoutBody);
    }


}
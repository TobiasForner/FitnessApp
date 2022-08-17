package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

public class AddWorkoutActivity extends AppCompatActivity implements PositiveNegativeDialogFragment.NoticeDialogListener {
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

        //hide keyboard if edit text not focussed
        EditText editText = findViewById(R.id.editText_workout_body);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        if (edit) {
            switchToEdit(intent.getStringExtra(WORKOUT_NAME));
        }
    }

    public void addWorkout(View view) {
        TextView workoutName = findViewById(R.id.text_workout_name);
        if (workoutName.getText().equals("")) {
            return;
        }
        TextView workoutText = findViewById(R.id.editText_workout_body);
        if (workoutText.getText().equals("")) {
            return;
        }
        if (!edit && WorkoutManager.workoutExists(workoutName.getText().toString())) {
            openDialog(workoutName.getText().toString(), 0);
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
        // TODO use regex
        line = Util.strip(line);
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
            String strippedName = Util.strip(exName);

            if (!WorkoutManager.exerciseExists(strippedName)) {
                openDialog(strippedName, 1);
                return false;
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


    public void goToAddExercise(String exName) {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        intent.putExtra(EXNAME, exName);
        startActivity(intent);
    }

    private void switchToEdit(String workoutName) {
        edit = true;
        TextView title = findViewById(R.id.text_add_title);
        title.setText(R.string.edit_workout);
        TextView workoutNameText = findViewById(R.id.text_workout_name);
        workoutNameText.setText(workoutName);
        TextView workoutText = findViewById(R.id.editText_workout_body);
        String workoutBody = WorkoutManager.getWorkoutTextFromFile(workoutName, this);
        workoutText.setText(workoutBody);

        Button add_edit = findViewById(R.id.add_edit_workout_button);
        add_edit.setText(R.string.edit);
    }

    public void onDialogPositiveClick(DialogFragment dialog) {
        PositiveNegativeDialogFragment fragment = (PositiveNegativeDialogFragment) dialog;
        if (fragment.getVersion() == 0) {
            switchToEdit(fragment.getMessageExtra());
        } else {
            goToAddExercise(fragment.getMessageExtra());
        }
    }

    public void onDialogNegativeClick(DialogFragment dialog) {
        PositiveNegativeDialogFragment fragment = (PositiveNegativeDialogFragment) dialog;
        if (fragment.getVersion() == 0) {
            TextView workoutName = findViewById(R.id.text_workout_name);
            TextView workoutText = findViewById(R.id.editText_workout_body);
            if (checkWorkoutString(workoutText.getText().toString())) {
                WorkoutManager.addWorkout(workoutName.getText().toString(), workoutText.getText().toString(), this);

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
        //do nothing if version is 1 or something else
    }

    private void openDialog(String messageExtra, int dialogVersion) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog;
        if (dialogVersion == 0) {
            dialog = new PositiveNegativeDialogFragment(R.string.workout_name_already_exists, R.string.yes, R.string.no_edit, messageExtra, dialogVersion);
        } else {
            dialog = new PositiveNegativeDialogFragment(R.string.exercise_does_not_exists, R.string.yes, R.string.cancel, messageExtra, dialogVersion);
        }
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
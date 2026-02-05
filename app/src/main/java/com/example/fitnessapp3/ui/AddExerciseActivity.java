package com.example.fitnessapp3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.example.fitnessapp3.data.Exercise;
import com.example.fitnessapp3.data.ExerciseManager;
import com.example.fitnessapp3.R;
import com.example.fitnessapp3.data.WorkoutComponent;


public class AddExerciseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, PositiveNegativeDialogFragment.NoticeDialogListener, CompoundButton.OnCheckedChangeListener {

    public static final String EDIT = "com.example.fitnessapp3.EDIT";
    private String exType;
    private boolean weighted;

    private boolean edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExerciseManager exerciseManager = new ExerciseManager(this);
        setContentView(R.layout.activity_add_exercise);
        Intent intent = getIntent();
        String exName = intent.getStringExtra(AddWorkoutActivity.EXNAME);
        TextView exerciseName = findViewById(R.id.editTextExName);
        exerciseName.setText(exName);
        edit = intent.getBooleanExtra(EDIT, false);
        if (edit) {
            TextView header = findViewById(R.id.textViewAddExerciseHeader);
            header.setText(R.string.edit_exercise);

            Button overwriteButton = findViewById(R.id.button_add_exercise);
            overwriteButton.setText(R.string.overwrite);
        }

        if (exerciseManager.exerciseExists(exName)) {
            WorkoutComponent exComp = exerciseManager.getWorkoutComponent(exName);
            Exercise exercise = (Exercise) exComp;
            weighted = exercise.isWeighted();
            exType = exercise.getType().toString();
        } else {
            //TODO maybe not even needed
            weighted = intent.getBooleanExtra(ManageExercisesActivity.WEIGHTED, true);
            exType = intent.getStringExtra(ManageExercisesActivity.TYPE);
            if (exType == null) {
                exType = "";
            }
        }


        Spinner exerciseTypeSpinner = findViewById(R.id.spinner_exType);
        exerciseTypeSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exercise_types_array, android.R.layout.simple_spinner_item);
        // Specify the popup_continue_previous_workout.xml to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        exerciseTypeSpinner.setAdapter(adapter);
        if (exType == null || exType.equals("Reps")) {
            exerciseTypeSpinner.setSelection(0);
        } else {
            exerciseTypeSpinner.setSelection(1);
        }

        SwitchCompat weightedSwitch = findViewById(R.id.switch1);
        weightedSwitch.setOnCheckedChangeListener(this);
        weightedSwitch.setChecked(weighted);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (pos == 0) {
            exType = "Reps";
        } else {
            exType = "Duration";
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        weighted = isChecked;
    }

    public void addExercise(View view) {
        assert view.getId() == R.id.button_add_exercise;
        TextView exName = findViewById(R.id.editTextExName);
        String exerciseName = exName.getText().toString();
        ExerciseManager exerciseManager = new ExerciseManager(this);
        if (exerciseManager.exerciseExists(exerciseName)) {
            openDialog(exerciseName);
        } else {
            finishAndAdd();
        }
    }

    private void openDialog(String messageExtra) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog;
        if (edit) {
            dialog = new PositiveNegativeDialogFragment(R.string.exercise_overwrite, R.string.overwrite, R.string.cancel, messageExtra, 0);
        } else {
            dialog = new PositiveNegativeDialogFragment(R.string.exercise_exists, R.string.overwrite, R.string.cancel, messageExtra, 0);
        }

        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    private void finishAndAdd() {
        TextView exName = findViewById(R.id.editTextExName);
        String exerciseName = exName.getText().toString();
        Exercise.ExType type = Exercise.ExType.fromString(exType);
        ExerciseManager exerciseManager = new ExerciseManager(this);
        exerciseManager.addExercise(exerciseName, type, weighted, this);
        finish();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        finishAndAdd();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}
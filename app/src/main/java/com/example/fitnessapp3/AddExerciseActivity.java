package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

public class AddExerciseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
    private String exType;
    private boolean weighted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);

        weighted = false;
        exType = "";

        Spinner exerciseTypeSpinner = findViewById(R.id.spinner_extype);
        exerciseTypeSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exercise_types_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        exerciseTypeSpinner.setAdapter(adapter);

        SwitchCompat weightedSwitch = findViewById(R.id.switch1);
        weightedSwitch.setOnCheckedChangeListener(this);
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
        TextView exName = findViewById(R.id.exerciseName);
        String exDetails = "ExerciseType=" + exType + ";" + "Weighted=" + weighted + ";" + "Abbreviation=" + exName;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(exName.getText().toString(), exDetails);
        editor.apply();
    }
}
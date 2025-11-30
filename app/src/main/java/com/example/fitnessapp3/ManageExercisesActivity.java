package com.example.fitnessapp3;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

public class ManageExercisesActivity extends AppCompatActivity {
    public static final String WEIGHTED = "com.example.fitnessapp3.WEIGHTED";
    public static final String TYPE = "com.example.fitnessapp3.TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_exercises);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout linear = findViewById(R.id.linear_layout_exercise_buttons);

        WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();

        int width = windowMetrics.getBounds().width();
        int buttonWidth = width  - 200;

        linear.removeAllViews();

        ExerciseManager exerciseManager = new ExerciseManager(this);
        List<String> exerciseNames = exerciseManager.getExerciseNames();
        Collections.sort(exerciseNames);
        for (String s : exerciseNames) {
            if (s.equals("Rest")) {
                continue;
            }
            Log.d("ManageExercisesActivity", "onResume: adding line for exercise "+s);
            LinearLayout horizontal = new LinearLayout(this);
            Button b = new Button(this);
            b.setText(s);
            b.setOnClickListener(this::editExercise);
            b.setWidth(buttonWidth);
            horizontal.addView(b);

            Button delButton = new Button(this);
            delButton.setText(R.string.delete);
            delButton.setOnClickListener((View v)->deleteExercise(s));
            horizontal.addView(delButton);
            linear.addView(horizontal);
        }
    }

    private void deleteExercise(String exerciseName){
        ExerciseManager exerciseManager = new ExerciseManager(this);
        exerciseManager.deleteExercise(exerciseName, this);
        this.onResume();
    }

    public void goToAddExercise(View v) {
        assert v.getId() == R.id.manage_exercises_act_go_to_add;
        Intent intent = new Intent(this, AddExerciseActivity.class);
        intent.putExtra(AddWorkoutActivity.EXNAME, "");
        startActivity(intent);
    }

    public void editExercise(View v) {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        String exName = ((Button) v).getText().toString();
        intent.putExtra(AddWorkoutActivity.EXNAME, exName);
        intent.putExtra(AddExerciseActivity.EDIT, true);
        startActivity(intent);
    }
}
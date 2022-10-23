package com.example.fitnessapp3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

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
        linear.removeAllViews();

        for (String s : WorkoutManager.getExerciseNames()) {
            if (s.equals("Rest")) {
                continue;
            }
            Button b = new Button(this);
            b.setText(s);
            b.setOnClickListener(this::editExercise);
            linear.addView(b);
        }
    }

    public void goToAddExercise(View v) {
        assert v.getId() == R.id.manage_exercises_act_go_to_add;
        Intent intent = new Intent(this, AddExerciseActivity.class);
        startActivity(intent);
    }

    public void editExercise(View v) {
        WorkoutComponent workoutComponent = WorkoutManager.getWorkoutComponentFromName(((Button) v).getText().toString());
        if (workoutComponent.isExercise()) {
            Intent intent = new Intent(this, AddExerciseActivity.class);
            intent.putExtra(AddWorkoutActivity.EXNAME, workoutComponent.getName());
            Exercise exercise = (Exercise) workoutComponent;
            intent.putExtra(WEIGHTED, exercise.isWeighted());
            Exercise.ExType type = exercise.getType();
            String typeString = type.toString();
            intent.putExtra(TYPE, typeString);
            startActivity(intent);
        }
    }
}
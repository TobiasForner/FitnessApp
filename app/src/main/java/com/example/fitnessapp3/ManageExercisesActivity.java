package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ManageExercisesActivity extends AppCompatActivity {
    public static final String WEIGHTED = "com.example.fitnessapp3.WEIGHTED";
    public static final String TYPE = "com.example.fitnessapp3.TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_exercises);

        LinearLayout linear = findViewById(R.id.linear_layout_exercise_buttons);

        for (String s : WorkoutManager.getExerciseNames()) {
            if (s.equals("Rest")) {
                continue;
            }
            Button b = new Button(this);
            b.setText(s);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editExercise(v);
                }
            });
            linear.addView(b);
        }
    }

    public void goToAddExercise(View v) {
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
            Exercise.EXTYPE type = exercise.getType();
            String typeString;
            if (type == Exercise.EXTYPE.DURATION) {
                typeString = "Duration";
            } else {
                typeString = "Reps";
            }
            intent.putExtra(TYPE, typeString);
            startActivity(intent);
        }
    }
}
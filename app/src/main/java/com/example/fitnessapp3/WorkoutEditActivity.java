package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

public class WorkoutEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_edit);

        LinearLayout linear = findViewById(R.id.linear_layout_workout_edit);
        List<String> workoutNames = WorkoutManager.getWorkoutNamesList();
        for (String s : workoutNames) {
            Button b = new Button(this);
            b.setText(s);
            b.setOnClickListener(this::editWorkout);
            linear.addView(b);
        }
    }

    private void editWorkout(View v) {
        Button button = (Button) v;
        Intent intent = new Intent(this, AddWorkoutActivity.class);
        intent.putExtra(AddWorkoutActivity.EDIT, true);
        intent.putExtra(AddWorkoutActivity.WORKOUT_NAME, button.getText());
        startActivity(intent);
    }

    public void goBack(View v) {
        finish();
    }

    public void goToAddWorkout(View v) {
        Intent intent = new Intent(this, AddWorkoutActivity.class);
        startActivity(intent);
    }
}
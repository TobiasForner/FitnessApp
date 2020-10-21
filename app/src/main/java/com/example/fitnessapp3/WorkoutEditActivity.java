package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class WorkoutEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_edit);

        LinearLayout linear = findViewById(R.id.linear_layout_workout_edit);
        String[] workoutNames = WorkoutManager.getWorkoutNames();
        for (String s : workoutNames) {
            Button b = new Button(this);
            b.setText(s);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editWorkout(v);
                }
            });
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
}
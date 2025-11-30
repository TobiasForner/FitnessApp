package com.example.fitnessapp3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class WorkoutEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_edit);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout linear = findViewById(R.id.linear_layout_workout_edit);
        List<String> workoutNames = WorkoutManager.getWorkoutNamesList();
        WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();

        int width = windowMetrics.getBounds().width();
        int buttonWidth = width  - 200;
        linear.removeAllViews();
        for (String s : workoutNames) {
            LinearLayout horizontal = new LinearLayout(this);

            Button b = new Button(this);
            b.setText(s);
            b.setOnClickListener(this::editWorkout);
            b.setWidth(buttonWidth);
            horizontal.addView(b);

            Button delButton = new Button(this);
            delButton.setText(R.string.delete);
            delButton.setOnClickListener((View v)->deleteWorkout(s));
            horizontal.addView(delButton);
            linear.addView(horizontal);
        }
    }

    private void deleteWorkout(String workoutName){
        WorkoutManager.deleteWorkout(workoutName, this);
        this.onResume();
    }

    private void editWorkout(View v) {
        Button button = (Button) v;
        Intent intent = new Intent(this, AddWorkoutActivity.class);
        intent.putExtra(AddWorkoutActivity.EDIT, true);
        intent.putExtra(AddWorkoutActivity.WORKOUT_NAME, button.getText());
        startActivity(intent);
    }

    public void goBack(View v) {
        assert v.getId() == R.id.imageButton_back;
        finish();
    }

    public void goToAddWorkout(View v) {
        assert v.getId() == R.id.workout_edit_add_workout_button;
        Intent intent = new Intent(this, AddWorkoutActivity.class);
        startActivity(intent);
    }
}
package com.example.fitnessapp3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp3.data.CurrentWorkout;
import com.example.fitnessapp3.com.example.fitnessapp3.MainActivity2;
import com.example.fitnessapp3.R;

public class ResumeWorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_workout);
        TextView text = findViewById(R.id.text_workout_in_progress_exists);
        text.setText(getString(R.string.workout_in_progress_exists, CurrentWorkout.getWorkoutNameInProgress(this)));
    }

    public void goToMainActivity(View view) {
        assert view.getId() == R.id.button_cancel;
        CurrentWorkout.setInProgress(false, this);
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

    public void resumeWorkout(View view) {
        assert view.getId() == R.id.resume_workout_act_continue_button;
        if (CurrentWorkout.workoutIsInProgress(this)) {
            CurrentWorkout.restoreWorkoutInProgress(this);

            Intent nextIntent = ActivityTransition.goToNextActivityInWorkout(this);
            nextIntent.setFlags(nextIntent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(nextIntent);
        }
    }
}
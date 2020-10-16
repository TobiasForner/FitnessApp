package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

public class ResumeWorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_workout);
        TextView text=findViewById(R.id.text_workout_in_progress_exists);
        text.setText(getString(R.string.workout_in_progress_exists, WorkoutManager.getWorkoutNameInProgress(this)));
    }

    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void resumeWorkout(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("workout_is_in_progress", false)) {
            CurrentWorkout.restoreWorkoutInProgress(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("workout_is_in_progress", true);
            editor.apply();
            if (CurrentWorkout.getNextWorkoutComponent().isExercise()) {
                Intent intent = new Intent(this, WorkoutActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, RestActivity.class);
                intent.putExtra(MainActivity.EXTRA_RETURN_DEST, "WorkoutActivity");
                startActivity(intent);
            }
        }
    }
}
package com.example.fitnessapp3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ActivityTransition {
    public static Intent goToNextActivityInWorkout(Activity origin) {
        Intent nextIntent;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(origin);
        if (!sharedPreferences.getBoolean("workout_is_in_progress", false)) {
            nextIntent = new Intent(origin, MainActivity.class);
        } else if (CurrentWorkout.hasNextExercise()) {
            if (CurrentWorkout.getNextWorkoutComponent().isRest()) {
                nextIntent = new Intent(origin, RestActivity.class);
                int time = ((Rest) CurrentWorkout.getNextWorkoutComponent()).getRestTime();
                nextIntent.putExtra(MainActivity.EXTRA_MESSAGE, time);
                nextIntent.putExtra(MainActivity.EXTRA_RETURN_DEST, "WorkoutActivity");
            } else if (((Exercise) CurrentWorkout.getNextWorkoutComponent()).getType() == Exercise.EXTYPE.DURATION) {
                nextIntent = new Intent(origin, DurationExerciseActivity.class);
            } else {
                nextIntent = new Intent(origin, WorkoutActivity.class);
            }
        } else {
            nextIntent = new Intent(origin, MainActivity.class);
        }
        return nextIntent;
    }
}

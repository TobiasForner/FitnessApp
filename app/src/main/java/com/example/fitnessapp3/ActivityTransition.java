package com.example.fitnessapp3;

import android.app.Activity;
import android.content.Intent;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ActivityTransition {
    public static Intent goToNextActivityInWorkout(Activity origin) {
        Intent nextIntent;
        try {
            JSONObject appStatus = new JSONObject(Objects.requireNonNull(Util.readFromInternal("app_status.json", origin)));
            if(appStatus.getBoolean("workout_is_in_progress") && CurrentWorkout.hasCurrentExercise()){
                if (CurrentWorkout.getCurrentWorkoutComponent().isRest()) {
                    nextIntent= restIntent(origin);
                } else if (((Exercise) CurrentWorkout.getCurrentWorkoutComponent()).getType() == Exercise.ExType.DURATION) {
                    nextIntent = new Intent(origin, DurationExerciseActivity.class);
                } else {
                    nextIntent = new Intent(origin, WorkoutActivity.class);
                }
            }
            else{
                nextIntent = new Intent(origin, MainActivity.class);
            }
            return nextIntent;
        }catch(JSONException e){
            e.printStackTrace();
            nextIntent = new Intent(origin, MainActivity.class);
        }
        return nextIntent;
    }

    private static Intent restIntent(Activity origin){
        Intent nextIntent = new Intent(origin, RestActivity.class);
        int time = ((Rest) CurrentWorkout.getCurrentWorkoutComponent()).getRestTime();
        nextIntent.putExtra(MainActivity.EXTRA_MESSAGE, time);
        nextIntent.putExtra(MainActivity.EXTRA_RETURN_DEST, "WorkoutActivity");
        return nextIntent;
    }
}

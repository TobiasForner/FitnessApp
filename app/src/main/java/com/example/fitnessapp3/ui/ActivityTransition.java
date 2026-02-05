package com.example.fitnessapp3.ui;

import android.app.Activity;
import android.content.Intent;


import com.example.fitnessapp3.data.CurrentWorkout;
import com.example.fitnessapp3.com.example.fitnessapp3.MainActivity2;
import com.example.fitnessapp3.data.Exercise;
import com.example.fitnessapp3.data.Rest;
import com.example.fitnessapp3.util.Util;

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
                    nextIntent = new Intent(origin, DurationExerciseActivity2.class);
                } else {
                    nextIntent = new Intent(origin, RepExerciseActivity.class);
                }
            }
            else{
                nextIntent = new Intent(origin, MainActivity2.class);
            }
            return nextIntent;
        }catch(JSONException e){
            e.printStackTrace();
            nextIntent = new Intent(origin, MainActivity2.class);
        }
        return nextIntent;
    }

    private static Intent restIntent(Activity origin){
        Intent nextIntent = new Intent(origin, RestActivity2.class);
        int time = ((Rest) CurrentWorkout.getCurrentWorkoutComponent()).getRestTime();
        nextIntent.putExtra(MainActivity.EXTRA_MESSAGE, time);
        nextIntent.putExtra(MainActivity.EXTRA_RETURN_DEST, "WorkoutActivity");
        return nextIntent;
    }
}

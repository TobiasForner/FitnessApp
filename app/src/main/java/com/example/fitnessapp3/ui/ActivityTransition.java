package com.example.fitnessapp3.ui;

import android.app.Activity;
import android.content.Intent;


import com.example.fitnessapp3.MainActivity2;
import com.example.fitnessapp3.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ActivityTransition {
    public static Intent goToNextActivityInWorkout(Activity origin) {
        Intent nextIntent;
        try {
            JSONObject appStatus = new JSONObject(Objects.requireNonNull(Util.readFromInternal("app_status.json", origin)));
            if(appStatus.getBoolean("workout_is_in_progress")){
                nextIntent = new Intent(origin, WorkoutActivityMain.class);
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
}

package com.example.fitnessapp3;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class Startup extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Test");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("FIRSTRUN", true)) {
            // Code to run once
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("FIRSTRUN", false);
            Set<String> workoutNames = new HashSet<String>();
            workoutNames.add("RR");
            editor.putStringSet("Workouts", workoutNames);
            editor.apply();
        }

    }
}

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("FIRSTRUN", true)) {
            // Code to run once
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("FIRSTRUN", false);
            Set<String> workoutNames = new HashSet<>();
            workoutNames.add("RR");
            editor.putStringSet("Workouts", workoutNames);
            StringBuilder rrString = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                rrString.append("Pullup,WEIGHT;");
                rrString.append("Rest,REST,180000;");
            }
            for (int i = 0; i < 3; i++) {
                rrString.append("Ring Dips,WEIGHT;");
                rrString.append("Rest,REST,180000;");
            }
            for (int i = 0; i < 3; i++) {
                rrString.append("Rows,WEIGHT;");
                rrString.append("Rest,REST,180000;");
                rrString.append("Push Ups,WEIGHT;");
                if (i < 2) {
                    rrString.append("Rest,REST,180000;");
                }
            }
            editor.putString("RR", rrString.toString());
            editor.apply();
        }

    }
}

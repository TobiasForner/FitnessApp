package com.example.fitnessapp3;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class Startup extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("FIRSTRUN", true);
        editor.apply();
        if (sharedPreferences.getBoolean("FIRSTRUN", true)) {
            // Code to run once
            initWorkoutNamesFile(this);

            initExercises(editor);
            editor.putBoolean("FIRSTRUN", false);
            Set<String> workoutNames = new HashSet<>();
            workoutNames.add("RR");
            editor.putStringSet("Workouts", workoutNames);
            StringBuilder rrString = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                rrString.append("Pull Up,WEIGHT;");
                rrString.append("Rest,REST,180000;");
            }
            for (int i = 0; i < 3; i++) {
                rrString.append("Ring Dip,WEIGHT;");
                rrString.append("Rest,REST,180000;");
            }
            for (int i = 0; i < 3; i++) {
                rrString.append("Row,WEIGHT;");
                rrString.append("Rest,REST,180000;");
                rrString.append("Push Up,WEIGHT;");
                if (i < 2) {
                    rrString.append("Rest,REST,180000;");
                }
            }
            editor.putString("RR", rrString.toString());
            editor.apply();
            Context context = getApplicationContext();
            initWorkoutNamesFile(context);
            ExerciseManager exerciseManager = new ExerciseManager(context);
            exerciseManager.initExerciseDetails(context);
        }
    }

    public static void initExercises(SharedPreferences.Editor editor) {
        editor.putString("Pull Up", "ExerciseType=Reps;Weighted=true;Abbreviation=Pull Up");
        editor.putString("Ring Dip", "ExerciseType=Reps;Weighted=true;Abbreviation=RD");
        editor.putString("Push Up", "ExerciseType=Reps;Weighted=true;Abbreviation=PshU");
        editor.putString("Row", "ExerciseType=Reps;Weighted=true;Abbreviation=Row");
        editor.apply();
    }

    public static void initWorkoutNamesFile(Context context) {
        String filename = "workout_names.txt";
        String fileContents = "RR";
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

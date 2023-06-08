package com.example.fitnessapp3;

import android.app.Application;
import android.content.Context;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class Startup extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JSONObject appStatus;
        if(Util.contextHasFile(this, "app_status.json")){
            try {
                appStatus = new JSONObject(Objects.requireNonNull(Util.readFromInternal("app_status.json", this)));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }else{
            appStatus = new JSONObject();
            try {
                appStatus.put("first_run", true);
                appStatus.put("workout_is_in_progress", false);
                Util.writeFileOnInternalStorage(this,"app_status.json",appStatus.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            if (appStatus.getBoolean("first_run")) {
                // Code to run once
                initWorkoutNamesFile(this);
                appStatus.put("first_run", false);
                Util.writeFileOnInternalStorage(this,"app_status.json",appStatus.toString());

                Context context = getApplicationContext();
                initWorkoutNamesFile(context);
                ExerciseManager exerciseManager = new ExerciseManager(context);
                exerciseManager.initExerciseDetails(context);
                WorkoutManager.init(this);
                String sep = System.getProperty("line.separator");
                String rrForFileString = "[Pull Up,Rest]x5" + sep + "[Ring Dip,Rest]x3" + sep +
                        "[Row,Rest,Push Up,Rest]x2" + sep + "Row,Rest,Push Up" + sep;
                WorkoutManager.addWorkout("RR", rrForFileString, this);
            } else {
                WorkoutManager.init(this);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initWorkoutNamesFile(Context context) {
        File file = new File(context.getFilesDir(), "workout_names.txt");
        String fileContents = "RR" + System.getProperty("line.separator");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileContents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

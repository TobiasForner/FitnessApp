package com.example.fitnessapp3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import java.util.Objects;

public class CurrentWorkout {
    public static Exercise[] exercises;
    public static int position = 0;
    public static String[] lastWorkout;
    public static String workoutName;
    public static boolean useLastWorkout;
    public static String currentWorkout = "";

    private static Exercise processExercise(String exString) {
        String[] info = exString.split(",");
        if (info.length < 2) {
            Log.e("CurrentWorkout", "empty exercise");
        }
        Exercise.EXTYPE type;
        switch (info[1]) {
            case "REST":
                type = Exercise.EXTYPE.REST;
                break;
            case "DUR":
                type = Exercise.EXTYPE.DURATION;
                break;
            default:
                type = Exercise.EXTYPE.WEIGHT;
        }
        Exercise ex = new Exercise(info[0], type);
        if (info[1].equals("REST")) {
            ex.setParameter(Integer.parseInt(info[2]));
        }
        return ex;

    }

    public static void finishWorkout(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(workoutName + "last_result", currentWorkout);
        editor.apply();
    }

    public static void init(String workoutName, Activity activity) {
        CurrentWorkout.workoutName = workoutName;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String[] exStrings = Objects.requireNonNull(sharedPreferences.getString(workoutName, "")).split(";");
        position = 0;
        /*
        Set<String> lastWorkoutDef = new HashSet<>();
        List<String> workoutResults = new ArrayList<>(Objects.requireNonNull(sharedPreferences.getStringSet(workoutName + "_results", lastWorkoutDef)));
        java.util.Collections.sort(workoutResults);*/
        exercises = new Exercise[exStrings.length];
        for (int i = 0; i < exStrings.length; i++) {
            exercises[i] = processExercise(exStrings[i]);
        }
        String lastWorkoutString = sharedPreferences.getString(workoutName + "last_result", "");
        assert lastWorkoutString != null;
        if (lastWorkoutString.length() > 0) {
            lastWorkout = lastWorkoutString.split(";");
            useLastWorkout = lastWorkout.length == exStrings.length;
        }
        /*
        String lastWorkoutResults = workoutResults.get(workoutResults.size() - 1);
        String[] lastWorkoutResultsSplit = lastWorkoutResults.split(";");
        if (lastWorkoutResultsSplit.length == exStrings.length) {
            lastWorkout = new String[lastWorkoutResults.length()];
            System.arraycopy(lastWorkoutResultsSplit, 0, lastWorkout, 0, exStrings.length);
        }*/
    }

    public static boolean hasNextExercise() {
        return position < exercises.length;
    }

    public static Exercise getNextExercise() throws IllegalArgumentException {
        if (!hasNextExercise()) {
            throw new IllegalArgumentException("No next exercise!");
        }
        return exercises[position];
    }
}

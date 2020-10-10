package com.example.fitnessapp3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CurrentWorkout {
    public static String[] lastWorkout;
    public static String workoutName;
    public static boolean useLastWorkout;
    public static String[] currentWorkout;
    public static String[] setStrings;
    protected static int[] numberOfExercise;
    protected static Map<String, String[]> exToResults;
    //protected static ExerciseManager exerciseManager;
    private static Workout workout;

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
        Exercise ex = new Exercise(info[0], type, true);
        if (info[1].equals("REST")) {
            ex.setParameter(Integer.parseInt(info[2]));
        }
        return ex;

    }

    public static void finishWorkout(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> lastWorkoutDef = new HashSet<>();
        Set<String> workoutResults = Objects.requireNonNull(sharedPreferences.getStringSet(workoutName + "_results", lastWorkoutDef));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd.hh-mm:", Locale.getDefault());
        Date today = Calendar.getInstance().getTime();
        String date = dateFormat.format(today);
        workoutResults.add(date + String.join(";", currentWorkout));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(workoutName + "last_result", String.join(";", currentWorkout));
        editor.putStringSet(workoutName + "_results", workoutResults);
        editor.apply();
    }

    public static void init(String workoutName, Activity activity) {
        //exerciseManager.initExerciseDetails(activity);
        useLastWorkout = false;
        CurrentWorkout.workoutName = workoutName;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String[] exStrings = Objects.requireNonNull(sharedPreferences.getString(workoutName, "")).split(";");
        //position = 0;
        workout = WorkoutManager.getWorkoutFromFile(workoutName, activity);
        int workoutLength = exStrings.length;

        currentWorkout = new String[workoutLength];
        //workoutComponents = new WorkoutComponent[workoutLength];
        setStrings = new String[workoutLength];
        numberOfExercise = new int[workoutLength];
        Map<String, Integer> exCounts = new HashMap<>();
        exToResults = new HashMap<>();
        for (int i = 0; i < workoutLength; i++) {
            //workoutComponents[i] = processExercise(exStrings[i]);
            //workoutComponents[i] = getNextWorkoutComponentFromManager(exStrings[i]);
            String compName = workout.getComponentAt(i).getName();
            exCounts.putIfAbsent(compName, 0);
            try {
                int newCount = Objects.requireNonNull(exCounts.getOrDefault(compName, 0)) + 1;
                numberOfExercise[i] = newCount - 1;
                exCounts.put(compName, newCount);
            } catch (NullPointerException e) {
                Log.e("CurrentWorkout", e.toString());
            }
            setStrings[i] = "" + exCounts.getOrDefault(compName, 0) + "/";
        }
        for (int i = 0; i < workoutLength; i++) {
            String exName = workout.getComponentAt(i).getName();
            int exCount = Objects.requireNonNull(exCounts.get(exName));
            if (!exToResults.containsKey(exName) && !exName.equals("Rest")) {
                String[] res = new String[exCount];
                exToResults.put(exName, res);
            }
            setStrings[i] += "" + exCount;
        }
        String lastWorkoutString = sharedPreferences.getString(workoutName + "last_result", "");
        assert lastWorkoutString != null;
        if (lastWorkoutString.length() > 0) {
            lastWorkout = lastWorkoutString.split(";");
            useLastWorkout = lastWorkout.length == workoutLength;
        }
    }

    public static boolean hasNextExercise() {
        return workout.hasNextExercise();
    }

    public static WorkoutComponent getNextWorkoutComponent() throws IllegalArgumentException {
        if (!hasNextExercise()) {
            throw new IllegalArgumentException("No next exercise!");
        }
        return workout.getCurrentComponent();
    }

    public static void goBack() {
        workout.goBack();
    }

    public static boolean logExercise(String exNum, String repNum) {
        if (exNum == null || repNum == null || exNum.equals("") || repNum.equals("")) {
            return false;
        }
        currentWorkout[workout.getPosition()] = exNum + "," + repNum;
        Objects.requireNonNull(exToResults.get(workout.getCurrentComponent().getName()))[numberOfExercise[workout.getPosition()]] = "+" + exNum + "kg x " + repNum;
        workout.proceed();
        return true;
    }

    public static void logRest(int millis) {
        //String compName=workout.getCurrentComponent().getName();
        //Objects.requireNonNull(exToResults.get(compName))[numberOfExercise[workout.getPosition()]] = "" + millis;
        workout.proceed();
    }

    public static String getPrevResultsInWorkout() {
        String[] prevResults = exToResults.getOrDefault(workout.getCurrentComponent().getName(), null);
        if (prevResults == null || prevResults[0] == null) {
            return "";
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < numberOfExercise[workout.getPosition()]; i++) {
            res.append("Set ").append(i + 1).append(":\t").append(prevResults[i]);
            res.append(System.getProperty("line.separator"));
        }
        return res.toString();
    }

    public static String getWorkoutComponentName() {
        return workout.getCurrentComponent().getName();
    }

    public static String[] getPrevResultsOfCurrentPosition() {
        return lastWorkout[workout.getPosition()].split(",");
    }

    public static String getSetString() {
        return setStrings[workout.getPosition()];
    }
}

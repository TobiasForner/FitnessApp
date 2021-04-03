package com.example.fitnessapp3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;


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
    private static Workout workout;

    public static void finishWorkout(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> lastWorkoutDef = new HashSet<>();
        Set<String> workoutResults = Objects.requireNonNull(sharedPreferences.getStringSet(workoutName + "_results", lastWorkoutDef));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.hh-mm:", Locale.getDefault());
        Date today = Calendar.getInstance().getTime();
        String date = dateFormat.format(today);
        workoutResults.add(date + String.join(";", currentWorkout));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("workout_is_in_progress", false);
        editor.putString("workout_in_progress", "");
        editor.putString(workoutName + "last_result", String.join(";", currentWorkout));
        editor.putStringSet(workoutName + "_results", workoutResults);
        editor.apply();
    }

    public static void init(String workoutName, Activity activity) {
        useLastWorkout = false;
        CurrentWorkout.workoutName = workoutName;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        workout = WorkoutManager.getWorkoutFromFile(workoutName, activity);
        if(workout ==null){
            handleWorkoutDoesNotExist();
            return;
        }
        int workoutLength = workout.getLength();

        currentWorkout = new String[workoutLength];
        setStrings = new String[workoutLength];
        numberOfExercise = new int[workoutLength];
        Map<String, Integer> exCounts = new HashMap<>();
        exToResults = new HashMap<>();
        for (int i = 0; i < workoutLength; i++) {
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
        saveProgress(activity);
    }

    private static void handleWorkoutDoesNotExist(){
        //TODO handle sensibly
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

    public static boolean logExercise(String exNum, String repNum, Activity activity) {
        if (exNum == null || repNum == null || exNum.equals("") || repNum.equals("")) {
            return false;
        }
        currentWorkout[workout.getPosition()] = exNum + "," + repNum;
        Objects.requireNonNull(exToResults.get(workout.getCurrentComponent().getName()))[numberOfExercise[workout.getPosition()]] = "+" + exNum + "kg x " + repNum;
        workout.proceed();
        saveProgress(activity);
        return true;
    }

    public static void logRest(int millis, Activity activity) {
        currentWorkout[workout.getPosition()] = "" + millis;
        workout.proceed();
        saveProgress(activity);
    }


    public static void logDuration(int duration, Activity activity){
        currentWorkout[workout.getPosition()] = "" + duration;
        workout.proceed();
        saveProgress(activity);
    }

    public static void logWeightedDuration(int duration, int weight, Activity activity){
        currentWorkout[workout.getPosition()] = "" + duration + "," + weight;
        workout.proceed();
        saveProgress(activity);
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

    private static void saveProgress(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("workout_is_in_progress", true);
        String sep = System.getProperty("line.separator");
        StringBuilder ex_to_res = new StringBuilder();
        for (String ex : exToResults.keySet()) {
            ex_to_res.append(ex).append(":").append(String.join(";", exToResults.get(ex))).append(sep);
        }
        editor.putString("workout_in_progress", workoutName + sep + String.join(";", currentWorkout) + sep + workout.getPosition() + sep + ex_to_res);
        editor.apply();
    }

    public static void restoreWorkoutInProgress(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (workoutIsInProgress(activity)) {
            String[] workout_details = Objects.requireNonNull(sharedPreferences.getString("workout_in_progress", "")).split(Objects.requireNonNull(System.getProperty("line.separator")));
            if (workout_details.length < 4) {
                Log.e("CurrentWorkout", "workout details of workout in progress do not have the required format");
                return;
            }
            init(workout_details[0], activity);
            String[] wip = workout_details[1].split(";");
            for (int i = 0; i < wip.length; i++) {
                if (wip[i].equals("null")) {
                    break;
                }
                currentWorkout[i] = wip[i];
            }
            while (workout.getPosition() < Integer.parseInt(workout_details[2])) {
                workout.proceed();
            }
            for (int i = 3; i < workout_details.length; i++) {
                if (workout_details[i].equals("")) {
                    continue;
                }
                String[] nameToVal = workout_details[i].split(":");
                if (nameToVal.length < 2) {
                    continue;
                }
                String[] results = nameToVal[1].split(";");
                for (int j = 0; j < results.length; j++) {
                    if (results[j].equals("null")) {
                        continue;
                    }
                    String[] tmp = exToResults.get(nameToVal[0]);
                    assert tmp != null;
                    if (tmp.length != results.length) {
                        Log.e("CurrentWorkout", "length mismatch");
                    }
                    tmp[j] = results[j];
                }
            }
        }
    }

    private static boolean workoutIsInProgress(Activity activity){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPreferences.getBoolean("workout_is_in_progress", false);
    }

    public static int getWorkoutLength() {
        return workout.getLength();
    }

    public static int getWorkoutPosition() {
        return workout.getPosition();
    }

    public static void setProgress(ProgressBar progressBar){
        progressBar.setMin(0);
        progressBar.setMax(getWorkoutLength());
        progressBar.setIndeterminate(false);
        progressBar.setProgress(getWorkoutPosition() + 1);
    }
}

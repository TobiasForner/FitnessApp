package com.example.fitnessapp3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CurrentWorkout {
    public static String[] lastWorkout;
    public static String workoutName;
    public static boolean useLastWorkout;
    public static String[] currentWorkout;
    public static List<String> setStrings;
    protected static List<Integer> numberOfExercise;
    protected static Map<String, String[]> exToResults;
    private static Workout workout;

    private static int currentWorkoutEnqueuePos;

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
        CurrentWorkout.workoutName = workoutName;
        workout = WorkoutManager.getWorkoutFromFile(workoutName, activity);
        if (workout == null) {
            handleWorkoutDoesNotExist();
            return;
        }
        int workoutLength = workout.getLength();
        initFields(workoutLength);

        StringOccurrenceCounter counter = new StringOccurrenceCounter();

        numberOfExercise = workout.getCompNamesStream().mapToInt(counter).boxed().collect(Collectors.toList());

        Map<String, Integer> exCounts = counter.getCountMap();
        setStrings = IntStream.range(0, numberOfExercise.size()).mapToObj(i -> formatSetString(i, exCounts)).collect(Collectors.toList());

        exToResults = exCounts.entrySet().stream().filter(e -> !e.getKey().equals("Rest")).collect(Collectors.toMap(Map.Entry::getKey, e -> new String[e.getValue()]));
        tryInitLastWorkout(activity, workoutLength);
        saveProgress(activity);
    }

    private static String formatSetString(int workoutPos, Map<String, Integer> exCounts){
        int setNum = numberOfExercise.get(workoutPos) + 1;
        String compName = workout.getComponentAt(workoutPos).getName();
        int maxSet = exCounts.get(compName);
        return setNum + "/" + maxSet;
    }

    private static void tryInitLastWorkout(Activity activity, int workoutLength){
        useLastWorkout = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String lastWorkoutString = sharedPreferences.getString(workoutName + "last_result", "");
        assert lastWorkoutString != null;
        if (lastWorkoutString.length() > 0) {
            lastWorkout = lastWorkoutString.split(";");
            useLastWorkout = lastWorkout.length == workoutLength;
        }
    }

    private static void handleWorkoutDoesNotExist() {
        //TODO handle sensibly
    }

    private static void initFields(int workoutLength) {
        currentWorkout = new String[workoutLength];
        setStrings = new ArrayList<>(workoutLength);
        numberOfExercise = new ArrayList<>(workoutLength);
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
        String compName = workout.getCurrentComponent().getName();
        String[] exRes = Objects.requireNonNull(exToResults.get(compName));
        exRes[numberOfExercise.get(workout.getPosition())] = "+" + exNum + "kg x " + repNum;
        workout.proceed();
        saveProgress(activity);
        return true;
    }

    public static void logRest(int millis, Activity activity) {
        currentWorkout[workout.getPosition()] = "" + millis;
        workout.proceed();
        saveProgress(activity);
    }


    public static void logDuration(int duration, Activity activity) {
        currentWorkout[workout.getPosition()] = "" + duration;
        workout.proceed();
        saveProgress(activity);
    }

    public static void logWeightedDuration(int duration, int weight, Activity activity) {
        currentWorkout[workout.getPosition()] = "" + duration + "," + weight;
        workout.proceed();
        saveProgress(activity);
    }

    public static String getPrevResultsInWorkout() {
        String compName = workout.getCurrentComponent().getName();
        String[] prevResults = exToResults.getOrDefault(compName, null);
        if (prevResults == null || prevResults[0] == null) {
            return "";
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < numberOfExercise.get(workout.getPosition()); i++) {
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
        return setStrings.get(workout.getPosition());
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
            String[] workoutDetails = sharedPreferences.getString("workout_in_progress", "").split(System.getProperty("line.separator"));
            restoreWorkoutFromString(workoutDetails, activity);
        }
    }

    private static boolean workoutIsInProgress(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPreferences.getBoolean("workout_is_in_progress", false);
    }

    public static void restoreWorkoutFromString(String[] workoutDetails, Activity activity){
        if (workoutDetails.length < 4) {
            Log.e("CurrentWorkout", "workout details of workout in progress do not have the required format");
            return;
        }
        init(workoutDetails[0], activity);
        List<String> wip = Arrays.asList(workoutDetails[1].split(";"));
        currentWorkoutEnqueuePos = 0;
        wip.forEach(CurrentWorkout::tryAddToWorkout);
        while (workout.getPosition() < Integer.parseInt(workoutDetails[2])) {
            workout.proceed();
        }
        checkPrevWorkoutParse(workoutDetails);
    }

    private static void tryAddToWorkout(String resString){
        if(!resString.equals("null")){
            currentWorkout[currentWorkoutEnqueuePos] = resString;
        }
        currentWorkoutEnqueuePos++;
    }

    private static void checkPrevWorkoutParse(String[] workoutDetails){
        for (int i = 3; i < workoutDetails.length; i++) {
            if (workoutDetails[i].equals("")) {
                continue;
            }
            String[] nameToVal = workoutDetails[i].split(":");
            if (nameToVal.length < 2) {
                continue;
            }
            String[] results = nameToVal[1].split(";");
            for (String result : results) {
                if (result.equals("null")) {
                    continue;
                }
                String[] tmp = exToResults.get(nameToVal[0]);
                assert tmp != null;
                if (tmp.length != results.length) {
                    Log.e("CurrentWorkout", "length mismatch");
                    throw new RuntimeException("Parse of previous workout failed due to length mismatch.");
                }
            }
        }
    }

    public static int getWorkoutLength() {
        return workout.getLength();
    }

    public static int getWorkoutPosition() {
        return workout.getPosition();
    }

    public static void setProgress(ProgressBar progressBar) {
        progressBar.setMin(0);
        progressBar.setMax(getWorkoutLength());
        progressBar.setIndeterminate(false);
        progressBar.setProgress(getWorkoutPosition() + 1);
    }
}

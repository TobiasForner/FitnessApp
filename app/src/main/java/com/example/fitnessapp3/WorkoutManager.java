package com.example.fitnessapp3;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class WorkoutManager {

    private static String[] workoutNames;
    private static ExerciseManager exerciseManager;

    public static void init(Context context) {
        readWorkoutNames(context);
        exerciseManager = new ExerciseManager(context);
    }


    public static void addWorkout(String name, String workoutBody, Context context) {
        addWorkoutName(name, context);
        String filename = name + "_workout.txt";
        File file = new File(context.getFilesDir(), filename);
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            fos.write(workoutBody.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addWorkoutName(String name, Context context) {
        if (Arrays.asList(workoutNames).contains(name)) {
            return;
        }
        String filename = "workout_names.txt";
        String fileContents = name + System.getProperty("line.separator");
        File file = new File(context.getFilesDir(), filename);
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(fileContents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        readWorkoutNames(context);
    }

    public static Workout generateWorkoutFromString(String text, String name, Context context) {
        Workout workout = new Workout(name);
        String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
        for (String line : lines) {
            parseWorkoutLine(line, workout, context);
        }
        return workout;
    }

    private static void parseWorkoutLine(String line, Workout workout, Context context) {
        Log.d("WorkoutManager", "parseWorkoutLine: start");
        if (line.equals("")) {
            return;
        }
        String[] parts = line.split("\\[");
        if (parts.length != 1 && parts.length != 2) {
            Log.e("WorkoutManager", "parseWorkoutLine: line has invalid format: "+line);
            return;
        }
        if (parts.length == 2 && !parts[0].equals("")) {
            Log.e("WorkoutManager", "parseWorkoutLine: line has invalid format: "+line);
            return;
        }
        String[] bodyAndTimes;
        if (parts.length == 2) {
            bodyAndTimes = parts[1].split("]");
        } else {
            bodyAndTimes = parts[0].split("]");
        }
        String[] exerciseNames = bodyAndTimes[0].split(",");
        for (String exName : exerciseNames) {
            if (!exerciseManager.exerciseExists(exName)) {
                Log.e("WorkoutManager", "parseWorkoutLine: line contains invalid exercise: "+line);
                return;
            }

            String strippedName = Util.strip(exName);
            if (!exerciseManager.exerciseExists(strippedName)) {
                exerciseManager.addStrippedExercise(exName, context);
            }
        }
        String timesStr;
        if (bodyAndTimes.length>=2){
            timesStr=bodyAndTimes[1];

        }else {
            // pattern without [...] x ..., default times to 1
            timesStr = "x1";
        }
        timesStr=Util.strip(timesStr);
        if (timesStr.length() >= 2) {
            if (timesStr.charAt(0) == 'x' | timesStr.charAt(0) == 'X') {
                String timesString = timesStr.substring(1);
                try {
                    int times = Integer.parseInt(timesString);
                    for (int i = 0; i < times; i++) {
                        for (String exName : exerciseNames) {
                            String strippedName = Util.strip(exName);
                            workout.addComponent(exerciseManager.getWorkoutComponent(strippedName));
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Log.e("AddWorkoutActivity", "Error parsing number.");
                }
            }
        } else {
            for (String exName : exerciseNames) {
                String strippedName = Util.strip(exName);
                workout.addComponent(exerciseManager.getWorkoutComponent(strippedName));
            }
        }
    }

    private static void readWorkoutNames(Context context) {
        try {
            File file = new File(context.getFilesDir(), "workout_names.txt");
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append(Objects.requireNonNull(System.getProperty("line.separator")));
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } finally {
                String contents = stringBuilder.toString();
                workoutNames = contents.split(Objects.requireNonNull(System.getProperty("line.separator")));
            }
        } catch (FileNotFoundException e) {
            Log.e("WorkoutManager", "Workout Names file workout_names not found.");
            workoutNames = new String[0];
        }

    }

    public static String[] getWorkoutNames() {
        return workoutNames;
    }

    public static String getWorkoutTextFromFile(String workoutName, Context context) {
        if (!Arrays.asList(workoutNames).contains(workoutName)) {
            Log.e("WorkoutManager", "Workout name is not valid.");
            return null;
        }
        String contents;
        try {
            File file = new File(context.getFilesDir(), workoutName + "_workout.txt");
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append(Objects.requireNonNull(System.getProperty("line.separator")));
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } finally {
                contents = stringBuilder.toString().trim();
            }
        } catch (FileNotFoundException e) {
            Log.e("WorkoutManager", "Workout Names file workout_names not found.");
            return null;
        }
        return contents;
    }

    public static Workout getWorkoutFromFile(String workoutName, Context context) {
        if (!Arrays.asList(workoutNames).contains(workoutName)) {
            Log.e("WorkoutManager", "Workout name is not valid.");
            return null;
        }
        String contents;
        try {
            File file = new File(context.getFilesDir(), workoutName + "_workout.txt");
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append(Objects.requireNonNull(System.getProperty("line.separator")));
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } finally {
                contents = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("WorkoutManager", "Workout Names file workout_names not found.");
            return null;
        }
        return generateWorkoutFromString(contents, workoutName, context);
    }

    public static boolean workoutExists(String name) {
        return Arrays.asList(workoutNames).contains(name);
    }

    public static boolean exerciseExists(String exName) {
        return exerciseManager.exerciseExists(exName);
    }

    public static void BackupWorkouts(Context context){
        WorkoutManager.readWorkoutNames(context);
        //TODO store workout content in e.g. json format
    }

    public static String getWorkoutNameInProgress(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (!sharedPreferences.getBoolean("workout_is_in_progress", false)) {
            return null;
        }
        String progress = Util.readFromInternal(Util.WORKOUT_IN_PROGRESS, activity);
        String[] workout_details = Objects.requireNonNull(progress).split(Objects.requireNonNull(System.getProperty("line.separator")));
        return workout_details[0];
    }

    public static void addExercise(String name, String exType, boolean weighted, String abbrev, Context context) {
        exerciseManager.addExercise(name, exType, weighted, abbrev, context);
    }

    public static ArrayList<String> getExerciseNames() {
        return exerciseManager.getExerciseNames();
    }

    public static WorkoutComponent getWorkoutComponentFromName(String name) {
        return exerciseManager.getWorkoutComponent(name);
    }
}

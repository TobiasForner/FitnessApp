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
    }

    public static Workout generateWorkoutFromString(String text, String name) {
        Workout workout = new Workout(name);
        String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
        for (String line : lines) {
            parseWorkoutLine(line, workout);
        }
        return workout;
    }

    private static void parseWorkoutLine(String line, Workout workout) {
        if (line.equals("")) {
            return;
        }
        String[] parts = line.split("\\[");
        if (parts.length != 1 && parts.length != 2) {
            return;
        }
        if (parts.length == 2 && !parts[0].equals("")) {
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
                return;
            }
        }
        if (bodyAndTimes.length == 2 && bodyAndTimes[1].length() >= 2) {
            if (bodyAndTimes[1].charAt(0) == 'x' | bodyAndTimes[1].charAt(0) == 'X') {
                String timesString = bodyAndTimes[1].substring(1);
                try {
                    int times = Integer.parseInt(timesString);
                    for (int i = 0; i < times; i++) {
                        for (String exName : exerciseNames) {
                            workout.addComponent(exerciseManager.getWorkoutComponent(exName));
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Log.e("AddWorkoutActivity", "Error parsing number.");
                }
            }
        } else {
            for (String exName : exerciseNames) {
                workout.addComponent(exerciseManager.getWorkoutComponent(exName));
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
        return generateWorkoutFromString(contents, workoutName);
    }

    public static boolean workoutExists(String name) {
        return Arrays.asList(workoutNames).contains(name);
    }

    public static boolean exerciseExists(String exName) {
        return exerciseManager.exerciseExists(exName);
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

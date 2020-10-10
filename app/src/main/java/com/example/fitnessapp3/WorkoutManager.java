package com.example.fitnessapp3;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class WorkoutManager {

    private static String[] workoutNames;
    private static ExerciseManager exerciseManager;

    public static void init(Context context) {
        readWorkoutNames(context);
        exerciseManager = new ExerciseManager(context);
    }

    public static boolean checkWorkoutString(String text) {
        String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
        for (String line : lines) {
            if (!checkWorkoutLine(line)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkWorkoutLine(String line) {
        if (line.equals("")) {
            return false;
        }
        String[] parts = line.split("\\[");
        if (parts.length != 1 && parts.length != 2) {
            return false;
        }
        if (parts.length == 2 && !parts[0].equals("")) {
            return false;
        }
        String[] bodyAndTimes;
        if (parts.length == 2) {
            bodyAndTimes = parts[1].split("]");
        } else {
            bodyAndTimes = parts[0].split("]");
        }
        if (bodyAndTimes.length == 0) {
            return false;
        }
        String[] exerciseNames = bodyAndTimes[0].split(",");
        for (String exName : exerciseNames) {

            if (!exerciseManager.exerciseExists(exName)) {
                //TODO open popup that asks whether to create the exercise
                return false;
            }
        }
        if (bodyAndTimes.length == 2 && bodyAndTimes[1].length() >= 2) {
            if (bodyAndTimes[1].charAt(0) == 'x' | bodyAndTimes[1].charAt(0) == 'X') {
                String timesString = bodyAndTimes[1].substring(1);
                try {
                    Integer.parseInt(timesString);
                } catch (NumberFormatException nfe) {
                    return false;
                }
            }
        }
        return true;
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
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
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
            if (!checkWorkoutLine(line)) {
                return null;
            }
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
            FileInputStream fis = context.openFileInput("workout_names.txt");
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

    public static Workout getWorkoutFromFile(String workoutName, Context context) {
        if (!Arrays.asList(workoutNames).contains(workoutName)) {
            Log.e("WorkoutManager", "Workout name is not valid.");
            return null;
        }
        String contents = "";
        try {
            FileInputStream fis = context.openFileInput(workoutName + "_workout.txt");
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
}

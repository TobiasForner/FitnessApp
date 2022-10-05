package com.example.fitnessapp3;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.List;
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

        String filenameJSON = name + "_workout.json";
        File fileJSON = new File(context.getFilesDir(), filenameJSON);
        try (FileOutputStream fos = new FileOutputStream(fileJSON, false)) {
            JSONArray workouts = workoutsJSON(context);
            fos.write(workouts.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException | JSONException e) {
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
            Log.e("WorkoutManager", "parseWorkoutLine: line has invalid format: " + line);
            return;
        }
        String[] parts = line.split("\\[");
        if (parts.length != 1 && parts.length != 2) {
            Log.e("WorkoutManager", "parseWorkoutLine: line has invalid format: " + line);
            return;
        }
        if (parts.length == 2 && !parts[0].equals("")) {
            Log.e("WorkoutManager", "parseWorkoutLine: line has invalid format: " + line);
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
                Log.e("WorkoutManager", "parseWorkoutLine: line contains invalid exercise ("+exName+": " + line);
                return;
            }

            String strippedName = Util.strip(exName);
            if (!exerciseManager.exerciseExists(strippedName)) {
                exerciseManager.addStrippedExercise(exName, context);
            }
        }
        String timesStr;
        if (bodyAndTimes.length >= 2) {
            timesStr = bodyAndTimes[1];

        } else {
            // pattern without [...] x ..., default times to 1
            timesStr = "x1";
        }
        timesStr = Util.strip(timesStr);
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

    public static void addExercise(String name, Exercise.ExType exType, boolean weighted, String abbrev, Context context) {
        exerciseManager.addExercise(name, exType, weighted, abbrev, context);
    }

    public static ArrayList<String> getExerciseNames() {
        return exerciseManager.getExerciseNames();
    }

    public static WorkoutComponent getWorkoutComponentFromName(String name) {
        return exerciseManager.getWorkoutComponent(name);
    }

    public static JSONArray workoutsJSON(Context context) throws JSONException {
        List<JSONObject> workouts = new ArrayList<>();
        for (String workoutName : workoutNames) {
            JSONObject workout = getWorkoutJSONFromFile(workoutName, context);
            workouts.add(workout);
        }
        return new JSONArray(workouts);
    }

    public static JSONObject getWorkoutJSONFromFile(String workoutName, Context context) throws JSONException {
        if (!Arrays.asList(workoutNames).contains(workoutName)) {
            throw new IllegalArgumentException("Workout name is not valid.");
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
        return generateWorkoutJSONFromString(contents, workoutName, context);
    }

    public static JSONObject generateWorkoutJSONFromString(String text, String name, Context context) throws JSONException {
        String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
        JSONObject workout = new JSONObject();
        workout.put("name", name);
        List<JSONObject> componentGroups = new ArrayList<>();
        for (String line : lines) {
            JSONObject group=parseWorkoutLineJSON(line, context);
            componentGroups.add(group);
        }
        workout.put("componentGroups", new JSONArray(componentGroups));
        return workout;
    }

    private static JSONObject parseWorkoutLineJSON(String line, Context context) throws JSONException {
        Log.d("WorkoutManager", "parseWorkoutLine: start");
        if (line.equals("")) {
            throw new IllegalArgumentException("Empty workout line.");
        }
        String[] parts = line.split("\\[");
        if (parts.length != 1 && parts.length != 2) {
            throw new IllegalArgumentException("parseWorkoutLine: line has invalid format: " + line);
        }
        if (parts.length == 2 && !parts[0].equals("")) {
            throw new IllegalArgumentException("parseWorkoutLine: line has invalid format: " + line);
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
                throw new IllegalArgumentException("parseWorkoutLine: line contains invalid exercise '"+exName+"': " + line);
            }

            String strippedName = Util.strip(exName);
            if (!exerciseManager.exerciseExists(strippedName)) {
                exerciseManager.addStrippedExercise(exName, context);
            }
        }
        String timesStr;
        if (bodyAndTimes.length >= 2) {
            timesStr = bodyAndTimes[1];

        } else {
            // pattern without [...] x ..., default times to 1
            timesStr = "x1";
        }
        timesStr = Util.strip(timesStr);
        if (timesStr.length() >= 2) {
            if (timesStr.charAt(0) == 'x' | timesStr.charAt(0) == 'X') {
                String timesString = timesStr.substring(1);
                try {
                    int times = Integer.parseInt(timesString);

                    List<String> groupComponents = new ArrayList<>();
                    for (String exName : exerciseNames) {
                        String strippedName = Util.strip(exName);
                        groupComponents.add(strippedName);
                    }
                    JSONObject group = new JSONObject();
                    group.put("repetitions", times);
                    group.put("components", new JSONArray(groupComponents));
                    return group;

                } catch (NumberFormatException nfe) {
                    throw new RuntimeException("Error parsing number.");
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RuntimeException("JSON error.");
                }
            }
            throw new RuntimeException("Invalid state.");
        } else {
            List<String> groupComponents = new ArrayList<>();
            for (String exName : exerciseNames) {
                String strippedName = Util.strip(exName);
                groupComponents.add(strippedName);
            }
            JSONObject group = new JSONObject();
            group.put("repetitions", 1);
            group.put("components", new JSONArray(groupComponents));
            return group;
        }
    }
}

package com.example.fitnessapp3;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class WorkoutManager {

    private final static String workoutFile = "workouts.json";
    private static List<String> workoutNamesList;
    private static ExerciseManager exerciseManager;

    public static void init(Context context) {
        exerciseManager = new ExerciseManager(context);
        readWorkoutNames(context);
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

        try {
            JSONObject workouts = new JSONObject(Objects.requireNonNull(Util.readFromInternal(workoutFile, context)));
            JSONObject toAdd = generateWorkoutJSONFromString(workoutBody, name, context);
            workouts.put(name, toAdd);
            overwriteWorkouts(workouts, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void addWorkoutJSON(JSONObject workout, Context context) {
        try {
            JSONObject workouts = new JSONObject(Objects.requireNonNull(Util.readFromInternal(workoutFile, context)));
            String name = workout.getString("name");
            workouts.put(name, workout);
            overwriteWorkouts(workouts, context);
            readWorkoutNames(context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void addWorkoutName(String name, Context context) {
        if (workoutExists(name)) {
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


    public static void overwriteWorkouts(JSONObject workouts, Context context) {
        Util.writeFileOnInternalStorage(context, workoutFile, workouts.toString());
    }

    private static void saveWorkouts(Context context) {
        try {
            JSONObject workouts = workoutsJSON(context);
            Util.writeFileOnInternalStorage(context, workoutFile, workouts.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static void readWorkoutNames(Context context) {
        String contentsJSON = Util.readFromInternal(workoutFile, context);
        try {
            assert contentsJSON != null;
            JSONObject json = new JSONObject(contentsJSON);
            ArrayList<String> names = new ArrayList<>();
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String key = it.next();
                names.add(key);
            }
            workoutNamesList=names;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveWorkouts(context);
    }

    public static List<String> getWorkoutNamesList() {
        return workoutNamesList;
    }

    public static String getWorkoutText(String workoutName, Context context) {
        try {
            JSONObject workout = getWorkoutJSONFromFile(workoutName, context);
            JSONArray componentGroups = workout.getJSONArray("componentGroups");
            List<String> resLines = new ArrayList<>();
            for (int i = 0; i < componentGroups.length(); i++) {
                JSONObject group = componentGroups.getJSONObject(i);
                String workoutLine = "[";
                int repetitions = group.getInt("repetitions");
                JSONArray exercises = group.getJSONArray("components");
                List<String>exNames = new ArrayList<>();

                for (int ex = 0; ex < exercises.length(); ex++) {
                    String exName = exercises.getString(ex);
                    exNames.add(exName);
                }
                workoutLine+=String.join(",", exNames);
                workoutLine+="] x "+repetitions;
                resLines.add(workoutLine);
            }
            return String.join("\n", resLines);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean workoutExists(String name) {
        return workoutNamesList.contains(name);
    }

    public static boolean exerciseExists(String exName) {
        return exerciseManager.exerciseExists(exName);
    }

    public static void addExercise(String name, Exercise.ExType exType, boolean weighted, Context context) {
        exerciseManager.addExercise(name, exType, weighted, context);
    }

    public static ArrayList<String> getExerciseNames() {
        return exerciseManager.getExerciseNames();
    }

    public static WorkoutComponent getWorkoutComponentFromName(String name) {
        return exerciseManager.getWorkoutComponent(name);
    }

    public static JSONObject workoutsJSON(Context context) throws JSONException {
        String workoutsJSON = Util.readFromInternal(workoutFile, context);
        assert workoutsJSON != null;
        return new JSONObject(workoutsJSON);
    }

    public static Workout getWorkout(String workoutName, Context context) {
        Workout result = new Workout(workoutName);
        try {
            JSONObject workout = getWorkoutJSONFromFile(workoutName, context);
            JSONArray componentGroups = workout.getJSONArray("componentGroups");
            for (int i = 0; i < componentGroups.length(); i++) {
                JSONObject group = componentGroups.getJSONObject(i);
                int repetitions = group.getInt("repetitions");
                JSONArray exercises = group.getJSONArray("components");
                for (int rep = 0; rep < repetitions; rep++) {
                    for (int ex = 0; ex < exercises.length(); ex++) {
                        WorkoutComponent comp = exerciseManager.getWorkoutComponent(exercises.getString(ex));
                        result.addComponent(comp);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject getWorkoutJSONFromFile(String workoutName, Context context) throws JSONException {
        JSONObject workouts = workoutsJSON(context);
        return workouts.getJSONObject(workoutName);
    }

    public static JSONObject generateWorkoutJSONFromString(String text, String name, Context context) throws JSONException {
        String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
        JSONObject workout = new JSONObject();
        workout.put("name", name);
        List<JSONObject> componentGroups = new ArrayList<>();
        for (String line : lines) {
            JSONObject group = parseWorkoutLineJSON(line, context);
            componentGroups.add(group);
        }
        workout.put("componentGroups", new JSONArray(componentGroups));
        return workout;
    }

    private static JSONObject parseWorkoutLineJSON(String line, Context context) throws JSONException {
        //todo improve parsing
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
                throw new IllegalArgumentException("parseWorkoutLine: line contains invalid exercise '" + exName + "': " + line);
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

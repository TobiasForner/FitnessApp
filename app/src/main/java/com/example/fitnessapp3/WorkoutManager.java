package com.example.fitnessapp3;

import android.content.Context;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkoutManager {

    private final static String workoutFile = "workouts.json";
    private static List<String> workoutNamesList;

    private final static Pattern workoutGroupP = Pattern.compile("\\[(.*)]\\s*x?\\s*(\\d+)?$");

    public static void init(Context context) {

        readWorkoutNames(context);
    }

    public static void deleteWorkout(String name, Context context) {
        String contentsJSON = Util.readFromInternal(workoutFile, context);
        try {
            assert contentsJSON != null;
            JSONObject workouts = new JSONObject(contentsJSON);
            workouts.remove(name);
            overwriteWorkouts(workouts, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            JSONObject toAdd = generateWorkoutJSONFromString(workoutBody, name);
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

    public static JSONObject workoutsJSON(Context context) throws JSONException {
        String workoutsJSON = Util.readFromInternal(workoutFile, context);
        assert workoutsJSON != null;
        return new JSONObject(workoutsJSON);
    }

    public static Workout getWorkout(String workoutName, Context context) {
        ExerciseManager exerciseManager = new ExerciseManager(context);
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

    public static JSONObject generateWorkoutJSONFromString(String text, String name) throws JSONException {
        String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
        JSONObject workout = new JSONObject();
        workout.put("name", name);
        List<JSONObject> componentGroups = new ArrayList<>();
        for (String line : lines) {
            //JSONObject group = parseWorkoutLineJSON(line, context);
            JSONObject group = parseWorkoutLineJSONNew(line);
            componentGroups.add(group);
        }
        workout.put("componentGroups", new JSONArray(componentGroups));
        return workout;
    }


    private static JSONObject parseWorkoutLineJSONNew(String line) {
        line = Util.strip(line);
        if (line.equals("")) {
            return null;
        }
        Matcher m = workoutGroupP.matcher(line);
        if (m.matches()) {
            String g = m.group(1);
            assert g != null;
            String[] parts = g.split(",");
            int times = 1;
            if (m.groupCount() == 2) {
                String timesStr = m.group(2);
                try {
                    assert timesStr != null;
                    times = Integer.parseInt(timesStr);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
            return parseGroup(parts, times);

        } else {
            String[] parts = line.split(",");
            return parseGroup(parts, 1);
        }
    }

    private static JSONObject parseGroup(String[] parts, int repetitions) {
        List<String> groupComponents = new ArrayList<>();
        for (String exName : parts) {
            String strippedName = Util.strip(exName);
            groupComponents.add(strippedName);
        }
        try {
            JSONObject group = new JSONObject();
            group.put("components", new JSONArray(groupComponents));
            group.put("repetitions", repetitions);
            return group;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

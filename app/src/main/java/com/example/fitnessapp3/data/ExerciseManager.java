package com.example.fitnessapp3.data;

import android.content.Context;
import android.util.Log;

import com.example.fitnessapp3.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseManager {
    private final Map<String, WorkoutComponent> nameToEx;

    public ExerciseManager(Context context) {
        nameToEx = new HashMap<>();
        readExerciseDetails(context);
        Log.d("ExerciseManager", nameToEx.keySet().toString());
    }

    public void initExerciseDetails(Context context) {
        addExercise("Pull Up", Exercise.ExType.REPS, true, context);
        addExercise("Ring Dip", Exercise.ExType.REPS, true, context);
        addExercise("Push Up", Exercise.ExType.REPS, true, context);
        addExercise("Row", Exercise.ExType.REPS, true, context);
        addExercise("Rest", Exercise.ExType.DURATION, true, context);
    }

    public void addExercise(String name, Exercise.ExType exType, boolean weighted, Context context) {
        String strippedName = Util.strip(name);

        nameToEx.put(strippedName, getExerciseFromDetails(name, exType, weighted));
        writeExercisesToJSON(context);
    }

    public void deleteExercise(String exerciseName, Context context) {
        if (nameToEx.containsKey(exerciseName)) {
            nameToEx.remove(exerciseName);
            writeExercisesToJSON(context);
        }
    }

    private void writeExercisesToJSON(Context context) {
        JSONArray exercises = this.exercisesJson();
        File file = new File(context.getFilesDir(), "exercise_details.json");
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(exercises.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e("ExerciseManager", "writeExercisesToJSON: Could not store exercises");
            e.printStackTrace();
        }
    }

    public JSONArray exercisesJson() {
        List<JSONObject> exercises = new ArrayList<>();
        for (String name : nameToEx.keySet()) {
            WorkoutComponent current = nameToEx.get(name);
            assert current != null;
            try {
                JSONObject fileContents = current.toJSON();

                exercises.add(fileContents);
            } catch (JSONException e) {
                Log.e("ExerciseManager", "exercisesJson: failed to convert component with name '" + name + "' to JSON!");
            }
        }

        return new JSONArray(exercises);
    }

    public void readExerciseDetails(Context context) {
        try {
            String content = Util.readFromInternal("exercise_details.json", context);
            JSONArray exerciseDetails = new JSONArray(content);
            for (int i = 0; i < exerciseDetails.length(); i++) {
                JSONObject compJSON = (JSONObject) exerciseDetails.get(i);
                WorkoutComponent exercise = Exercise.fromJSON(compJSON);
                nameToEx.put(exercise.getName(), exercise);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void overwriteExerciseDetailsJSON(JSONArray json, Context context) throws JSONException {
        for (int i = 0; i < json.length(); i++) {
            JSONObject current = json.getJSONObject(i);
            String name = current.getString("name");
                Exercise exercise = Exercise.fromJSON(current);
                nameToEx.put(name, exercise);
        }
        writeExercisesToJSON(context);
    }

    private boolean isRest(String exName) {
        if (exName.length() >= 4) {
            String start = exName.substring(0, 4);
            return start.equals("Rest");
        }
        return false;
    }

    public WorkoutComponent getWorkoutComponent(String exName) {

        assert exName != null;
        if (nameToEx.containsKey(exName)) {
            return nameToEx.get(exName);
        } else {
            Log.e("ExerciseManager", "Exercise " + exName + " not defined.");
            return null;
        }
    }

    private WorkoutComponent getExerciseFromDetails(String exName, Exercise.ExType exType, boolean weighted) {
        return new Exercise(exName, exType, weighted);
    }

    public boolean exerciseExists(String exName) {
        return isRest(exName) | nameToEx.containsKey(exName);
    }

    public ArrayList<String> getExerciseNames() {
        return new ArrayList<>(nameToEx.keySet());
    }


}

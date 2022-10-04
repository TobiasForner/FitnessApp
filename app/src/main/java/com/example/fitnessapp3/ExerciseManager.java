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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExerciseManager {
    private final Map<String, WorkoutComponent> nameToEx;
    private final Map<String, String> abbrevToFullName;

    public ExerciseManager(Context context) {
        nameToEx = new HashMap<>();
        abbrevToFullName = new HashMap<>();
        readExerciseDetailsJSON(context);
        Log.d("ExerciseManager", nameToEx.keySet().toString());
    }

    public void initExerciseDetails(Context context) {
        addExercise("Pull Up", Exercise.ExType.REPS, true, "PU", context);
        addExercise("Ring Dip", Exercise.ExType.REPS, true, "RD", context);
        addExercise("Push Up", Exercise.ExType.REPS, true, "PshU", context);
        addExercise("Row", Exercise.ExType.REPS, true, "Row", context);
        addExercise("Rest", Exercise.ExType.REST, true, "Rest", context);
    }

    public void addExercise(String name, Exercise.ExType exType, boolean weighted, String abbrev, Context context) {
        if (!abbrev.equals("")) {
            updateAbbreviations(abbrev, name);
        }

        nameToEx.put(name, getExerciseFromDetails(name, exType, weighted, abbrev));
        writeExercisesToJSON(context);
    }

    public void addStrippedExercise(String exName, Context context) {
        String strippedName = Util.strip(exName);
        WorkoutComponent comp = getWorkoutComponent(exName);
        if (comp.isExercise()) {
            Exercise ex = (Exercise) comp;
            addExercise(strippedName, ex.getType(), ex.isWeighted(), ex.getAbbrev(), context);
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

    public JSONObject abbreviationsJson() throws JSONException {
        JSONObject res = new JSONObject();
        for (Map.Entry<String, String> entry : this.abbrevToFullName.entrySet()) {
            res.put(entry.getKey(), entry.getValue());
        }

        return res;
    }

    public void readExerciseDetailsJSON(Context context) {
        try {
            String content = Util.readFromInternal("exercise_details.json", context);
            JSONArray exerciseDetails = new JSONArray(content);
            for (int i = 0; i < exerciseDetails.length(); i++) {
                JSONObject compJSON = (JSONObject) exerciseDetails.get(i);

                if (compJSON.get(WorkoutComponent.typeJSON).equals(Exercise.ExType.REST.toString())) {
                    WorkoutComponent rest = Rest.fromJSON(compJSON);
                    nameToEx.put(rest.getName(), rest);

                } else {
                    WorkoutComponent exercise = Exercise.fromJSON(compJSON);
                    nameToEx.put(exercise.getName(), exercise);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isRest(String exName) {
        if (exName.length() >= 4) {
            String start = exName.substring(0, 4);
            return start.equals("Rest");
        }
        return false;
    }

    private Rest parseRest(String exName) {
        if (exName.length() >= 5) {
            String start = exName.substring(0, 5);
            if (start.equals("Rest:")) {
                int time = Integer.parseInt(exName.substring(5));
                return new Rest(time);
            }
        }
        return new Rest(180000);
    }

    public WorkoutComponent getWorkoutComponent(String exName) {
        String name = abbrevToFullName.getOrDefault(exName, exName);
        assert name != null;
        if (isRest(name)) {
            return parseRest(name);
        } else if (nameToEx.containsKey(name)) {
            return nameToEx.get(name);
        } else {
            Log.e("ExerciseManager", "Exercise " + exName + " not defined.");
            return null;
        }
    }

    private WorkoutComponent getExerciseFromDetails(String exName, Exercise.ExType exType, boolean weighted, String abbrev) {
        if (exType == Exercise.ExType.REST) {
            return new Rest(180000);
        }
        Exercise exercise = new Exercise(exName, exType, weighted);
        exercise.setAbbrev(abbrev);
        return exercise;
    }

    private void updateAbbreviations(String abbrev, String name) {
        String[] abbreviations = abbrev.split(",");
        for (String abbreviation : abbreviations) {
            abbrevToFullName.put(abbreviation, name);
        }
    }

    public boolean exerciseExists(String exName) {
        return isRest(exName) | nameToEx.containsKey(exName);
    }

    public ArrayList<String> getExerciseNames() {
        return new ArrayList<>(nameToEx.keySet());
    }


}

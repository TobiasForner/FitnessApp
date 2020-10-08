package com.example.fitnessapp3;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExerciseManager {
    private Map<String, Exercise> nameToEx;
    private Map<String, String> abbrevToFullName;

    public ExerciseManager(Context context) {
        nameToEx = new HashMap<>();
        abbrevToFullName = new HashMap<>();
        readExerciseDetails(context);
    }

    public void initExerciseDetails(Context context) {
        addExercise("Pull Up", "Reps", true, "PU", context);
        addExercise("Ring Dip", "Reps", true, "RD", context);
        addExercise("Push Up", "Reps", true, "PshU", context);
        addExercise("Row", "Reps", true, "Row", context);
        addExercise("Rest", "Rest", true, "Rest", context);
    }

    public void addExercise(String name, String exType, boolean weighted, String abbrev, Context context) {
        if (nameToEx.containsKey(name)) {
            return;
        }
        try (FileOutputStream fos = context.openFileOutput("exercise_details.txt", Context.MODE_PRIVATE + Context.MODE_APPEND)) {
            String sep = Objects.requireNonNull(System.getProperty("line.separator"));
            String abbrevString = "";
            if (!abbrev.equals("")) {
                updateAbbreviations(abbrev, name);
            }
            String fileContents = name + ":ExerciseType=" + exType + ";Weighted=" + weighted + abbrevString + sep;
            fos.write(fileContents.getBytes(StandardCharsets.UTF_8));
            nameToEx.put(name, getExerciseFromDetails(name, exType, weighted, abbrev));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readExerciseDetails(Context context) {
        try {
            FileInputStream fis = context.openFileInput("exercise_details.txt");
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    String[] exerciseDet = line.split(":");
                    String[] details = exerciseDet[1].split(";");
                    if (details.length < 2) {
                        Log.e("ExerciseManager", "Details length invalid (smaller than 2).");
                    } else {
                        String exTypeString = details[0].split("=")[1];
                        boolean weighted = details[1].split("=")[1].equals("true");
                        Exercise exercise = getExerciseFromDetails(exerciseDet[0], exTypeString, weighted, "");
                        if (exercise.getType() == Exercise.EXTYPE.REST) {
                            exercise.setParameter(180000);
                        }
                        nameToEx.put(exerciseDet[0], exercise);
                        if (details.length == 3) {
                            String abbrev = details[2].split("=")[1];
                            exercise.setAbbrev(abbrev);
                            updateAbbreviations(abbrev, exercise.getName());
                        }
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            }
        } catch (FileNotFoundException e) {
            Log.e("ExerciseManager", "Exercise details file exercise_details not found.");
        }
    }

    public Exercise getExercise(String exName) {
        String name = abbrevToFullName.getOrDefault(exName, exName);
        if (nameToEx.containsKey(name)) {
            return nameToEx.get(name);
        } else {
            Log.e("ExerciseManager", "Exercise " + exName + " not defined.");
            return null;
        }
    }

    private Exercise getExerciseFromDetails(String exName, String exType, boolean weighted, String abbrev) {
        Exercise.EXTYPE type = Exercise.EXTYPE.WEIGHT;
        if (exType.equals("Duration")) {
            type = Exercise.EXTYPE.DURATION;
        } else if (exType.equals("Rest")) {
            type = Exercise.EXTYPE.REST;
        }
        Exercise exercise = new Exercise(exName, type, weighted);
        exercise.setAbbrev(abbrev);
        return exercise;
    }

    private void updateAbbreviations(String abbrev, String name) {
        String[] abbreviations = abbrev.split(",");
        for (String abbreviation : abbreviations) {
            abbrevToFullName.put(abbreviation, name);
        }
    }
}

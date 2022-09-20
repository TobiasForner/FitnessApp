package com.example.fitnessapp3;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExerciseManager {
    private final Map<String, WorkoutComponent> nameToEx;
    private final Map<String, String> abbrevToFullName;

    public ExerciseManager(Context context) {
        nameToEx = new HashMap<>();
        abbrevToFullName = new HashMap<>();
        readExerciseDetails(context);
    }

    public void initExerciseDetails(Context context) {
        //TODO: use enum asap instead of hardcoded Strings
        addExercise("Pull Up", "Reps", true, "PU", context);
        addExercise("Ring Dip", "Reps", true, "RD", context);
        addExercise("Push Up", "Reps", true, "PshU", context);
        addExercise("Row", "Reps", true, "Row", context);
        addExercise("Rest", "Rest", true, "Rest", context);
    }

    public void addExercise(String name, String exType, boolean weighted, String abbrev, Context context) {
        if (nameToEx.containsKey(name)) {
            //TODO ask whether user wants to overwrite
            nameToEx.put(name, getExerciseFromDetails(name, exType, weighted, abbrev));
            writeExercisesToFile(context);
            return;
        }
        try {
            File file = new File(context.getFilesDir(), "exercise_details.txt");
            FileOutputStream fos = new FileOutputStream(file, true);
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

    public void addStrippedExercise(String exName, Context context) {
        String strippedName = Util.strip(exName);
        WorkoutComponent comp = getWorkoutComponent(exName);
        if(comp.isExercise()){
            Exercise ex = (Exercise) comp;
        addExercise(strippedName, Exercise.typeToString(ex.getType()), ex.isWeighted(), ex.getAbbrev(), context);}
    }

    private void writeExercisesToFile(Context context){
        StringBuilder out = new StringBuilder();
        for(String name: nameToEx.keySet()){
            WorkoutComponent current = nameToEx.get(name);
            assert current != null;
            String fileContents = componentToString(current);
            out.append(fileContents);
        }
        File file = new File(context.getFilesDir(), "exercise_details.txt");
        try{
        FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(out.toString().getBytes(StandardCharsets.UTF_8));
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String componentToString(WorkoutComponent comp){
        String sep = Objects.requireNonNull(System.getProperty("line.separator"));
        if(comp.isRest()){
            Rest r = (Rest) comp;
            return "Rest:"+r.getRestTime()+sep;
        }else{
            Exercise e = (Exercise) comp;
            String exType;
            if (e.getType() == Exercise.EXTYPE.DURATION) {
                exType = "Duration";
            } else {
                exType = "Reps";
            }
            String name = e.getName();
            boolean weighted = e.isWeighted();
            return name + ":ExerciseType=" + exType + ";Weighted=" + weighted + sep;
        }

    }

    public void readExerciseDetails(Context context) {
        try {
            File file = new File(context.getFilesDir(), "exercise_details.txt");
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    String[] exerciseDet = line.split(":");
                    String[] details = exerciseDet[1].split(";");
                    if (details.length < 2) {
                        Log.e("ExerciseManager", "Details length invalid (smaller than 2).");
                    }
                    if (isRest(exerciseDet[0])) {
                        Rest rest = parseRest(exerciseDet[0]);
                        nameToEx.put(exerciseDet[0], rest);
                    } else {
                        String exTypeString = details[0].split("=")[1];
                        boolean weighted = details[1].split("=")[1].equals("true");
                        WorkoutComponent component = getExerciseFromDetails(exerciseDet[0], exTypeString, weighted, "");
                        nameToEx.put(exerciseDet[0], component);
                        if (details.length == 3) {
                            Exercise exercise = (Exercise) component;
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

    private boolean isRest(String exName){
        if(exName.length()>=4){
            String start = exName.substring(0,4);
            return start.equals("Rest");
        }
        return false;
    }

    private Rest parseRest(String exName){
        if(exName.length()>=5) {
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
        if(isRest(name)){
            return parseRest(name);
        }
        else if (nameToEx.containsKey(name)) {
            return nameToEx.get(name);
        } else {
            Log.e("ExerciseManager", "Exercise " + exName + " not defined.");
            return null;
        }
    }

    private WorkoutComponent getExerciseFromDetails(String exName, String exType, boolean weighted, String abbrev) {
        Exercise.EXTYPE type = Exercise.EXTYPE.REPS;
        if (exType.equals("Duration")) {
            type = Exercise.EXTYPE.DURATION;
        } else if (exType.equals("Rest")) {
            return new Rest(180000);
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

    public boolean exerciseExists(String exName) {
        return isRest(exName) | nameToEx.containsKey(exName);
    }

    public ArrayList<String> getExerciseNames() {
        return new ArrayList<>(nameToEx.keySet());
    }
}

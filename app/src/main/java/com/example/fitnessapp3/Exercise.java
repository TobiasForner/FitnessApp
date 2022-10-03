package com.example.fitnessapp3;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Exercise extends WorkoutComponent {

    enum ExType {
        REST, REPS, DURATION;

        public static ExType fromString(String exType) {
            if (exType.equals("Rest")) {
                return REST;
            } else if (exType.equals("Reps")) {
                return REPS;
            } else {
                return DURATION;
            }
        }

        @NonNull
        @Override
        public String toString() {
            if (this == REST) {
                return "Rest";
            } else if (this == REPS) {
                return "Reps";
            } else {
                return "Duration";
            }
        }
    }

    public static String typeToString(ExType type){
        if(type==ExType.DURATION){
            return "Duration";
        }
        else {
            return "Reps";
        }
    }

    private final String name;
    private final ExType type;
    private int parameter;//weight for weighted exercises, duration for duration-based exercises
    private final boolean weighted;
    private String abbrev;

    public Exercise(String name, ExType type, boolean weighted) {
        this.name = name;
        this.type = type;
        this.weighted = weighted;
        this.parameter = 0;
        this.abbrev = "";
    }

    public String getName() {
        return this.name;
    }

    public ExType getType() {
        return type;
    }

    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    public String getAbbrev() {
        return this.abbrev;
    }

    public boolean isWeighted() {
        return weighted;
    }

    public boolean isExercise() {
        return true;
    }

    public boolean isRest() {
        return false;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject res = new JSONObject();

        String name = this.getName();
        res.put(nameJSON, name);
        String exType = this.getType().toString();

        res.put(typeJSON, exType);

        boolean weighted = this.isWeighted();
        res.put(weightedJSON, weighted);
        return res;
    }

    public static Exercise fromJSON(JSONObject object) throws JSONException {
        String name = (String) object.get(nameJSON);
        ExType type = ExType.fromString((String)object.get(typeJSON));
        boolean weighted = (boolean) object.get(weightedJSON);
        return new Exercise(name, type, weighted);
    }
}

package com.example.fitnessapp3;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class WorkoutComponent {
    public static final String typeJSON = "type";
    public static final String timeJSON = "time";
    public static final String nameJSON = "name";
    public static final String weightedJSON = "weighted";

    public abstract boolean isExercise();

    public abstract boolean isRest();

    public abstract String getName();

    public abstract JSONObject toJSON() throws JSONException;
}

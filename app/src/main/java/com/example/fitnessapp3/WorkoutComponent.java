package com.example.fitnessapp3;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class WorkoutComponent {
    public final String typeJSON = "type";
    public final String timeJSON = "time";
    public final String nameJSON = "name";
    public final String weightedJSON = "weighted";
    public abstract boolean isExercise();

    public abstract boolean isRest();

    public abstract String getName();

    public abstract JSONObject toJSON() throws JSONException;

    public abstract WorkoutComponent fromJSON(JSONObject object) throws JSONException;
}

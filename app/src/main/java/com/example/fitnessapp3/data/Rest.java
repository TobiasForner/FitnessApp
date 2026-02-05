package com.example.fitnessapp3.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Rest extends WorkoutComponent {
    private final int restTime;

    Rest(int restTime) {
        this.restTime = restTime;
    }

    public boolean isExercise() {
        return false;
    }

    public boolean isRest() {
        return true;
    }

    public int getRestTime() {
        return this.restTime;
    }

    public String getName() {
        return "Rest";
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject res = new JSONObject();

        res.put(nameJSON, "Rest");
        res.put(timeJSON, restTime);
        return res;
    }

    public static Rest fromJSON(JSONObject object) throws JSONException {

        if(!object.has(timeJSON)){
            Log.e("Rest", "Cant parse "+object);
        }
        int time = (int)object.get(timeJSON);
        return new Rest(time);
    }
}

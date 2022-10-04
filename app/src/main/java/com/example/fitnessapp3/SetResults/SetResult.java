package com.example.fitnessapp3.SetResults;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SetResult {
    private int addedWeight;
    private int repNr; //either the number of reps or the duration of the current set
    private boolean isDuration;

    private final static String addedWeightJSON = "addedWeight";
    private final static String repNrJSON = "repNr";
    private final static String isDurationJSON = "isDuration";

    public SetResult(int addedWeight, int repNr){
        this.addedWeight=addedWeight;
        this.repNr=repNr;
        this.isDuration=false;
    }

    public int getAddedWeight(){
        return addedWeight;
    }

    public int getRepNr(){
        return repNr;
    }

    public boolean isDuration(){
        return isDuration;
    }

    public void setAddedWeight(int addedWeight){
        this.addedWeight=addedWeight;    }

    public void setRepNr(int repNr){
        this.repNr=repNr;
    }

    public void setIsDuration(boolean isDuration){
        this.isDuration=isDuration;
    }

    @NonNull
    public String toString(){
        return "+"+addedWeight+" x "+repNr;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject res = new JSONObject();
        res.put(addedWeightJSON, addedWeight);
        res.put(repNrJSON, repNr);
        res.put(isDurationJSON, isDuration);
        return res;
    }

    public static SetResult fromJSON(JSONObject json) throws JSONException {
        int addedWeight = (int)json.get(addedWeightJSON);
        int repNr= (int)json.get(repNrJSON);
        boolean isDuration= (boolean)json.get(isDurationJSON);
        SetResult res = new SetResult(addedWeight, repNr);
        res.setIsDuration(isDuration);
        return res;
    }

}

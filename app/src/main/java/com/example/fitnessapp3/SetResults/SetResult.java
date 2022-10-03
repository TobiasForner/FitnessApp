package com.example.fitnessapp3.SetResults;

import androidx.annotation.NonNull;

public class SetResult {
    private int addedWeight;
    private int repNr; //either the number of reps or the duration of the current set
    private boolean isDuration;

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

    public String repr(){
        return addedWeight+";"+repNr+";"+isDuration;
    }
}

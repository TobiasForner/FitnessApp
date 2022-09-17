package com.example.fitnessapp3;

import androidx.annotation.NonNull;

public class SetResult {
    private int addedWeight;
    private int repNr;

    public SetResult(int addedWeight, int repNr){
        this.addedWeight=addedWeight;
        this.repNr=repNr;
    }

    public int getAddedWeight(){
        return addedWeight;
    }

    public int getRepNr(){
        return repNr;
    }

    @NonNull
    public String toString(){
        return "+"+addedWeight+" x "+repNr;
    }
}

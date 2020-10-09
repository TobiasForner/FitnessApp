package com.example.fitnessapp3;

public class Exercise extends WorkoutComponent {
    public enum EXTYPE {
        WEIGHT, DURATION, REST, REPS
    }

    private String name;
    private EXTYPE type;
    private int parameter;//weight for weighted exercises, duration for duration-based exercises
    private boolean weighted;
    private String abbrev;

    public Exercise(String name, EXTYPE type, boolean weighted) {
        this.name = name;
        this.type = type;
        this.weighted = weighted;
        this.parameter = 0;
        this.abbrev = "";
    }

    public String getName() {
        return this.name;
    }

    public EXTYPE getType() {
        return type;
    }

    public void setParameter(int x) {
        this.parameter = x;
    }

    public int getParameter() {
        return this.parameter;
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
}

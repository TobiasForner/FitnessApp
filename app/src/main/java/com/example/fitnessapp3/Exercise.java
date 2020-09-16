package com.example.fitnessapp3;

public class Exercise {
    public enum EXTYPE {
        WEIGHT, DURATION, REST
    }

    private String name;
    private EXTYPE type;

    public Exercise(String name, EXTYPE type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public EXTYPE getType() {
        return type;
    }
}

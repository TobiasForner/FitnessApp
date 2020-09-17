package com.example.fitnessapp3;

public class Exercise {
    public enum EXTYPE {
        WEIGHT, DURATION, REST
    }

    private String name;
    private EXTYPE type;
    private int parameter;//weight for weighted exercises, duration for duration-based exercises

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

    public void setParameter(int x) {
        this.parameter = x;
    }

    public int getParameter() {
        return this.parameter;
    }
}

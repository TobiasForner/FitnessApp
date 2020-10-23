package com.example.fitnessapp3;

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
}

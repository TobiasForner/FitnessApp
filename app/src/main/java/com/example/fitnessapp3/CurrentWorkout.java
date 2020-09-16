package com.example.fitnessapp3;

public class CurrentWorkout {
    public static Exercise[] exercises;
    public static int position = 0;

    public static void initExercises() {
        exercises = new Exercise[6];
        for (int i = 0; i < exercises.length; i++) {
            if (i % 2 == 1) {
                exercises[i] = new Exercise("Rest", Exercise.EXTYPE.REST);
            } else {
                exercises[i] = new Exercise("Pullup", Exercise.EXTYPE.WEIGHT);
            }
        }
    }
}

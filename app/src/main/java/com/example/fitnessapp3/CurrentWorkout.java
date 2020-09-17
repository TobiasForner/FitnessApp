package com.example.fitnessapp3;

public class CurrentWorkout {
    public static Exercise[] exercises;
    public static int position = 0;

    public static void initExercises() {
        position = 0;
        exercises = new Exercise[6];
        for (int i = 0; i < exercises.length; i++) {
            if (i % 2 == 1) {
                exercises[i] = new Exercise("Rest", Exercise.EXTYPE.REST);
                exercises[i].setParameter(60000);
            } else {
                exercises[i] = new Exercise("Pullup", Exercise.EXTYPE.WEIGHT);
            }
        }
    }

    public static boolean hasNextExercise() {
        return position < exercises.length;
    }

    public static Exercise getNextExercise() throws IllegalArgumentException {
        if (!hasNextExercise()) {
            throw new IllegalArgumentException("No next exercise!");
        }
        return exercises[position];
    }
}

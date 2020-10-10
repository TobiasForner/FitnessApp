package com.example.fitnessapp3;

import java.util.ArrayList;
import java.util.List;

public class Workout {
    private int position;
    private List<WorkoutComponent> workoutComponents;
    private String workoutName;

    public Workout(String name) {
        this.workoutName = name;
        this.position = 0;
        this.workoutComponents = new ArrayList<>();
    }

    public void addComponent(WorkoutComponent comp) {
        workoutComponents.add(comp);
    }

    public void proceed() {
        if (position < workoutComponents.size()) {
            this.position += 1;
        }
    }

    public void goBack() {
        if (position > 0) {
            this.position -= 1;
        }
    }

    public WorkoutComponent getCurrentComponent() {
        return workoutComponents.get(position);
    }

    public String getName() {
        return workoutName;
    }

    public int getLength() {
        return workoutComponents.size();
    }

    public WorkoutComponent getComponentAt(int index) {
        return workoutComponents.get(index);
    }

    public boolean hasNextExercise() {
        return position < workoutComponents.size();
    }

    public int getPosition(){
        return position;
    }
}

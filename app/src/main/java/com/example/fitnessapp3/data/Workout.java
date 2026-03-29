package com.example.fitnessapp3.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Workout {
    private final List<WorkoutComponent> workoutComponents;
    private final String workoutName;

    public Workout(String name) {
        this.workoutName = name;
        this.workoutComponents = new ArrayList<>();
    }

    public void addComponent(WorkoutComponent comp) {
        workoutComponents.add(comp);
    }

    public WorkoutComponent getComponentAtPosition(int position) {
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

    public Stream<String> getCompNamesStream(){
        return workoutComponents.stream().map(WorkoutComponent::getName);
    }
}

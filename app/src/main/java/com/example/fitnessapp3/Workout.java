package com.example.fitnessapp3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Workout {
    private int position;
    private final List<WorkoutComponent> workoutComponents;
    private final String workoutName;

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
        if(position<workoutComponents.size()){
            return workoutComponents.get(position);
        }else{
            return null;
        }
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

    public Stream<String> getCompNamesStream(){
        return workoutComponents.stream().map(WorkoutComponent::getName);
    }
}

package com.example.fitnessapp3;

import androidx.annotation.NonNull;

public class WorkoutStats {
    int count;

    String lastCompletedDate;

    int posInSortedNames;

    public WorkoutStats(int count, String lastCompletedDate, int posInSortedNames) {
        this.count = count;
        this.lastCompletedDate = lastCompletedDate;
        this.posInSortedNames = posInSortedNames;
    }

    public int compareTo(WorkoutStats other) {
        if (this.lastCompletedDate.compareTo(other.lastCompletedDate) == 0) {
            if (this.count > other.count) {
                return -1;
            }
            return Integer.compare(this.posInSortedNames, other.posInSortedNames);
        } else {
            return -1 * this.lastCompletedDate.compareTo(other.lastCompletedDate);
        }
    }

    @NonNull
    public String toString() {
        return "WorkoutStats(count=" + this.count + ", lastCompletedDate=" + lastCompletedDate + ", posInSortedNames=" + posInSortedNames + ")";
    }
}

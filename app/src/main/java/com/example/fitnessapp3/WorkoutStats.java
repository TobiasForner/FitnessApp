package com.example.fitnessapp3;

import androidx.annotation.NonNull;

public class WorkoutStats {
    int count;

    String lastCompletedDate;

    int posInSortedNames;
    int posInSortedCounts;

    int posInSortedDates;

    public WorkoutStats(int count, String lastCompletedDate, int posInSortedNames, int posInSortedCounts, int posInSortedDates) {
        this.count = count;
        this.lastCompletedDate = lastCompletedDate;
        this.posInSortedNames = posInSortedNames;
        this.posInSortedCounts = posInSortedCounts;
        this.posInSortedDates = posInSortedDates;
    }

    private double score() {
        return 0.6 * posInSortedCounts + 0.4 * posInSortedDates;
    }

    public int compareTo(WorkoutStats other) {
        int scoreComparison = Double.compare(this.score(), other.score());
        if (scoreComparison == 0) {
            return -1 * this.lastCompletedDate.compareTo(other.lastCompletedDate);
        }
        return -1 * scoreComparison;
    }

    @NonNull
    public String toString() {
        return "WorkoutStats(count=" + this.count + ", lastCompletedDate=" + lastCompletedDate + ", posInSortedNames=" + posInSortedNames + ")";
    }
}

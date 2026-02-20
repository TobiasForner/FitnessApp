package com.example.fitnessapp3.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.fitnessapp3.data.CurrentWorkout

@Composable
fun ExerciseHeader(exerciseName:String) {
    val done = MutableList(CurrentWorkout.getWorkoutLength()) { false }
    repeat(CurrentWorkout.getWorkoutPosition()){
        index->
        done[index]=true

    }
    WorkoutProgressBar(Modifier, CurrentWorkout.getWorkoutLength(), CurrentWorkout.getWorkoutPosition(), done)
    Text(exerciseName, fontSize = 40.sp)
    Text(CurrentWorkout.getSetString(), fontSize = 15.sp)
}
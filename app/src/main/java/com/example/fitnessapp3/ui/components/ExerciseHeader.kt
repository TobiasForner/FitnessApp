package com.example.fitnessapp3.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.fitnessapp3.data.CurrentWorkout

@Composable
fun ExerciseHeader(
    exerciseName: String,
    workoutPosition: Int,
    done: MutableList<Boolean>?=null
) {
    val donePositions = done?:MutableList(CurrentWorkout.getWorkoutLength()) { false }
    if(done==null){
    repeat(CurrentWorkout.getWorkoutLength()) { index ->
        donePositions[index]= CurrentWorkout.positionIsFinished(index)
    }}

    WorkoutProgressBar(Modifier, CurrentWorkout.getWorkoutLength(), workoutPosition, donePositions)
    Text(exerciseName, fontSize = 40.sp)
    Text(CurrentWorkout.getSetStringForPosition(workoutPosition), fontSize = 15.sp)
}
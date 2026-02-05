package com.example.fitnessapp3.ui.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.sp
import com.example.fitnessapp3.data.CurrentWorkout

@Composable
fun ExerciseHeader(exerciseName:String, progress:Float) {
    LinearProgressIndicator(progress = { progress }, strokeCap = StrokeCap.Round)
    Text(exerciseName, fontSize = 40.sp)
    Text(CurrentWorkout.getSetString(), fontSize = 15.sp)
}
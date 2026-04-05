package com.example.fitnessapp3.ui.components

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.fitnessapp3.SetResults.SetResult
import com.example.fitnessapp3.data.CurrentWorkout
import com.example.fitnessapp3.data.Exercise


@Composable
fun RepExerciseMainContent(
    modifier: Modifier,
    workoutPosition: Int,
    afterFinish: () -> Unit
) {
    val activity = LocalActivity.current
    val workoutComponent = CurrentWorkout.getWorkoutComponentAtPosition(workoutPosition)

    var finished by rememberSaveable {
        mutableStateOf(
            CurrentWorkout.positionIsFinished(
                workoutPosition
            )
        )
    }

    val setResult: SetResult? = CurrentWorkout.getPositionPrevSetResult(workoutPosition)

    val isWeighted = (workoutComponent as Exercise).isWeighted
    var text by rememberSaveable { mutableStateOf(setResult?.addedWeight?.toString() ?: "0") }

    var repNumber by rememberSaveable { mutableIntStateOf(setResult?.repNr ?: 10) }

    var showWeightError by rememberSaveable { mutableStateOf(false) }
    if (finished) {
        val setResult = CurrentWorkout.getSetResultForPosition(workoutPosition)
        Column {
            Text("Finished!", fontSize = 60.sp)
            Text("${setResult.repNr} reps")
            if (isWeighted) {
                Text("Weight: ${setResult.addedWeight} kg")
            }
        }

    } else {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {

            NumberStepper(
                repNumber,
                IntRange(0, 100),
                onChange = { repNumber = it },
                cycling = false
            )

            if (isWeighted) {
                Spacer(modifier = Modifier.weight(0.1f))
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
            Button(
                onClick = {
                    // stored as float in preparation of using float for weight
                    val weight = text.toFloatOrNull()
                    if (weight == null) {
                        showWeightError = true
                    } else {
                        logExercise(repNumber, weight.toInt(), workoutPosition, activity)
                        finished = true
                        afterFinish()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Log", fontSize = 30.sp) }

            Spacer(modifier = Modifier.weight(1f))
            val prevResults = CurrentWorkout.getPrevResultsInWorkoutForPosition(workoutPosition)
            if (!prevResults.isEmpty()) {
                Text(prevResults, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (showWeightError) {
                AlertDialog(
                    onDismissRequest = { showWeightError = false },
                    title = { Text("Invalid input") },
                    text = { Text("Please enter a valid number.") },
                    confirmButton = {
                        TextButton(onClick = { showWeightError = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

fun logExercise(repNumber: Int, weight: Int?, workoutPosition: Int, activity: Activity?) {
    if (weight != null) {
        CurrentWorkout.logExercise(weight, repNumber, activity, workoutPosition)
    } else {
        CurrentWorkout.logExercise(0, repNumber, activity, workoutPosition)
    }
}
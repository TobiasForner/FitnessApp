package com.example.fitnessapp3.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp3.data.CurrentWorkout
import com.example.fitnessapp3.data.Exercise
import com.example.fitnessapp3.ui.components.ExerciseHeader
import com.example.fitnessapp3.ui.components.NumberStepper
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme

class RepExerciseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ActivityContent()
        }
    }
}

@Composable
private fun ActivityContent() {
    val name = CurrentWorkout.getWorkoutComponentName() ?: "Exercise name"

    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ExerciseHeader(exerciseName = name)

                Spacer(modifier = Modifier.weight(1f))

                RepExerciseMainContent(modifier = Modifier.weight(5f))
            }
        }
    }
}

@Composable
fun RepExerciseMainContent(modifier: Modifier) {
    val activity = LocalActivity.current

    val workoutPosition = CurrentWorkout.getWorkoutPosition()

    val setResult = if (!CurrentWorkout.useLastWorkout) {
        CurrentWorkout.getPrevSetResultsOfCurrentExercise()
    } else {
        CurrentWorkout.getPrevSetResultsOfCurrentPosition()
    }

    val isWeighted = (CurrentWorkout.getCurrentWorkoutComponent() as Exercise).isWeighted
    var text by rememberSaveable { mutableStateOf(setResult.addedWeight.toString()) }

    var repNumber by rememberSaveable { mutableIntStateOf(setResult.repNr) }

    var showWeightError by rememberSaveable { mutableStateOf(false) }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        NumberStepper(repNumber, IntRange(0, 100), onChange = { repNumber = it }, cycling = false)

        if (isWeighted) {
            Spacer(modifier = Modifier.weight(0.1f))
            TextField(
                value = setResult.addedWeight.toString(),
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
                    goToNextActivity(activity)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Log", fontSize = 30.sp) }

        Spacer(modifier = Modifier.weight(1f))
        val prevResults = CurrentWorkout.getPrevResultsInWorkout()
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

fun logExercise(repNumber: Int, weight: Int?, workoutPosition: Int, activity: Activity?) {
    if (weight != null) {
        CurrentWorkout.logExercise(weight, repNumber, activity, workoutPosition)
    } else {
        CurrentWorkout.logExercise(0, repNumber, activity, workoutPosition)
    }
}

private fun goToNextActivity(activity: Activity?) {
    if (!CurrentWorkout.hasCurrentExercise()) {
        CurrentWorkout.finishWorkout(activity)
        activity?.startActivity(ActivityTransition.goToNextActivityInWorkout(activity))
        activity?.finish()
    }
    activity?.startActivity(ActivityTransition.goToNextActivityInWorkout(activity))
}
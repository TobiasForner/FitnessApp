package com.example.fitnessapp3.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp3.SetResults.SetResult
import com.example.fitnessapp3.data.CurrentWorkout
import com.example.fitnessapp3.data.Exercise
import com.example.fitnessapp3.ui.components.ExerciseHeader
import com.example.fitnessapp3.ui.components.TimerViewModel
import com.example.fitnessapp3.ui.components.NumberStepper
import com.example.fitnessapp3.ui.components.Timer
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme

class DurationExerciseActivity2 : ComponentActivity() {
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
    val activity = LocalActivity.current

    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ExerciseHeader(name)
                Spacer(modifier = Modifier.weight(1f))
                DurationExerciseMainContent(
                    modifier = Modifier.weight(5f),
                    afterFinish = { goToNextActivity(activity) })
            }
        }
    }
}

@Composable
fun DurationExerciseMainContent(
    modifier: Modifier,
    workoutPosition: Int = CurrentWorkout.getWorkoutPosition(),
    afterFinish: () -> Unit
) {
    val activity = LocalActivity.current
    val setResult: SetResult? = CurrentWorkout.getPositionSetResult(workoutPosition)

    var pickedSeconds by rememberSaveable { mutableIntStateOf(setResult?.repNr?.mod(60) ?: 30) }
    var pickedMinutes by rememberSaveable { mutableIntStateOf(setResult?.repNr?.div(60) ?: 0) }


    val isWeighted = (CurrentWorkout.getCurrentWorkoutComponent() as Exercise).isWeighted
    var text by remember { mutableStateOf(setResult?.addedWeight?.toString() ?: "0") }
    val weight = try {
        text.toFloat()
    } catch (_: NumberFormatException) {
        0f
    }

    var finished by rememberSaveable {
        mutableStateOf(
            CurrentWorkout.positionIsFinished(
                workoutPosition
            )
        )
    }
    var timerStarted by rememberSaveable { mutableStateOf(false) }

    val timerViewModel: TimerViewModel = viewModel()
    if (finished) {
        Column {
            Text("Finished!", fontSize = 60.sp)
            Text("Duration: $pickedMinutes min $pickedSeconds sec")
            if (isWeighted) {
                Text("Weight: $weight kg")
            }
        }
    } else if (timerStarted) {
        Timer(
            modifier = modifier, viewModel = timerViewModel, onFinished = {
                finished = true
                logDuration(
                    60 * pickedMinutes + pickedSeconds,
                    if (isWeighted) {
                        weight.toInt()
                    } else {
                        null
                    },
                    activity,
                    workoutPosition
                )
                afterFinish()
            },
            skippable = true
        )
    } else {
        DurationPicking(
            modifier = modifier,
            {
                timerStarted = true
                timerViewModel.start(60 * pickedMinutes + pickedSeconds)
            },
            pickedSeconds,
            pickedMinutes,
            { pickedSeconds = it },
            { pickedMinutes = it },
            isWeighted,
            onWeightChange = { text = it })
    }
}

@Composable
fun DurationPicking(
    modifier: Modifier,
    onCountdownStart: () -> Unit,
    pickedSeconds: Int,
    pickedMinutes: Int,
    onSecondsChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    isWeighted: Boolean,
    onWeightChange: (String) -> Unit
) {
    val setResult = if (!CurrentWorkout.useLastWorkout) {
        CurrentWorkout.getPrevSetResultsOfCurrentExercise()
    } else {
        CurrentWorkout.getPrevSetResultsOfCurrentPosition()
    }

    Column(modifier = modifier) {
        Row {
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text("min", modifier = Modifier.align(Alignment.CenterHorizontally))
                NumberStepper(
                    pickedMinutes,
                    IntRange(0, 59),
                    onChange = onMinutesChange,
                    cycling = true
                )
            }
            Column {
                Text("sec", modifier = Modifier.align(Alignment.CenterHorizontally))

                NumberStepper(
                    pickedSeconds,
                    IntRange(0, 59),
                    onChange = onSecondsChange,
                    cycling = true
                )
            }
        }
        if (isWeighted) {
            TextField(
                value = setResult.addedWeight.toString(),
                onValueChange = onWeightChange,
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
        Spacer(modifier = Modifier.weight(0.5f))
        Button(
            onClick = onCountdownStart,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Start", fontSize = 30.sp) }


        val prevResults = CurrentWorkout.getPrevResultsInWorkout()
        if (!prevResults.isEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(prevResults, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

private fun logDuration(duration: Int, weight: Int?, activity: Activity?, workoutPosition: Int) {
    if (weight != null) {
        CurrentWorkout.logWeightedDuration(duration, weight, activity, workoutPosition)
    } else {
        CurrentWorkout.logDuration(duration, activity, workoutPosition)
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

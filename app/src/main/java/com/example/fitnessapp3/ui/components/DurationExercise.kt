package com.example.fitnessapp3.ui.components

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp3.SetResults.SetResult
import com.example.fitnessapp3.data.CurrentWorkout
import com.example.fitnessapp3.data.Exercise

@Composable
fun DurationExerciseMainContent(
    modifier: Modifier,
    workoutPosition: Int,
    afterFinish: () -> Unit
) {
    val activity = LocalActivity.current
    val setResult: SetResult? = CurrentWorkout.getPositionPrevSetResult(workoutPosition)

    var pickedSeconds by rememberSaveable { mutableIntStateOf(setResult?.repNr?.mod(60) ?: 30) }
    var pickedMinutes by rememberSaveable { mutableIntStateOf(setResult?.repNr?.div(60) ?: 0) }


    val isWeighted =
        (CurrentWorkout.getWorkoutComponentAtPosition(workoutPosition) as Exercise).isWeighted
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
        val setResult = CurrentWorkout.getSetResultForPosition(workoutPosition)
        val minutes = setResult.repNr/60
        val seconds = setResult.repNr.mod(60)

        var durationText = "Duration: "
        if (minutes > 0) {
            durationText += "$minutes min"
        }
        if (seconds > 0 || minutes == 0) {
            if (minutes > 0) {
                durationText += " "
            }
            durationText += "$seconds sec"
        }
        Column {
            Text("Finished!", fontSize = 60.sp)
            Text(durationText)
            if (isWeighted) {
                Text("Weight: ${setResult.addedWeight} kg")
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
            onWeightChange = { text = it }, workoutPosition = workoutPosition
        )
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
    onWeightChange: (String) -> Unit,
    workoutPosition: Int
) {

    val setResult: SetResult? = CurrentWorkout.getPositionPrevSetResult(workoutPosition)

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
                value = setResult?.addedWeight?.toString() ?: "0",
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


        val prevResults = CurrentWorkout.getPrevResultsInWorkoutForPosition(workoutPosition)
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

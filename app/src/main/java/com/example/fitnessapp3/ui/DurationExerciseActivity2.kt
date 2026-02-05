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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val activity = LocalActivity.current

    var name = CurrentWorkout.getWorkoutComponentName()
    if (name == null) {
        name = "Exercise name"
    }

    var pickedSeconds by rememberSaveable { mutableIntStateOf(0) }
    var pickedMinutes by rememberSaveable { mutableIntStateOf(0) }

    var finished by rememberSaveable { mutableStateOf(false) }

    val setResult = if (!CurrentWorkout.useLastWorkout) {
        CurrentWorkout.getPrevSetResultsOfCurrentExercise()
    } else {
        CurrentWorkout.getPrevSetResultsOfCurrentPosition()
    }
    if (setResult != null && setResult.isDuration) {
        val repNr = setResult.repNr
        pickedSeconds = repNr.mod(60)
        pickedMinutes = repNr / 60
    }

    var timerStarted by rememberSaveable { mutableStateOf(false) }

    val workoutPosition = CurrentWorkout.getWorkoutPosition()

    val progress = (workoutPosition+1).toFloat() / CurrentWorkout.getWorkoutLength()


    val isWeighted = (CurrentWorkout.getCurrentWorkoutComponent() as Exercise).isWeighted
    var text by remember { mutableStateOf(setResult.addedWeight.toString()) }
    val weight = try {
        text.toFloat()
    } catch (_: NumberFormatException) {
        0f
    }

    val timerViewModel: TimerViewModel = viewModel()
    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ExerciseHeader(name, progress)
                if (timerStarted && !finished) {
                    Column{
                        Spacer(modifier = Modifier.weight(0.3f))
                    Timer(modifier=Modifier.weight(1.0f),viewModel = timerViewModel, onFinished = {
                        finished = true
                        logDuration(
                            60 * pickedMinutes + pickedSeconds,
                            if (isWeighted){weight.toInt()}else{null},
                            activity,
                            workoutPosition
                        )

                        goToNextActivity(activity)
                    },
                        skippable = true)
                        Spacer(modifier = Modifier.weight(1f))
                    }

                } else {
                    DurationPicking({
                        timerStarted = true
                        timerViewModel.start(60 * pickedMinutes + pickedSeconds)
                    }, pickedSeconds, pickedMinutes, { pickedSeconds = it }, { pickedMinutes = it }, isWeighted,
                        onWeightChange = {text=it})
                }
            }
        }
    }
}

@Composable
fun DurationPicking(
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

    Column {
        Spacer(modifier = Modifier.weight(0.3f))
        Row {

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text("min", modifier = Modifier.align(Alignment.CenterHorizontally))
                Stepper(pickedMinutes, IntRange(0, 59), onChange = onMinutesChange, cycling = true)
            }
            Column {
                Text("sec", modifier = Modifier.align(Alignment.CenterHorizontally))

                Stepper(pickedSeconds, IntRange(0, 59), onChange = onSecondsChange, cycling = true)
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


        Spacer(modifier = Modifier.weight(1f))
        val prevResults = CurrentWorkout.getPrevResultsInWorkout()
        if (!prevResults.isEmpty()) {
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

private fun goToNext(activity: Activity?) {
    if (CurrentWorkout.hasCurrentExercise()) {
        goToNextActivity(activity)
    } else {
        CurrentWorkout.finishWorkout(activity)
        goToNextActivity(activity)
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

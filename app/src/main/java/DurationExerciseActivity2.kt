package com.example.fitnessapp3

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp3.timer.TimerViewModel
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme

class DurationExerciseActivity2: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ActivityContent()
        }
    }
}

@Composable
private fun ActivityContent() {
    val activity= LocalActivity.current

    var name = CurrentWorkout.getWorkoutComponentName()
    if(name==null){
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
    if (setResult != null &&setResult.isDuration) {
        val repNr = setResult.repNr
        pickedSeconds = repNr.mod(60)
        pickedMinutes = repNr/60
    }

    var timerStarted by rememberSaveable { mutableStateOf(false) }

    val workoutPosition = CurrentWorkout.getWorkoutPosition()

    val progress = workoutPosition.toFloat() / CurrentWorkout.getWorkoutLength()

    val timerViewModel: TimerViewModel = viewModel()
    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(top = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LinearProgressIndicator(progress = {progress})
                Text(name, fontSize = 40.sp)
                Text(CurrentWorkout.getSetString(), fontSize = 10.sp)
                if(timerStarted && !finished){
                    /*Column{
                    CountdownTimer()
                    Button(onClick = {
                        finished= true
                        logDuration(60*pickedMinutes+pickedSeconds,null, activity, workoutPosition)

                        goToNext(activity)
                    }, modifier = Modifier.align(Alignment.CenterHorizontally)) {Text("Skip") }
                    }*/
                    TimerRunning(viewModel=timerViewModel, onFinished = {
                        finished= true
                        logDuration(60*pickedMinutes+pickedSeconds,null, activity, workoutPosition)

                        goToNext(activity)
                    })

                }else{
                DurationPicking({timerStarted=true
                                timerViewModel.start(60*pickedMinutes+pickedSeconds)
                                }, pickedSeconds, pickedMinutes, {pickedSeconds=it}, {pickedMinutes=it})
            }}
        }
    }
}
@Composable
fun DurationPicking(onCountdownStart: ()->Unit, pickedSeconds: Int, pickedMinutes: Int, onSecondsChange: (Int) -> Unit, onMinutesChange: (Int) -> Unit){
    Column{
        Row {

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text("min", modifier = Modifier.align(Alignment.CenterHorizontally))
                Stepper(pickedMinutes, IntRange(0, 59), onChange = onMinutesChange, cycling = true)
            }
            Column{
                Text("sec", modifier = Modifier.align(Alignment.CenterHorizontally))

                Stepper(pickedSeconds, IntRange(0, 59), onChange = onSecondsChange, cycling = true)
            }}

        Button(onClick = onCountdownStart, modifier = Modifier.align(Alignment.CenterHorizontally)){Text("Start")}

        val prevResults = CurrentWorkout.getPrevResultsInWorkout()

        if (!prevResults.isEmpty()) {
            Text(prevResults, modifier = Modifier.padding(top=10.dp))
        }
    }
}

@Composable
fun TimerRunning(
    viewModel: TimerViewModel= viewModel(),
    onFinished: () -> Unit
){

    val remaining by viewModel.remaining.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.finished.collect {
        onFinished()
        }
    }

    Column{
    Text("Time left: $remaining")
Button(onClick = {viewModel.skipTimer()}) {Text("Skip") }
    }
}

@Composable
fun Stepper(
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    cycling: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = {
                if (value > range.first) onChange(value - 1)
            else if (cycling){
                onChange(range.last)
            }}
        ) {
            Text("-")
        }

        Text(
            text = value.toString().padStart(2, '0'),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { if (value < range.last) onChange(value + 1)
            else{
                onChange(range.first)
            }}
        ) {
            Text("+")
        }
    }
}



private fun logDuration(duration: Int, weight: Int?, activity: Activity?, workoutPosition: Int) {
    if (weight!=null) {
        CurrentWorkout.logWeightedDuration(duration, weight, activity, workoutPosition)
    } else {
        CurrentWorkout.logDuration(duration, activity, workoutPosition)
    }
}

private fun goToNext(activity: Activity?){
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

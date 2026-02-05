package com.example.fitnessapp3.ui

import android.app.Activity
import android.content.Intent.getIntent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp3.data.CurrentWorkout
import com.example.fitnessapp3.ui.components.ExerciseHeader
import com.example.fitnessapp3.ui.components.Timer
import com.example.fitnessapp3.ui.components.TimerViewModel
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme
import java.util.Objects

class RestActivity2 : ComponentActivity() {
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

    val name = CurrentWorkout.getWorkoutComponentName()?:"Exercise name"

    val workoutPosition = CurrentWorkout.getWorkoutPosition()

    val progress = (workoutPosition+1).toFloat() / CurrentWorkout.getWorkoutLength()

    val timerViewModel: TimerViewModel = viewModel()

    val intent = activity?.intent
    val millis = intent?.getIntExtra(MainActivity.EXTRA_MESSAGE, 30000)
    timerViewModel.start(millis?.div(1000) ?: 120)

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
                Spacer(Modifier.weight(1.0f))
                Timer(viewModel=timerViewModel, onFinished = {
                    if ("WorkoutActivity" == Objects.requireNonNull(
                            intent?.getStringExtra(
                                MainActivity.EXTRA_RETURN_DEST
                            )
                        )
                    ) {
                        if (millis!=null){
                        CurrentWorkout.logRest(millis, activity, workoutPosition)}
                    }
                    goToNextActivity(activity)}, skippable = true, modifier = Modifier.weight(1f))
                Spacer(Modifier.weight(1.0f))
                }
            }

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

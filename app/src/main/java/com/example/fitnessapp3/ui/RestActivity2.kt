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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Spacer(Modifier.weight(1.0f))
                RestActivityMainContent(
                    modifier = Modifier.weight(4f),
                    afterFinish = { goToNextActivity(activity) })
                Spacer(Modifier.weight(1.0f))
            }
        }
    }
}

@Composable
fun RestActivityMainContent(
    modifier: Modifier,
    workoutPosition: Int = CurrentWorkout.getWorkoutPosition(),
    afterFinish: () -> Unit
) {
    val activity = LocalActivity.current
    var finished by rememberSaveable {
        mutableStateOf(
            CurrentWorkout.positionIsFinished(
                workoutPosition
            )
        )
    }

    val timerViewModel: TimerViewModel = viewModel()
    val intent = activity?.intent
    val millis = intent?.getIntExtra(MainActivity.EXTRA_MESSAGE, 30000)
    timerViewModel.start(millis?.div(1000) ?: 120)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (finished) {
            Column {
                Text("Finished!", fontSize = 60.sp)
                val seconds = millis?.div(1000) ?: 120
                Text("Duration: $seconds sec")
            }
        } else {
            Timer(viewModel = timerViewModel, onFinished = {
                if ("WorkoutActivity" == Objects.requireNonNull(
                        intent?.getStringExtra(
                            MainActivity.EXTRA_RETURN_DEST
                        )
                    )
                ) {
                    if (millis != null) {
                        CurrentWorkout.logRest(millis, activity, workoutPosition)
                    }
                }
                finished = true
                afterFinish()
            }, skippable = true, modifier = Modifier.weight(1f))
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

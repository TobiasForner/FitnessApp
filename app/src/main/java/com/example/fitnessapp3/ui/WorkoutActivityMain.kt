package com.example.fitnessapp3.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnessapp3.MainActivity2
import com.example.fitnessapp3.data.CurrentWorkout
import com.example.fitnessapp3.data.Exercise
import com.example.fitnessapp3.ui.components.DurationExerciseMainContent
import com.example.fitnessapp3.ui.components.ExerciseHeader
import com.example.fitnessapp3.ui.components.RepExerciseMainContent
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme
import kotlinx.coroutines.launch

class WorkoutActivityMain : ComponentActivity() {
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

    // compute first not done position in current workout
    var pos = 0
    while (pos < CurrentWorkout.getWorkoutLength() && CurrentWorkout.positionIsFinished(pos)) {
        pos += 1
    }

    val pagerState =
        rememberPagerState(initialPage = pos, pageCount = {
            CurrentWorkout.getWorkoutLength()
        })

    val workoutPosition by remember { derivedStateOf { pagerState.currentPage } }

    var name by rememberSaveable {
        mutableStateOf(
            CurrentWorkout.getWorkoutComponentAtPosition(
                workoutPosition
            ).name
        )
    }

    var showEndButUnfinishedAlert by remember { mutableStateOf(false) }
    var showWorkoutFinishedAlert by remember { mutableStateOf(false) }

    val animationScope = rememberCoroutineScope()

    val finishWorkout: () -> Unit = {
        CurrentWorkout.finishWorkout(activity)
        activity?.startActivity(
            Intent(
                activity,
                MainActivity2::class.java
            )
        )
        activity?.finish()
    }


    val donePositions =
        rememberSaveable { MutableList(CurrentWorkout.getWorkoutLength()) { false }.toMutableStateList() }
    repeat(CurrentWorkout.getWorkoutLength()) { index ->
        donePositions[index] = CurrentWorkout.positionIsFinished(index)
    }

    val afterFinish: () -> Unit = {
        donePositions[workoutPosition] = true
        if (workoutPosition == CurrentWorkout.getWorkoutLength() - 1) {
            showEndButUnfinishedAlert = false
            if (CurrentWorkout.workoutIsFinished()) {
                finishWorkout()
            } else {
                showEndButUnfinishedAlert = true
            }
        } else {
            animationScope.launch {
                pagerState.animateScrollToPage(
                    workoutPosition + 1
                )
            }
        }
    }
    LaunchedEffect(pagerState) {
        pagerState.animateScrollToPage(
            workoutPosition
        )
    }

    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ExerciseHeader(name, workoutPosition)
                HorizontalPager(state = pagerState) { page ->
                    val comp = CurrentWorkout.getWorkoutComponentAtPosition(page)
                    Column {
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(3f),
                            contentAlignment = Alignment.Center
                        ) {
                            val exercise = comp as Exercise
                            if (exercise.type == Exercise.ExType.DURATION) {
                                DurationExerciseMainContent(
                                    Modifier,
                                    workoutPosition = page,
                                    afterFinish = afterFinish
                                )
                            } else {
                                RepExerciseMainContent(
                                    Modifier,
                                    workoutPosition = page,
                                    afterFinish = afterFinish
                                )
                            }

                        }
                        Spacer(Modifier.weight(2f))
                    }
                }

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }
                        .collect { page ->
                            name = CurrentWorkout.getWorkoutComponentAtPosition(page).name
                            if (CurrentWorkout.workoutIsFinished()) {
                                showWorkoutFinishedAlert = true
                            } else if (workoutPosition == CurrentWorkout.getWorkoutLength() - 1 && CurrentWorkout.positionIsFinished(
                                    workoutPosition
                                )
                            ) {
                                showEndButUnfinishedAlert = true
                            }
                        }
                }

                if (showEndButUnfinishedAlert) {
                    AlertDialog(
                        onDismissRequest = { showEndButUnfinishedAlert = false },
                        title = { Text("Workout not finished") },
                        text = { Text("You have reached the end of the workout, but there are still unfinished Exercises. Would you like to jump to the first unfinished one?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showEndButUnfinishedAlert = false
                                // go to first unfinished exercise position
                                var pos = 0

                                for (i in 0..<CurrentWorkout.getWorkoutLength()) {
                                    val entry = CurrentWorkout.currentWorkout[i]
                                    pos = i
                                    if (entry == null || entry.isEmpty()) {
                                        break
                                    }
                                }
                                animationScope.launch {
                                    pagerState.animateScrollToPage(
                                        pos
                                    )
                                }

                            }) {
                                Text("Jump")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEndButUnfinishedAlert = false }) {
                                Text("Dismiss")
                            }
                        }

                    )
                }

                if (showWorkoutFinishedAlert) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Workout finished") },
                        text = { Text("You have finished your workout!") },
                        confirmButton = {
                            TextButton(onClick = {
                                finishWorkout()
                            }) { Text("Finish") }
                        })

                }

            }
        }
    }
}
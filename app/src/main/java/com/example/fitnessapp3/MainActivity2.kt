package com.example.fitnessapp3.com.example.fitnessapp3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp3.R
import com.example.fitnessapp3.ui.SettingsActivity
import com.example.fitnessapp3.ui.WeightActivity2
import com.example.fitnessapp3.data.CurrentWorkout
import com.example.fitnessapp3.data.WorkoutManager
import com.example.fitnessapp3.data.WorkoutStats
import com.example.fitnessapp3.ui.ActivityTransition
import com.example.fitnessapp3.ui.AddExerciseActivity
import com.example.fitnessapp3.ui.ManageExercisesActivity
import com.example.fitnessapp3.ui.WorkoutEditActivity
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme
import com.example.fitnessapp3.util.Util
import org.json.JSONException
import org.json.JSONObject
import java.util.function.Function

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        setContent {
            ActivityContent()
        }
    }
}

@Composable
private fun ActivityContent() {
    val context = LocalContext.current
    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                IconButton(

                    onClick = {

                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)

                    }, modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_settings_24),
                        contentDescription = "Settings cog",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                }
                ToWeightActivity()

                ToAddExerciseActivity()

                ToManageExercisesActivity()

                WorkoutList()
            }
        }
    }
}

@Composable
fun ToWeightActivity() {
    val context = LocalActivity.current

    Button(onClick = {
        context?.startActivity(Intent(context, WeightActivity2::class.java))
    }, modifier = Modifier.fillMaxWidth()) { Text("Weight") }
}

@Composable
fun ToAddExerciseActivity() {
    val context = LocalActivity.current

    Button(onClick = {
        context?.startActivity(Intent(context, AddExerciseActivity::class.java))
    }, modifier = Modifier.fillMaxWidth()) { Text("Add Exercise") }
}

@Composable
fun ToManageExercisesActivity() {
    val context = LocalActivity.current

    Button(onClick = {
        context?.startActivity(Intent(context, ManageExercisesActivity::class.java))
    }, modifier = Modifier.fillMaxWidth()) { Text("Manage Exercises") }
}

@Composable
fun WorkoutList() {
    val context = LocalContext.current
    val workoutNames = sortedWorkoutNames(context)


    Column() {
        Row {
            Text("Workouts", fontSize = 30.sp)
            IconButton(onClick = {
                val intent = Intent(context, WorkoutEditActivity::class.java)
                context.startActivity(intent)

            }) {
                Icon(
                    painter = painterResource(id = R.drawable.edit_pen_icon),
                    contentDescription = "Settings cog",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            items(workoutNames.size) { item ->

                val workoutName = workoutNames[item]
                val foundExercises = HashSet<String?>()
                val exNames = ArrayList<String?>()
                val w = WorkoutManager.getWorkout(workoutName, context)
                for (i in 0..<w.length) {
                    val comp = w.getComponentAt(i)
                    if (comp.isExercise()) {
                        val exName = comp.getName()
                        if (!foundExercises.contains(exName)) {
                            foundExercises.add(exName)
                            exNames.add(exName)
                        }
                    }
                }

                val overview = java.lang.String.join(", ", exNames)

                Card(
                    onClick = { startWorkout(workoutName, context as Activity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(5.dp)
                ) {
                    Column {
                        Text(
                            workoutNames[item],
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 30.sp
                        )
                        Text(overview)
                    }
                }

            }
        }
    }
}

fun startWorkout(workoutName: String?, activity: Activity) {
    CurrentWorkout.init(workoutName, activity)
    startWorkout(activity)
}

private fun startWorkout(activity: Activity) {
    try {
        val appStatus = JSONObject(

            Util.readFromInternal(
                "app_status.json",
                activity
            )
        )
        appStatus.put("workout_is_in_progress", true)
        Util.writeFileOnInternalStorage(activity, "app_status.json", appStatus.toString())
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    activity.startActivity(ActivityTransition.goToNextActivityInWorkout(activity))
}

fun sortedWorkoutNames(context: Context): MutableList<String>{

    val workoutNames = WorkoutManager.getWorkoutNamesList()
    workoutNames.sortWith(Comparator.naturalOrder())
    val workoutToSortScore: MutableMap<String?, WorkoutStats> = HashMap()

    //load workoutStats to determine sorting
    val workoutStats = Util.readFromInternal("workout_stats.json", context)
    var workoutStatsJSON: JSONObject
    if (workoutStats == null) {
        workoutStatsJSON = JSONObject()
    } else {
        workoutStatsJSON = try {
            JSONObject(workoutStats)
        } catch (_: JSONException) {
            JSONObject()
        }
    }
    for (s in workoutNames) {
        val currWorkoutStats: JSONObject?
        val cWorkoutStats = WorkoutStats(0, "", 999999999, 99999999, 9999999)
        cWorkoutStats.posInSortedNames = workoutNames.indexOf(s)
        if (workoutStatsJSON.has(s)) {
            try {
                currWorkoutStats = workoutStatsJSON.getJSONObject(s)

                if (currWorkoutStats.has("count")) {
                    cWorkoutStats.count = currWorkoutStats.getInt("count")
                }

                if (currWorkoutStats.has("lastCompletion")) {
                    cWorkoutStats.lastCompletedDate = currWorkoutStats.getString("lastCompletion")
                }
                workoutToSortScore.put(s, cWorkoutStats)
            } catch (_: JSONException) {
                workoutToSortScore.put(s, cWorkoutStats)
            }
        } else {
            workoutToSortScore.put(s, cWorkoutStats)
        }
    }
    workoutNames.sortWith(Comparator.comparingInt { s: String? ->
        val stats = workoutToSortScore[s]
        stats?.count ?: 0
    })
    for (s in workoutNames) {
        val cWorkoutStats = checkNotNull(workoutToSortScore.get(s))
        cWorkoutStats.posInSortedCounts = workoutNames.indexOf(s)
    }
    workoutNames.sortWith(Comparator.comparing(Function { s: String? ->
        val stats = workoutToSortScore[s]
        if (stats == null) {
            "ZZZZZZZZZZ"
        } else {
            stats.lastCompletedDate
        }
    }))
    for (s in workoutNames) {
        val cWorkoutStats = checkNotNull(workoutToSortScore[s])
        cWorkoutStats.posInSortedDates = workoutNames.indexOf(s)
    }

    Log.d("MainActivity", "sort scores for workout: $workoutToSortScore")
    workoutNames.sortWith(Comparator { s1: String?, s2: String? ->
        val score1 = workoutToSortScore[s1]
        val score2 = workoutToSortScore[s2]
        if (score1 == null) {
            1
        } else if (score2 == null) {
            -1
        } else {
            score1.compareTo(score2)
        }
    })
    return workoutNames
}
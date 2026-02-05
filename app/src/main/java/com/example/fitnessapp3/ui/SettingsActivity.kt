package com.example.fitnessapp3.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme
import org.json.JSONException
import org.json.JSONObject
import androidx.compose.material3.AlertDialog
import androidx.documentfile.provider.DocumentFile
import com.example.fitnessapp3.R
import com.example.fitnessapp3.com.example.fitnessapp3.MainActivity2
import com.example.fitnessapp3.data.ExerciseManager
import com.example.fitnessapp3.data.WorkoutManager

class SettingsActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val activity = this
        setContent {
            ActivityContent(activity = activity)
        }
    }
}

@Composable
fun ActivityContent(activity: SettingsActivity) {
    val context = LocalContext.current
    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column {
                Button(
                    onClick = {
                        Startup.initWorkoutNamesFile(activity)
                    }, modifier = Modifier.padding(innerPadding)){
                    Text("Initialize Workouts")
                    }

                BackupDirectoryPicker()

                RestoreBackupButton()

                Button(onClick = {context.startActivity(Intent(context, MainActivity2::class.java))}) {
                    Text("Main 2")
                }
            }
        }
    }
}

@Composable
fun BackupDirectoryPicker() {
    val context = LocalContext.current

    val getContentLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        Log.d("MainActivity", "onActivityResult: ${uri.path}")

        try {
            val treeDoc = DocumentFile.fromTreeUri(context, uri) ?: return@rememberLauncherForActivityResult

            val fullBackup = fullJSONData(context)

            // Create or replace the file
            val backupFile =
                treeDoc.findFile("workout_backup.json")
                    ?: treeDoc.createFile("application/json", "workout_backup.json")

            backupFile?.uri?.let { resUri ->
                context.contentResolver.openOutputStream(resUri)?.use { outputStream ->
                    outputStream.write(
                        fullBackup.toString().toByteArray(Charsets.UTF_8)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Button(
        onClick = { getContentLauncher.launch(null) }
    ) {
        Text("Export Backup")
    }
}

@Throws(JSONException::class)
private fun fullJSONData(context: Context): JSONObject {
    val res = JSONObject()
    // store exercises
    val exerciseManager = ExerciseManager(context)
    val exercises = exerciseManager.exercisesJson()
    res.put("exercises", exercises)
    // store workout details
    val workoutsJSON = WorkoutManager.workoutsJSON(context)
    res.put("workouts", workoutsJSON)
    // store logged weight
    val loggedWeight = getPastWeights(context)
    res.put("weight", loggedWeight)
    // store results for last workouts (set strings)
    // TODO
    return res
}

@Composable
fun RestoreBackupButton(
) {
    val context = LocalContext.current

    var showConfirmDialog by remember { mutableStateOf(false) }
    var backupContents by remember { mutableStateOf<String?>(null) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        try {
            val docFile = DocumentFile.fromSingleUri(context, uri) ?: return@rememberLauncherForActivityResult

            val contents = context.contentResolver
                .openInputStream(docFile.uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText().trim() }

            Log.d("MainActivity", "content read: $contents")

            backupContents = contents
            showConfirmDialog = true

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Button(
        onClick = {
            restoreLauncher.launch(arrayOf("application/json"))
        }
    ) {
        Text("Restore Backup")
    }

    if (showConfirmDialog && backupContents != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = stringResource(R.string.confirm_restore)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    restoreBackup(
                        context = context,
                        contents = backupContents!!
                    )              }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun restoreBackup(
    context: Context,
    contents: String
) {
    try {
        val backup = JSONObject(contents)

        val exercises = backup.getJSONArray("exercises")
        ExerciseManager(context)
            .overwriteExerciseDetailsJSON(exercises, context)

        val workouts = backup.getJSONObject("workouts")
        WorkoutManager.overwriteWorkouts(workouts, context)

    } catch (e: JSONException) {
        e.printStackTrace()
    }
}


package com.example.fitnessapp3

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
                Text(CurrentWorkout.getWorkoutComponentName())
                ToWeightActivity()

                ToAddExerciseActivity()

                ToManageExercisesActivity()

                WorkoutList()
            }
        }
    }
}
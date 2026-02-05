package com.example.fitnessapp3.ui.components

import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


@SuppressLint("DefaultLocale")
@Composable
fun Timer(
    modifier: Modifier=Modifier,
    viewModel: TimerViewModel = viewModel(),
    onFinished: () -> Unit,
    skippable: Boolean,
) {
    val remaining by viewModel.remaining.collectAsStateWithLifecycle()
    val remMins = remaining / 60
    val remSecs = remaining.mod(60)
    val timerText = String.format("%02d:%02d", remMins, remSecs)

    LaunchedEffect(Unit) {
        viewModel.finished.collect {
            onFinished()
            if (!viewModel.skipped.value) {
                val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
            }
        }
    }
    Column(modifier = modifier) {
        Text(timerText, fontSize = 60.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        if(skippable){
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            viewModel.skipTimer()
        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                "Skip",
                fontSize = 30.sp
            )
        }}
    }
}
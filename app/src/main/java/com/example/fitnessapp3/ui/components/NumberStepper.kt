package com.example.fitnessapp3.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberStepper(
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    cycling: Boolean,
    buttonFontSize: TextUnit = 20.sp,
    numberFontSize: TextUnit = 30.sp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Button(
            onClick = {
                if (value < range.last) onChange(value + 1)
                else {
                    onChange(range.first)
                }
            }
        ) {
            Text("+", fontSize = buttonFontSize)
        }

        Text(
            text = value.toString().padStart(2, '0'),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center,
            fontSize = numberFontSize
        )

        Button(
            onClick = {
                if (value > range.first) onChange(value - 1)
                else if (cycling) {
                    onChange(range.last)
                }
            }
        ) {
            Text("-", fontSize = buttonFontSize)
        }
    }
}
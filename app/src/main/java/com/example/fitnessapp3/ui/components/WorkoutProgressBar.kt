package com.example.fitnessapp3.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp


@Composable
fun WorkoutProgressBar(
    modifier: Modifier,
    stepCount: Int,
    position: Int,
    done: List<Boolean>,
    completedColor: Color = Color(0xFF4CAF50),
    focusedColor: Color = Color(0xFF2196F3),
    defaultColor: Color = Color.LightGray,
    onStepClick: ((Int) -> Unit)? = null
) {

    @Composable
    fun StepItem(index: Int) {
        val isCompleted = done[index]
        val isCurrent = (index == position)
        val animatedScale by animateFloatAsState(
            targetValue = if (isCurrent) 1.2f else 1f,
            label = "scale"
        )
        val animateColor by animateColorAsState(
            targetValue = when {
                isCompleted -> completedColor
                isCurrent -> focusedColor
                else -> defaultColor
            }, label = "color"
        )

        Box(
            modifier = modifier
                .size(28.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .border(
                    width = if (isCurrent) 3.dp else 1.dp,
                    color = animateColor,
                    shape = RoundedCornerShape(6.dp)
                )
                .background(
                    color = if (isCompleted) animateColor else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(enabled = onStepClick != null) {
                    onStepClick?.invoke(index)

                }
        )

    }

    val listState = rememberLazyListState()

    LaunchedEffect(position) {
        listState.animateScrollToItem(position)
    }
    LazyRow(
        state = listState,
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(stepCount) { index ->
            StepItem(index)
            if (index < stepCount - 1) {
                Box(modifier = Modifier
                    .height(2.dp)
                    .width(24.dp)
                    .background(defaultColor))
            }
        }
    }
}
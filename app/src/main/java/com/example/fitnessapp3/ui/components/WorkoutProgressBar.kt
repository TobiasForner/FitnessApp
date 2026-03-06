package com.example.fitnessapp3.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalWindowInfo
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
        val backgroundColor by animateColorAsState(
            targetValue = when {
                isCompleted -> completedColor
                else -> Color.Transparent
            }, label = "color"
        )
        val borderColor by animateColorAsState(
            targetValue = when {
                isCurrent -> focusedColor
                isCompleted -> completedColor
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
                    color = borderColor,
                    shape = RoundedCornerShape(6.dp)
                )
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(enabled = onStepClick != null) {
                    onStepClick?.invoke(index)

                }
        )

    }

    val listState = rememberLazyListState()

    // box width (28) + line width (24)
    val elementWidth = 52
    val screenWidth = LocalWindowInfo.current.containerDpSize.width

    val elementsFitting = screenWidth / elementWidth
    val scrollPosition =
        minOf(maxOf((2*position - elementsFitting.value + 3).toInt(), 0), 2*stepCount)

    LaunchedEffect(position) {
        listState.animateScrollToItem(scrollPosition)
    }


    LazyRow(
        state = listState,
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // spacer at the start and end, lines between stepCount boxes
        items(2 * stepCount + 1) { index ->
            if (index == 0 || index == 2 * stepCount) {
                Spacer(Modifier.width(10.dp))
            } else {
                // boxes at odd indices
                if (index.mod(2) == 1) {
                    StepItem((index - 1)/2)
                } else {

                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(24.dp)
                                .background(defaultColor)
                        )
                }
            }
        }
    }
}
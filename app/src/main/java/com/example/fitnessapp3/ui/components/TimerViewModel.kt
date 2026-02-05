package com.example.fitnessapp3.ui.components

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val END_TIME = "end_time"
    }

    private val _remaining = MutableStateFlow(0)
    val remaining: StateFlow<Int> = _remaining

    private val _finished = MutableSharedFlow<Unit>()
    val finished: SharedFlow<Unit> = _finished

    private val _skipped = MutableStateFlow(false)
    val skipped: StateFlow<Boolean> = _skipped

    private var job: Job? = null

    fun start(seconds: Int) {
        val endTime = System.currentTimeMillis() + seconds * 1_000L
        savedStateHandle[END_TIME] = endTime
        tick(endTime)
    }

    fun skipTimer() {
        job?.cancel()
        job = null
        _remaining.value = 0
        _skipped.value = true
        savedStateHandle.remove<Long>(END_TIME)

        viewModelScope.launch {
            _finished.emit(Unit)
        }
    }

    private fun tick(endTime: Long) {
        job?.cancel()
        job = viewModelScope.launch {
            while (true) {
                val remainingMs = endTime - System.currentTimeMillis()
                if (remainingMs <= 0) {
                    _remaining.value = 0
                    _finished.emit(Unit)
                    break
                }
                val seconds = ((remainingMs + 999) / 1_000).toInt()
                _remaining.value = seconds

                val delayMs = remainingMs % 1_000
                delay(if (delayMs == 0L) 1_000 else delayMs)
            }
        }
    }
}
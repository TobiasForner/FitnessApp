package com.example.fitnessapp3.timer

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
                _remaining.value = (remainingMs / 1_000).toInt()
                delay(1_000)
            }
        }
    }
}

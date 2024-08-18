package com.example.spor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepCountViewModel : ViewModel() {
    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int>
        get() = _stepCount

    fun setStepCount(count: Int) {
        Log.d("StepCountViewModel", "Adım sayısı güncelleniyor: $count")
        _stepCount.value = count
        // Güncellenen adım sayısını log mesajı olarak kaydet
        _stepCount.value?.let { updatedCount ->
            Log.d("StepCountViewModel", "Adım sayısı güncellendi: $updatedCount")
        }
    }
}
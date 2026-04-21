package com.jnetai.petcare.ui

import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

fun <T> Flow<T>.observe(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
    lifecycleOwner.lifecycleScope.launch {
        collect { observer(it) }
    }
}
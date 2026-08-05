package com.underthemask.android.feature.home

import androidx.lifecycle.ViewModel
import com.underthemask.android.BuildConfig
import com.underthemask.android.core.config.BackendConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    val debugBackendAddress: String? = if (BuildConfig.DEBUG) {
        "Backend: ${BackendConfig.host}:8080"
    } else {
        null
    }
}

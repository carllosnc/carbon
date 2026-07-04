package com.carbon.launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carbon.launcher.data.AppModel
import com.carbon.launcher.data.AppRepository
import com.carbon.launcher.data.DockPref
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LauncherState(
    val apps: List<AppModel> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = "",
) {
    val dockApps: List<AppModel> get() = apps.take(DockPref.MAX_DOCK_APPS)
}

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app)
    private val _state = MutableStateFlow(LauncherState())
    val state: StateFlow<LauncherState> = _state.asStateFlow()

    init { loadApps() }

    fun loadApps() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val start = System.currentTimeMillis()
            val apps = repo.getInstalledApps()
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < 500) delay(500 - elapsed)
            _state.value = _state.value.copy(apps = apps, isLoading = false)
        }
    }

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }
}


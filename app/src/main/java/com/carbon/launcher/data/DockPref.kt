package com.carbon.launcher.data

import android.content.Context
import android.content.SharedPreferences

object DockPref {
    private const val KEY_DOCK_PACKAGES = "dock_packages"
    private const val MAX_DOCK_APPS = 5

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("carbon_dock", Context.MODE_PRIVATE)

    fun isConfigured(context: Context): Boolean =
        prefs(context).contains(KEY_DOCK_PACKAGES)

    fun get(context: Context): List<String> =
        prefs(context)
            .getString(KEY_DOCK_PACKAGES, null)
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.distinct()
            ?.take(MAX_DOCK_APPS)
            .orEmpty()

    fun save(context: Context, packageNames: List<String>) {
        val normalized = packageNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX_DOCK_APPS)

        prefs(context)
            .edit()
            .putString(KEY_DOCK_PACKAGES, normalized.joinToString(","))
            .apply()
    }
}
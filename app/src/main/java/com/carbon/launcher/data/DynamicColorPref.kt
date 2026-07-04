package com.carbon.launcher.data

import android.content.Context
import android.content.SharedPreferences

object DynamicColorPref {
    private const val KEY_DYNAMIC_COLOR = "dynamic_color_enabled"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("carbon_theme", Context.MODE_PRIVATE)

    fun get(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DYNAMIC_COLOR, true)

    fun save(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }
}

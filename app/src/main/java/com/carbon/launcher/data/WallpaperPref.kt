package com.carbon.launcher.data

import android.content.Context
import android.content.SharedPreferences
import com.carbon.launcher.R

object WallpaperPref {
    private const val KEY_DRAWABLE_RES = "launcher_wallpaper_res"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("carbon_wallpaper", Context.MODE_PRIVATE)

    fun save(context: Context, drawableResId: Int) {
        prefs(context).edit().putInt(KEY_DRAWABLE_RES, drawableResId).apply()
    }

    fun get(context: Context): Int =
        prefs(context).getInt(KEY_DRAWABLE_RES, R.drawable.bg2)
}

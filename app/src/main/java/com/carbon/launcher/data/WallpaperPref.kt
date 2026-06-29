package com.carbon.launcher.data

import android.content.Context
import android.content.SharedPreferences
import com.carbon.launcher.R

object WallpaperPref {
    private const val KEY_DRAWABLE_RES = "launcher_wallpaper_res"
    private val defaultDrawableRes = R.drawable.aurora_drift
    private val availableWallpapers = setOf(
        R.drawable.aurora_drift,
        R.drawable.violet_haze,
        R.drawable.prism_bloom,
        R.drawable.obsidian_wave,
        R.drawable.liquid_sunrise,
        R.drawable.ember_mist,
        R.drawable.lunar_current,
        R.drawable.neon_fog,
        R.drawable.cosmic_silk,
        R.drawable.electric_orbit,
        R.drawable.shadow_glass,
        R.drawable.plasma_field,
        R.drawable.midnight_flux,
    )

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("carbon_wallpaper", Context.MODE_PRIVATE)

    fun save(context: Context, drawableResId: Int) {
        prefs(context).edit().putInt(KEY_DRAWABLE_RES, drawableResId).apply()
    }

    fun get(context: Context): Int {
        val savedWallpaper = prefs(context).getInt(KEY_DRAWABLE_RES, defaultDrawableRes)
        return if (savedWallpaper in availableWallpapers) savedWallpaper else defaultDrawableRes
    }
}
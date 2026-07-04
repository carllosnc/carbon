package com.carbon.launcher.data

import android.content.Context
import android.content.SharedPreferences

object CategoryOrderPref {
    private const val KEY_CATEGORY_ORDER = "category_order"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("carbon_categories", Context.MODE_PRIVATE)

    fun get(context: Context): List<AppCategory> {
        val saved = prefs(context)
            .getString(KEY_CATEGORY_ORDER, null)
            ?.split(',')
            ?.mapNotNull { name -> AppCategory.entries.firstOrNull { it.name == name.trim() } }
            .orEmpty()

        val missing = AppCategory.entries
            .filter { it !in saved }
            .sortedBy { it.order }

        return (saved + missing).distinct()
    }

    fun orderMap(context: Context): Map<AppCategory, Int> =
        get(context).withIndex().associate { (index, category) -> category to index }

    fun save(context: Context, categories: List<AppCategory>) {
        val normalized = (categories + AppCategory.entries.sortedBy { it.order })
            .distinct()
            .joinToString(",") { it.name }

        prefs(context)
            .edit()
            .putString(KEY_CATEGORY_ORDER, normalized)
            .apply()
    }
}

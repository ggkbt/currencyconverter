package com.ggkbt.currencyconverter.preferences

import android.app.Activity
import android.content.Context
import android.util.Log
import java.util.*

object LocaleManager {

    private const val LANGUAGE_KEY = "language_key"

    fun loadLocale(context: Context) {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(LANGUAGE_KEY, Locale.getDefault().language)
        language?.let {
            Log.d("TAG_LANG", "language: $it")
        }
        language?.let { setNewLocale(context, it) }
    }

    private fun setNewLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun setLocale(activity: Activity, languageCode: String) {
        if (getCurrentLanguage(activity) != languageCode) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val resources = activity.resources
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)

            // Сохранение выбранного языка
            val prefs = activity.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
            prefs.putString(LANGUAGE_KEY, languageCode)
            prefs.apply()

            // Перезапуск активити для применения изменений
            activity.recreate()
        }
    }

    fun getCurrentLanguage(context: Context): String {
        return context.getSharedPreferences("Settings", Context.MODE_PRIVATE).getString(LANGUAGE_KEY, Locale.getDefault().language) ?: Locale.getDefault().language
    }
}

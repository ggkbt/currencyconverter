package com.ggkbt.currencyconverter.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ggkbt.currencyconverter.enums.Currency
import com.google.gson.Gson

class Prefs(context: Context) {
    private val PREFERENCES_KEY = "preferences"
    private val APP_PREF_API_OPT_KEY = "api_option"
    private val BASE_CURRENCY_KEY = "base_currency"
    private val TARGET_CURRENCY_KEY = "target_currency"
    private val THEME_KEY = "theme"

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)


    var isCbr: Boolean
        get() = preferences.getBoolean(APP_PREF_API_OPT_KEY, true)
        set(value) = preferences.edit().putBoolean(APP_PREF_API_OPT_KEY, value).apply()

    var systemTheme: Boolean
        get() = preferences.getBoolean(THEME_KEY, true)
        set(value) = preferences.edit().putBoolean(THEME_KEY, value).apply()

    var baseCurrency: Currency
        get() {
            val json = preferences.getString(BASE_CURRENCY_KEY, null)
            return if (json != null) {
                val gson = Gson()
                gson.fromJson(json, Currency::class.java)
            } else {
                Currency.EUR
            }
        }
        set(value) {
            val json = Gson().toJson(value)
            preferences.edit().putString(BASE_CURRENCY_KEY, json).apply()
        }


    var targetCurrency: Currency
        get() {
            val json = preferences.getString(TARGET_CURRENCY_KEY, null)
            return if (json != null) {
                val gson = Gson()
                gson.fromJson(json, Currency::class.java)
            } else {
                Currency.RUB
            }
        }
        set(value) {
            val json = Gson().toJson(value)
            preferences.edit().putString(TARGET_CURRENCY_KEY, json).apply()
        }
}
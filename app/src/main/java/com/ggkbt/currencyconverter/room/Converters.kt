package com.ggkbt.currencyconverter.room

import androidx.room.TypeConverter
import com.ggkbt.currencyconverter.model.cbr.CbrCurrency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal

class Converters {
    @TypeConverter
    fun fromStringToMap(value: String): Map<String, BigDecimal> {
        val mapType = object : TypeToken<Map<String, BigDecimal>>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMapToString(map: Map<String, BigDecimal>): String {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromCurrencyMap(value: Map<String, CbrCurrency>?): String {
        if (value == null) {
            return ""
        }
        val gson = Gson()
        val type = object : TypeToken<Map<String, CbrCurrency>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCurrencyMap(value: String): Map<String, CbrCurrency>? {
        if (value.isBlank()) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<Map<String, CbrCurrency>>() {}.type
        return gson.fromJson(value, type)
    }
}

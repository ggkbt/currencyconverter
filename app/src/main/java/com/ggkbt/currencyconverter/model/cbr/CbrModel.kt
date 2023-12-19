package com.ggkbt.currencyconverter.model.cbr

import androidx.room.*
import com.ggkbt.currencyconverter.room.Converters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "cbr")
@TypeConverters(Converters::class)
data class CbrModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cbr_id")
    val id: Int,
    @SerializedName("Date") val date: String,
    @SerializedName("PreviousDate") val previousDate: String,
    @SerializedName("PreviousURL") val previousURL: String,
    @SerializedName("Timestamp") val timestamp: String,
    @SerializedName("Valute") val valute: Map<String, CbrCurrency>
)

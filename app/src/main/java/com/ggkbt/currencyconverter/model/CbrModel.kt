package com.ggkbt.currencyconverter

import android.os.Parcelable
import androidx.room.*
import com.ggkbt.currencyconverter.room.Converters
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

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

@Parcelize
data class CbrCurrency(
    @SerializedName("ID") val id: String,
    @SerializedName("NumCode") val numCode: String?,
    @SerializedName("CharCode") val charCode: String,
    @SerializedName("Nominal") val nominal: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("Value") val value: BigDecimal,
    @SerializedName("Previous") val previousValue: BigDecimal
) : Parcelable

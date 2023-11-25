package com.ggkbt.currencyconverter.model

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import com.ggkbt.currencyconverter.room.Converters
import java.math.BigDecimal

@Parcelize
@Entity(tableName = "exchange_rates")
@TypeConverters(Converters::class)
data class XrModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "exchange_rates_id")
    val id: Int,
    @ColumnInfo(name = "result")
    @SerializedName("result") val result: Boolean,
    @ColumnInfo(name = "documentation")
    @SerializedName("documentation") val documentation: String,
    @ColumnInfo(name = "terms_of_use")
    @SerializedName("terms_of_use") val termsOfUse: String,
    @ColumnInfo(name = "time_last_update_unix")
    @SerializedName("time_last_update_unix") val timeLastUpdateUnix: Long,
    @ColumnInfo(name = "time_last_update_utc")
    @SerializedName("time_last_update_utc") val timeLastUpdateUtc: String,
    @ColumnInfo(name = "time_next_update_unix")
    @SerializedName("time_next_update_unix") val timeNextUpdateUnix: Long,
    @ColumnInfo(name = "time_next_update_utc")
    @SerializedName("time_next_update_utc") val timeNextUpdateUtc: String,
    @ColumnInfo(name = "base_code")
    @SerializedName("base_code") val baseCode: String,
    @ColumnInfo(name = "conversion_rates")
    @SerializedName("conversion_rates") val rates: Map<String, BigDecimal>
) : Parcelable
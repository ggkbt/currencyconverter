package com.ggkbt.currencyconverter.model.cbr

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

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
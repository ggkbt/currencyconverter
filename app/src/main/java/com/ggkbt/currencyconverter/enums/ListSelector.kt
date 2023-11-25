package com.ggkbt.currencyconverter.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ListSelector : Parcelable {
    SOURCE,
    TARGET,
    UNDEFINED
}

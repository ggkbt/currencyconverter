package com.ggkbt.currencyconverter.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "favorite_pairs", primaryKeys = ["from", "to"])
data class FavoritePair(
    @ColumnInfo(name = "from")
    val from: String,
    @ColumnInfo(name = "to")
    val to: String
)

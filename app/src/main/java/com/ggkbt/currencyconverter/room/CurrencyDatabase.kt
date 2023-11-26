package com.ggkbt.currencyconverter.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ggkbt.currencyconverter.model.CbrModel
import com.ggkbt.currencyconverter.model.FavoritePair
import com.ggkbt.currencyconverter.model.XrModel
import com.ggkbt.currencyconverter.room.dao.CbrModelDao
import com.ggkbt.currencyconverter.room.dao.PairsDao
import com.ggkbt.currencyconverter.room.dao.XrModelDao

@Database(entities = [CbrModel::class, XrModel::class, FavoritePair::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CurrencyDatabase : RoomDatabase() {
    abstract fun cbrApiModelDao(): CbrModelDao
    abstract fun xrModelDao(): XrModelDao
    abstract fun pairsDao(): PairsDao
}
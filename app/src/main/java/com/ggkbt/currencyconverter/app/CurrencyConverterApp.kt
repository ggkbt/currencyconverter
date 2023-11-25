package com.ggkbt.currencyconverter.app

import android.app.Application
import androidx.room.Room.databaseBuilder
import com.ggkbt.currencyconverter.di.ServiceLocator
import com.ggkbt.currencyconverter.preferences.Prefs
import com.ggkbt.currencyconverter.room.CurrencyDatabase


class CurrencyConverterApp : Application() {
    companion object {
        lateinit var instance: CurrencyConverterApp
            private set
    }

    lateinit var database: CurrencyDatabase
        private set
    lateinit var prefs: Prefs
        private set


    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        database = databaseBuilder(this, CurrencyDatabase::class.java, "database")
            .build()
        prefs = Prefs(context = instance.applicationContext)
        ServiceLocator.getInstance()

    }
}
package com.ggkbt.currencyconverter.repository

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.ggkbt.currencyconverter.CbrModel
import com.ggkbt.currencyconverter.app.CurrencyConverterApp
import com.ggkbt.currencyconverter.JSONCbrApi
import com.ggkbt.currencyconverter.room.CurrencyDatabase
import com.ggkbt.currencyconverter.room.dao.CbrModelDao


class CbrRepository {
    private val cbrModelDao: CbrModelDao
    private val db: CurrencyDatabase = CurrencyConverterApp.instance.database

    init {
        cbrModelDao = db.cbrApiModelDao()
    }

    fun loadFromApi(): Single<CbrModel> {
        return JSONCbrApi.getInstance().getAllData()
            .observeOn(AndroidSchedulers.mainThread())
    }
    fun writeToDb(cbrModel: CbrModel): Completable {
        return cbrModelDao.clearTable()
            .andThen(cbrModelDao.insert(cbrModel))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadFromDb(): Single<CbrModel?> {
        return cbrModelDao.getRecent()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}
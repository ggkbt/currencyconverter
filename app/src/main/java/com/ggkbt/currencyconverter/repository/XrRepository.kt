package com.ggkbt.currencyconverter.repository

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.ggkbt.currencyconverter.app.CurrencyConverterApp
import com.ggkbt.currencyconverter.model.XrModel
import com.ggkbt.currencyconverter.network.XrApi
import com.ggkbt.currencyconverter.room.CurrencyDatabase
import com.ggkbt.currencyconverter.room.dao.XrModelDao

class XrRepository {
    private val xrModelDao: XrModelDao
    private val db: CurrencyDatabase = CurrencyConverterApp.instance.database

    init {
        xrModelDao = db.xrModelDao()
    }

    fun loadFromApi(base: String): Single<XrModel> {
        return XrApi.getInstance().getRateByBase(base = base)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadFromDbByBase(base: String): Single<XrModel?> {
        return xrModelDao.getByBase(base = base)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun writeToDb(xrModel: XrModel): Completable {
        return xrModelDao.deleteByBaseCode(xrModel.baseCode)
            .andThen(xrModelDao.insert(xrModel))
            .subscribeOn(Schedulers.io())
    }
}
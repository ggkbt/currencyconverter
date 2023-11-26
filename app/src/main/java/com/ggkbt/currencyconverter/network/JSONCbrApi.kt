package com.ggkbt.currencyconverter.network

import com.ggkbt.currencyconverter.model.CbrModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface JSONCbrApi {
    @GET("/daily_json.js")
    fun getAllData(): Single<CbrModel>

    companion object {
        private var requestClient: JSONCbrApi? = null
        fun getInstance(): JSONCbrApi {
            if (requestClient == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://cbr-xml-daily.ru")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())).build()
                requestClient = retrofit.create(JSONCbrApi::class.java)
            }
            return requestClient!!
        }
    }
}
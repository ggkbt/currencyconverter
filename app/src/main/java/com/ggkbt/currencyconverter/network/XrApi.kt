package com.ggkbt.currencyconverter.network

import com.ggkbt.currencyconverter.model.XrModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface XrApi {
    @GET("/v6/{access_key}/latest/{baseCode}")
    fun getRateByBase(
        @Path("access_key") apiKey: String = "b2e47f771e3d767eff1e463f",
        @Path("baseCode") base: String = "EUR"
    ): Single<XrModel>

    companion object {
        var requestClient: XrApi? = null
        fun getInstance(): XrApi {
            if (requestClient == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://v6.exchangerate-api.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .build()
                requestClient = retrofit.create(XrApi::class.java)
            }
            return requestClient!!
        }
    }
}
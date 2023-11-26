package com.ggkbt.currencyconverter.di

import com.ggkbt.currencyconverter.repository.CbrRepository
import com.ggkbt.currencyconverter.repository.PairsRepository
import com.ggkbt.currencyconverter.repository.XrRepository

class ServiceLocator private constructor() {
    val cbrRepository: CbrRepository = CbrRepository()
    val xrRepository: XrRepository = XrRepository()
    val pairsRepository: PairsRepository = PairsRepository()

    companion object {
        private var instance: ServiceLocator? = null

        fun getInstance(): ServiceLocator {
            if (instance == null) {
                instance = ServiceLocator()
            }
            return instance!!
        }
    }

}
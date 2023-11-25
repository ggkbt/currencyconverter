package com.ggkbt.currencyconverter.di

import com.ggkbt.currencyconverter.repository.CbrRepository
import com.ggkbt.currencyconverter.repository.XrRepository

class ServiceLocator private constructor() {
    val cbrRepository: CbrRepository
    val xrRepository: XrRepository

    init {
        cbrRepository = CbrRepository()
        xrRepository = XrRepository()
    }

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
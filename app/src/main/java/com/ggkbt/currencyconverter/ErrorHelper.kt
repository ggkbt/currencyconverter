package com.ggkbt.currencyconverter

import android.content.Context
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ErrorHelper(private val context: Context) {

    fun getFetchingError(e: Throwable): String? {
        if (isInternetMissingException(e)) return context.getString(R.string.error_no_internet_connection)
        if (e is SocketTimeoutException) return context.getString(R.string.error_timeout)
        if (e is HttpException) {
            return e.response()?.let { getErrorForCode(it.code()) }
        }
        return context.getString(R.string.error_app)
    }


    private fun isInternetMissingException(e: Throwable): Boolean {
        return e is UnknownHostException || e is ConnectException || e is SSLException
    }

    private fun getErrorForCode(code: Int): String? {
        if (code == 401 || code == 403) return context.getString(R.string.error_session)
        if (code == 500) return null
        if (code > 500) return context.getString(R.string.error_internal_server)
        return null

    }


}
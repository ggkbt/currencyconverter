package com.ggkbt.currencyconverter.viewmodel

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ggkbt.currencyconverter.app.CurrencyConverterApp
import com.ggkbt.currencyconverter.di.ServiceLocator
import com.ggkbt.currencyconverter.enums.Currency
import com.ggkbt.currencyconverter.model.FavoritePair
import com.ggkbt.currencyconverter.model.XrModel
import com.ggkbt.currencyconverter.model.cbr.CbrCurrency
import com.ggkbt.currencyconverter.model.cbr.CbrModel
import io.reactivex.rxkotlin.subscribeBy
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
class CurrencyViewModel : ViewModel() {
    private val cbrCurrenciesData: MutableLiveData<List<CbrCurrency>> by lazy {
        MutableLiveData<List<CbrCurrency>>().also {
            loadDbApiCbrData()
        }
    }
    private val xrCurrenciesData: MutableLiveData<Map<String, BigDecimal>> by lazy {
        MutableLiveData<Map<String, BigDecimal>>().also {
            loadDbApiXrData()
        }
    }
    private val _favoritePairs = MutableLiveData<List<FavoritePair>>()
    private val favoritePairs: LiveData<List<FavoritePair>>
        get() = _favoritePairs

    val prefs = CurrencyConverterApp.instance.prefs
    val isCbrLiveData = MutableLiveData(prefs.isCbr)

    private val isUpdated = MutableLiveData<Boolean>()

    init {
        loadPairs()
    }

    var oldSourceValue: String? = null
    var oldTargetValue: String? = null

    private val error = MutableLiveData<Pair<Throwable?, Boolean?>?>()

    var oneTargetVal: BigDecimal? = null
    var valueInTarget: BigDecimal? = null

    var baseCurrency: Currency = prefs.baseCurrency
        get() {
            return prefs.baseCurrency
        }
        set(value) {
            field = value
            prefs.baseCurrency = value
        }
    var targetCurrency: Currency = prefs.targetCurrency
        get() {
            return prefs.targetCurrency
        }
        set(value) {
            if (prefs.isCbr) loadDbApiCbrData() else loadDbApiXrData()
            field = value
            prefs.targetCurrency = value
        }

    fun getCbrData(): LiveData<List<CbrCurrency>> {
        return cbrCurrenciesData
    }

    fun getXrData(): LiveData<Map<String, BigDecimal>> {
        return xrCurrenciesData
    }

    fun getFilteredFavoritePairs(): LiveData<List<FavoritePair>> {
        val result = MediatorLiveData<List<FavoritePair>>()

        result.addSource(favoritePairs) { pairs ->
            Log.d("TAG123", "getFilteredFavoritePairs favoritePairs")
            Log.d("TAG_PAIRS", "viewModel getFilteredFavoritePairs pairs: $pairs")
            if (prefs.isCbr) {
                result.value = combineLatestCbrData(cbrCurrenciesData.value, pairs)
            } else {
                result.value = pairs
            }
        }

        result.addSource(isUpdated) {
            Log.d("TAG123", "getFilteredFavoritePairs isUpdated")
            if (prefs.isCbr) {
                result.value = combineLatestCbrData(cbrCurrenciesData.value, favoritePairs.value)
            } else {
                result.value = favoritePairs.value
            }
        }

        if (prefs.isCbr) {
            result.addSource(cbrCurrenciesData) { cbrData ->
                Log.d("TAG123", "getFilteredFavoritePairs cbrCurrenciesData")
                result.value = combineLatestCbrData(cbrData, favoritePairs.value)
            }
        }

        return result
    }

    fun getError(): LiveData<Pair<Throwable?, Boolean?>?> {
        return error
    }

    fun refreshData() {
        if (prefs.isCbr) loadCbrData() else loadXrData()
    }

    fun updatePrefs(isCbr: Boolean) {
        isCbrLiveData.value = isCbr
        this.prefs.isCbr = isCbr
        if (isCbr) loadDbApiCbrData() else loadDbApiXrData()
    }

    @SuppressLint("CheckResult")
    private fun loadCbrData(noDbData: Boolean? = null) {
        ServiceLocator.getInstance().cbrRepository.loadFromApi()
            .subscribeBy(
                onSuccess = {
                    Log.d("VIEW_MODEL_TAG", "Данные успешно загружены из API Cbr: $it")
                    cbrCurrenciesData.value = it.valute.values.toList()
                    writeToCbrDb(it)
                    error.value = null
                },
                onError = { error.value = Pair(it, noDbData) })
    }

    @SuppressLint("CheckResult")
    private fun loadXrData(noDbData: Boolean? = null) {
        ServiceLocator.getInstance().xrRepository.loadFromApi(baseCurrency.name)
            .subscribeBy(
                onSuccess = {
                    Log.d("VIEW_MODEL_TAG", "Данные успешно загружены из API [Xr]: $it")
                    xrCurrenciesData.value = it.rates
                    writeToXrDb(it)
                    error.value = null
                },
                onError = { error.value = Pair(it, noDbData) })
    }

    @SuppressLint("CheckResult")
    private fun loadDbApiXrData() {
        ServiceLocator.getInstance().xrRepository.loadFromDbByBase(baseCurrency.name)
            .subscribe({ dbData ->
                Log.d("VIEW_MODEL_TAG", "Получены данные из базы данных [Xr]: $dbData")

                if (dbData != null) {
                    if (isDataStale(dbData.timeNextUpdateUnix)) {
                        loadXrData(false)
                    } else {
                        xrCurrenciesData.value = dbData.rates
                    }
                } else {
                    loadXrData(true)
                }
            }, {
                Log.d("VIEW_MODEL_TAG", "Ошибка чтения из базы данных [Xr]: $it")
                loadXrData(true)
            })

    }

    @SuppressLint("CheckResult")
    private fun loadDbApiCbrData() {
        ServiceLocator.getInstance().cbrRepository.loadFromDb()
            .subscribe({ dbData ->
                Log.d("VIEW_MODEL_TAG", "Получены данные из базы данных [Cbr]: $dbData")
                if (dbData != null) {
                    if (isDataStale(dbData.timestamp)) {
                        loadCbrData(false)
                    } else {
                        cbrCurrenciesData.value = dbData.valute.values.toList()
                    }

                }
            }, {
                Log.d("VIEW_MODEL_TAG", "Ошибка чтения из базы данных [Cbr]: $it")
                loadCbrData(true)
            })
    }

    @SuppressLint("CheckResult")
    private fun loadPairs() {
        ServiceLocator.getInstance().pairsRepository.loadFromDb()
            .subscribe ({ pairs ->
                _favoritePairs.postValue(pairs)
                Log.d("VIEW_MODEL_TAG", "Получены данные из базы данных [Cbr]: $pairs")
            }, {
                Log.d("VIEW_MODEL_TAG", "Ошибка чтения из базы данных [Pairs]: $it")
            })
    }

    @SuppressLint("CheckResult")
    fun writePairToDb(pair: FavoritePair) {
        ServiceLocator.getInstance().pairsRepository.writeToDb(pair)
            .subscribe({
                Log.d("VIEW_MODEL_TAG", "Данные в базу данных успешно записаны [Pairs]")
                val updatedList = _favoritePairs.value?.toMutableList() ?: mutableListOf()
                updatedList.add(pair)
                _favoritePairs.postValue(updatedList)
            }, {
                Log.d("VIEW_MODEL_TAG", "Ошибка записи в базу данных [Pairs]: $it")
            })
    }

    @SuppressLint("CheckResult")
    fun deletePairFromDb(pair: FavoritePair) {
        ServiceLocator.getInstance().pairsRepository.deleteFromDb(pair)
            .subscribe({
                Log.d("VIEW_MODEL_TAG", "Данные из БД удалены [Pairs]")
                val updatedList = _favoritePairs.value?.toMutableList() ?: mutableListOf()
                updatedList.remove(pair)
                _favoritePairs.postValue(updatedList)
            }, {
                Log.d("VIEW_MODEL_TAG", "Ошибка удаления из БД [Pairs]: $it")
            })
    }

    @SuppressLint("CheckResult")
    private fun writeToCbrDb(cbrModel: CbrModel) {
        ServiceLocator.getInstance().cbrRepository.writeToDb(cbrModel.copy(id = 0))
            .subscribe({
                Log.d("VIEW_MODEL_TAG", "Данные в базу данных успешно записаны [Cbr]")
            }, {
                Log.d("VIEW_MODEL_TAG", "Ошибка записи в базу данных [Cbr]: $it")
            })

    }

    @SuppressLint("CheckResult")
    private fun writeToXrDb(xrModel: XrModel) {
        ServiceLocator.getInstance().xrRepository.writeToDb(xrModel.copy(id = 0))
            .subscribe({
                Log.d("VIEW_MODEL_TAG", "Данные в базу данных успешно записаны [Xr]")
            }, {
                Log.d("VIEW_MODEL_TAG", "Ошибка записи в базу данных [Xr]: $it")
            })
    }

    private fun combineLatestCbrData(currenciesList: List<CbrCurrency>?, pairs: List<FavoritePair>?): List<FavoritePair> {
        if (currenciesList.isNullOrEmpty() || pairs.isNullOrEmpty()) return emptyList()
        return pairs.filter { pair ->
            currenciesList.any { currency ->
                pair.from.contains(currency.charCode) && pair.to == Currency.RUB.name
            }
        }
    }

    fun nullOldValues() {
        oldSourceValue = null
        oldTargetValue = null
        Log.d("TAG123", "nullOldValues")
    }

    fun updatePair(newBase: Currency = baseCurrency, newTarget: Currency = targetCurrency) {
        baseCurrency = newBase
        targetCurrency = newTarget
        if (prefs.isCbr) loadDbApiCbrData() else loadDbApiXrData()
        isUpdated.value = true
    }

    private fun isDataStale(timestamp: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val oneDayMillis: Long = 86400000
        val lastUpdateMillis: Long = try {
            OffsetDateTime.parse(timestamp).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid timestamp format: $timestamp")
        }
        Log.d("VIEW_MODEL_TAG", "currentTime - lastUpdateMillis: ${currentTime - lastUpdateMillis}")
        Log.d("VIEW_MODEL_TAG", "isDataStale: ${(currentTime - lastUpdateMillis) > oneDayMillis}")
        return (System.currentTimeMillis() - lastUpdateMillis) > oneDayMillis
    }

    private fun isDataStale(nextUpdate: Long): Boolean {
        Log.d("VIEW_MODEL_TAG", "System.currentTimeMillis() / 1000: ${System.currentTimeMillis() / 1000} nextUpdate: $nextUpdate")
        return (System.currentTimeMillis() / 1000) > nextUpdate
    }


}

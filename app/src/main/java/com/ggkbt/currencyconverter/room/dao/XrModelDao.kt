package com.ggkbt.currencyconverter.room.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import com.ggkbt.currencyconverter.model.XrModel

@Dao
interface XrModelDao {
    @Query("SELECT * FROM exchange_rates")
    fun getAll(): Single<List<XrModel?>?>

    @Query("SELECT * FROM exchange_rates WHERE exchange_rates_id = :id")
    fun getById(id: Long): Single<XrModel?>

    @Query("SELECT * FROM exchange_rates WHERE base_code = :base ORDER BY time_last_update_unix DESC LIMIT 1")
    fun getByBase(base: String): Single<XrModel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currencyData: XrModel): Completable

    @Update
    fun update(currencyData: XrModel): Completable

    @Delete
    fun delete(currencyData: XrModel): Single<Int>

    @Query("DELETE FROM exchange_rates WHERE base_code = :baseCode")
    fun deleteByBaseCode(baseCode: String): Completable
}

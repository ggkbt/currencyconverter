package com.ggkbt.currencyconverter.room.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import com.ggkbt.currencyconverter.model.cbr.CbrModel

@Dao
interface CbrModelDao {
    @Query("SELECT * FROM cbr")
    fun getAll(): Single<List<CbrModel?>?>

    @Query("SELECT * FROM cbr WHERE cbr_id = :id")
    fun getById(id: Long): Single<CbrModel?>

    @Query("SELECT * FROM cbr ORDER BY date DESC LIMIT 1")
    fun getRecent(): Single<CbrModel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currencyData: CbrModel): Completable

    @Update
    fun update(currencyData: CbrModel): Completable

    @Delete
    fun delete(currencyData: CbrModel): Single<Int>

    @Query("DELETE FROM cbr")
    fun clearTable(): Completable
}
package com.ggkbt.currencyconverter.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ggkbt.currencyconverter.model.FavoritePair
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface PairsDao {
    @Query("SELECT * FROM favorite_pairs")
    fun getAll(): Single<List<FavoritePair>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(pair: FavoritePair): Completable

    @Update
    fun update(pair: FavoritePair): Completable
    @Delete
    fun delete(pair: FavoritePair): Single<Int>
}
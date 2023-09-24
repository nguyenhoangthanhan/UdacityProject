package com.udacity.asteroidradar.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.Asteroid

@Dao
interface AsteroidDatabaseDao {

    @Insert
    suspend fun insert(asteroidEntity: AsteroidEntity)

    @Update
    suspend fun update(asteroidEntity: AsteroidEntity)

    @Query("SELECT * from asteroid_info_table WHERE id = :id")
    suspend fun get(id: Long): AsteroidEntity?

    @Query("DELETE FROM asteroid_info_table")
    suspend fun clear()

    @Query("SELECT * FROM asteroid_info_table")
    fun getAllAsteroids(): LiveData<List<AsteroidEntity>>

    @Query("SELECT * from asteroid_info_table WHERE id = :id")
    fun getNightWithId(id: Long): LiveData<AsteroidEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroidEntities: AsteroidEntity)

    @Query("SELECT * FROM asteroid_info_table WHERE close_approach_date = :startDate ORDER BY close_approach_date DESC")
    fun getAsteroidsDay(startDate: String): LiveData<List<AsteroidEntity>>

    @Query("SELECT * FROM asteroid_info_table WHERE close_approach_date BETWEEN :startDate AND :endDate ORDER BY close_approach_date ASC")
    fun getAsteroidsInAPeriod(startDate: String, endDate: String): LiveData<List<AsteroidEntity>>
}
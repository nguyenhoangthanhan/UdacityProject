package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidsApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {

    private val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format((Calendar.getInstance().also { it.add(Calendar.DATE, 7) }).time)

    private val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

    val allAsteroids: LiveData<List<Asteroid>> = database.asteroidDatabaseDao.getAllAsteroids().map {it.asDomainModel()}

    val todayAsteroids: LiveData<List<Asteroid>> = database.asteroidDatabaseDao.getAsteroidsDay(startDate).map {
        Log.d("AsteroidRepository_todayAsteroids_startDate",startDate.toString())
        it.asDomainModel()
    }

    val sevenDayAsteroids: LiveData<List<Asteroid>> = database.asteroidDatabaseDao.getAsteroidsInAPeriod(startDate, endDate).map {
        Log.d("AsteroidRepository_sevenDayAsteroids_endDate",endDate.toString())
        Log.d("AsteroidRepository_sevenDayAsteroids_startDate",startDate.toString())
        it.asDomainModel()
    }

    suspend fun refreshAsteroids() {
        Log.d("AsteroidRepository_refreshAsteroids:", "refreshAsteroids")
        withContext(Dispatchers.IO){
            Log.d("AsteroidRepository_refreshAsteroids_Refresh Asteroids", "Success0")
            try{
                Log.d("AsteroidRepository_refreshAsteroids_Refresh Asteroids", "Success1")
                val getJSONAsteroids:String = AsteroidsApi.retrofitService.getAsteroids(Constants.API_KEY)
                val jSONAsteroidsDeferred = parseAsteroidsJsonResult(JSONObject(getJSONAsteroids))
                database.asteroidDatabaseDao.insertAll(*jSONAsteroidsDeferred.asDatabaseModel())
                Log.d("AsteroidRepository_refreshAsteroids_Refresh Asteroids", "Success2")
            } catch (e: Exception) {
                Log.e("AsteroidRepository_refreshAsteroids_Failed!", e.message.toString())
            }
        }
    }

}
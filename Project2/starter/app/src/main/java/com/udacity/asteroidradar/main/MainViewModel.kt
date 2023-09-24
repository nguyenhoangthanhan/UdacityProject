package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidsApi
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AsteroidDatabase.getInstance(application)
    private val asteroidRepository = AsteroidRepository(database)

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _navigateToSelectedAsteroid= MutableLiveData<Asteroid?>()
    val navigateToSelectedProperty: MutableLiveData<Asteroid?>
        get() = _navigateToSelectedAsteroid

    private var _filterAsteroid = MutableLiveData(FilterAsteroid.TODAY)

    val asteroidList: LiveData<List<Asteroid>> = _filterAsteroid.switchMap{
        Log.d("MainViewModel__filterAsteroid",it.toString())
        when (it) {
            FilterAsteroid.WEEK -> asteroidRepository.sevenDayAsteroids
            FilterAsteroid.TODAY -> asteroidRepository.todayAsteroids
            else -> asteroidRepository.allAsteroids
        }
    }

    /**
     * Call getAsteroidList() on init so we can display status immediately.
     */
    init {
        viewModelScope.launch {
            Log.d("MainViewModel","init")
            asteroidRepository.refreshAsteroids()
            refreshPictureOfDay()
        }
    }

    private fun getAsteroidListFakeData() {
        viewModelScope.launch {
            var asteroids: ArrayList<Asteroid> = ArrayList()
            asteroids.add(
                Asteroid(
                1 ,
                "code name demo 3",
                "Date demo 3",
                1.3,
                2.3,
                3.3,
                4.3,
                true
                )
            )
            asteroids.add(Asteroid(
                2 ,
                "code name demo 4",
                "Date demo 4",
                1.4,
                2.4,
                3.4,
                4.4,
                false
            ))
            asteroids.add(Asteroid(
                3 ,
                "code name demo 5",
                "Date demo 5",
                1.5,
                2.5,
                3.5,
                4.5,
                false
            ))
            asteroids.add(Asteroid(
                4 ,
                "code name demo 6",
                "Date demo 6",
                1.6,
                2.6,
                3.6,
                4.6,
                true
            ))
            asteroids.add(Asteroid(
                5 ,
                "code name demo 7",
                "Date demo 7",
                1.7,
                2.7,
                3.7,
                4.7,
                false
            ))
            asteroids.add(Asteroid(
                6 ,
                "code name demo 8",
                "Date demo 8",
                1.8,
                2.8,
                3.8,
                4.8,
                true
            ))
        }
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }

    fun onChangeFilter(filter: FilterAsteroid) {
        _filterAsteroid.postValue(filter)
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }

    private suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            try {
                _pictureOfDay.postValue(
                    AsteroidsApi.retrofitService.getPictureOfDay()
                )
            } catch (err: Exception) {
                Log.e("refreshPictureOfDay", err.printStackTrace().toString())
            }
        }
    }
}

enum class FilterAsteroid {
    TODAY, WEEK, ALL
}
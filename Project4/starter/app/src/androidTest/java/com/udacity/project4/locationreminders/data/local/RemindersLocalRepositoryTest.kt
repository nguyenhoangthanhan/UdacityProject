package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun initDbAndRepository() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
            remindersDatabase.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun clearSetup() {
        remindersDatabase.close()
    }

    @Test
    fun saveAReminderAndGetFromDB_getReminderById() = runBlocking {
        val reminder = ReminderDTO("title 1", "description 1", "location 1", 11.1, 22.2)
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id)

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertEquals(result.data.id, reminder.id)
        assertEquals(result.data.title, reminder.title)
        assertEquals(result.data.description, reminder.description)
        assertEquals(result.data.location, reminder.location)
        assertEquals(result.data.latitude, reminder.latitude)
        assertEquals(result.data.longitude, reminder.longitude)
    }

    @Test
    fun saveAReminderDeleteAllAndGetFromDB_testGetEmptyList()= runBlocking {
        val reminder = ReminderDTO("title 1", "description 1", "location 1", 11.1, 22.2)
        remindersLocalRepository.saveReminder(reminder)

        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertThat(result.data, `is` (emptyList()))
    }

    @Test
    fun saveAReminderDeleteAllAndGetByIdFromDB_testReturnError() = runBlocking {
        val reminder = ReminderDTO("title 1", "description 1", "location 1", 11.1, 22.2)
        remindersLocalRepository.saveReminder(reminder)

        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminder(reminder.id)

        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

}
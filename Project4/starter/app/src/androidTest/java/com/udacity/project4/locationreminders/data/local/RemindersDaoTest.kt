package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun initDb() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun saveAReminderAndGetFromDB_getReminderById() = runTest {
        val reminder = ReminderDTO("title 1", "description 1", "location 1", 11.1, 22.2)

        remindersDatabase.reminderDao().saveReminder(reminder)

        val result = remindersDatabase.reminderDao().getReminderById(reminder.id)

        assertThat(result as ReminderDTO, notNullValue())
        assertEquals(result.id, reminder.id)
        assertEquals(result.title, reminder.title)
        assertEquals(result.description, reminder.description)
        assertEquals(result.location, reminder.location)
        assertEquals(result.latitude, reminder.latitude)
        assertEquals(result.longitude, reminder.longitude)
    }

    @Test
    fun add4RemindersAndGetFromDB_getAllRemindersFromDb() = runTest {
        val reminder1 = ReminderDTO("title 1", "description 1", "location 1", 11.1, 21.2)
        val reminder2 = ReminderDTO("title 2", "description 2", "location 2", 12.1, 22.2)
        val reminder3 = ReminderDTO("title 3", "description 3", "location 3", 13.1, 23.2)
        val reminder4 = ReminderDTO("title 4", "description 4", "location 4", 14.1, 24.2)

        remindersDatabase.reminderDao().saveReminder(reminder1)
        remindersDatabase.reminderDao().saveReminder(reminder2)
        remindersDatabase.reminderDao().saveReminder(reminder3)
        remindersDatabase.reminderDao().saveReminder(reminder4)

        val remindersList = remindersDatabase.reminderDao().getReminders()

        assertThat(remindersList, `is`(notNullValue()))
    }

    @Test
    fun add4RemindersAndDeleteToDB_deleteAllReminders() = runTest {
        val reminder1 = ReminderDTO("title 1", "description 1", "location 1", 11.1, 21.2)
        val reminder2 = ReminderDTO("title 2", "description 2", "location 2", 12.1, 22.2)
        val reminder3 = ReminderDTO("title 3", "description 3", "location 3", 13.1, 23.2)
        val reminder4 = ReminderDTO("title 4", "description 4", "location 4", 14.1, 24.2)

        remindersDatabase.reminderDao().saveReminder(reminder1)
        remindersDatabase.reminderDao().saveReminder(reminder2)
        remindersDatabase.reminderDao().saveReminder(reminder3)
        remindersDatabase.reminderDao().saveReminder(reminder4)

        remindersDatabase.reminderDao().deleteAllReminders()

        val remindersList = remindersDatabase.reminderDao().getReminders()

        assertThat(remindersList, `is`(emptyList()))
    }
}
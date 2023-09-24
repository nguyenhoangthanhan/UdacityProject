package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.rule.MainCoroutineRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_testReturnError() = runTest {
        fakeDataSource.setReturnError(true)
        fakeDataSource.saveReminder(
            ReminderDTO(
                "title 1",
                "description 1",
                "location 1",
                11.1,
                22.2
            )
        )
        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.value, CoreMatchers.`is`("Reminders not found")
        )
    }

    @Test
    fun loadReminders_testRemainderListNotEmpty() = runTest  {
        val reminder = ReminderDTO("My Store", "Pick Stuff", "Abuja", 6.454202, 7.599545)

        fakeDataSource.saveReminder(reminder)
        remindersListViewModel.loadReminders()

        assertEquals(remindersListViewModel.remindersList.value?.isEmpty(), false)
    }

    @Test
    fun loadReminders_testShowLoading() = runTest {
        mainCoroutineRule.pauseDispatcher()

        val reminder = ReminderDTO("My Store", "Pick Stuff", "Abuja", 6.454202, 7.599545)

        fakeDataSource.saveReminder(reminder)
        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(remindersListViewModel.showLoading.value, CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()

        MatcherAssert.assertThat(remindersListViewModel.showLoading.value, CoreMatchers.`is`(false))

    }

    @Test
    fun loadReminders_testSnackBarValue() {
        mainCoroutineRule.pauseDispatcher()

        fakeDataSource.setReturnError(true)

        remindersListViewModel.loadReminders()

        mainCoroutineRule.resumeDispatcher()

        assertThat(remindersListViewModel.showSnackBar.value).isEqualTo("Reminders not found")
    }


}
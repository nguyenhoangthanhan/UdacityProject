package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.rule.MainCoroutineRule

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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun validateEnteredData_testEmptyTitleAndUpdateSnackBar() {
        val reminder = ReminderDataItem(
            "",
            "description 1",
            "location 1",
            11.1,
            22.2)

        assertThat(saveReminderViewModel.validateEnteredData(reminder)).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.value).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun validateEnteredData_testEmptyLocationAndUpdateSnackBar() {
        val reminder = ReminderDataItem(
            "title 1",
            "description 1",
            "",
            11.1,
            22.2)

        assertThat(saveReminderViewModel.validateEnteredData(reminder)).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.value).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun saveReminders_testShowLoading() = runTest {

        val reminder = ReminderDataItem(
            "title 1",
            "description 1",
            "location 1",
            11.1,
            22.2)

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)

        MatcherAssert.assertThat(saveReminderViewModel.showLoading.value, CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.value, CoreMatchers.`is`(false))

    }


}
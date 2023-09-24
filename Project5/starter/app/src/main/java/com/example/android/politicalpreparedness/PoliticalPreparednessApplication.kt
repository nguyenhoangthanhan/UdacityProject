package com.example.android.politicalpreparedness

import android.app.Application
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.election.ElectionsViewModel
import com.example.android.politicalpreparedness.election.VoterInfoViewModel
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.ElectionEntity
import com.example.android.politicalpreparedness.repository.PoliticalPreparednessRepository
import com.example.android.politicalpreparedness.representative.RepresentativeViewModel
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class PoliticalPreparednessApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val module = module {
            viewModel { (electionEntity: ElectionEntity) ->
                VoterInfoViewModel(get())
            }
            viewModel { ElectionsViewModel(get()) }
            viewModel { RepresentativeViewModel(get()) }
            single { ElectionDatabase.getInstance(this@PoliticalPreparednessApplication).electionDao as ElectionDao }
            single { CivicsApi.retrofitService as CivicsApiService }
            single { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) as DateFormat }
            single {
                PoliticalPreparednessRepository(
                ) as PoliticalPreparednessRepository
            }
        }
        startKoin {
            androidContext(this@PoliticalPreparednessApplication)
            modules(listOf(module))
        }
    }
}
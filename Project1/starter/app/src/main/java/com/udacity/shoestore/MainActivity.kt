package com.udacity.shoestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.udacity.shoestore.databinding.ActivityMainBinding
import com.udacity.shoestore.viewmodels.ShoeStoreViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var shoeStoreViewModel: ShoeStoreViewModel

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        Timber.plant(Timber.DebugTree())

        shoeStoreViewModel = ViewModelProvider(this)[ShoeStoreViewModel::class.java]
        shoeStoreViewModel.getShoesData()

        setSupportActionBar(binding.toolbar);
    }
}

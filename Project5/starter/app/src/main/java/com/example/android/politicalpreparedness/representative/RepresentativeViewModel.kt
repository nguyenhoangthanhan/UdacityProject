package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.repository.PoliticalPreparednessRepository
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class RepresentativeViewModel(
    private val politicalPreparednessRepository: PoliticalPreparednessRepository,
): ViewModel() {

    private val TAG = "RepresentativeViewModel"

    //TODO: Establish live data for representatives and address

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */
    private val _representatives2: MutableLiveData<List<Representative>> = MutableLiveData()
    val representatives2: LiveData<List<Representative>>
        get() = _representatives2

    private val _address = MutableLiveData<Address?>()
    val address: LiveData<Address?>
        get() = _address

    val dataLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val message: SingleLiveEvent<Int> = SingleLiveEvent()
    val messageString = SingleLiveEvent<String>()

    fun searchForRepresentatives(address: Address?) {
        _representatives2.value = listOf()
        if(address == null) {
            message.value = R.string.representative_failed_parse_location
            return
        }
        _address.value = address
        searchRepresentativesIfFormValid(address)
    }

    private fun searchRepresentativesIfFormValid(address: Address) {
        if (address.line1.isBlank()) {
            message.value = R.string.error_missing_first_line_address
            return
        }
        if (address.city.isBlank()) {
            message.value = R.string.error_missing_city
            return
        }
        if (address.state.isBlank()) {
            message.value = R.string.error_missing_state
            return
        }
        if (address.zip.isBlank()) {
            message.value = R.string.error_missing_zip
            return
        }
        search(address)
    }

    //TODO: Create function to fetch representatives from API from a provided address
    private fun search(address: Address) {
        dataLoading.value = true
        viewModelScope.launch {
            _representatives2.value = politicalPreparednessRepository.searchRepresentatives(address)

            Log.d(TAG, "#_representatives2.value = " + _representatives2.value.toString())
            dataLoading.value = false
        }
    }

    fun setState(state: String?) {
        _address.value?.state = state.orEmpty()
    }

    //TODO: Create function get address from geo location

    //TODO: Create function to get address from individual fields

}

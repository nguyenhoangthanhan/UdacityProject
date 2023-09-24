package com.udacity.shoestore.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.shoestore.models.Shoe
import com.udacity.shoestore.repository.ShoeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShoeStoreViewModel : ViewModel() {

    private var userRepository = ShoeRepository()
    private var _shoes: MutableLiveData<ArrayList<Shoe>?> = MutableLiveData()
    val shoes: LiveData<ArrayList<Shoe>?>
        get() = _shoes

    private var _newShoe: Shoe? = null
    val newShoe: Shoe?
        get() = _newShoe

    init {

    }

    fun getShoesData(){
        viewModelScope.launch {
            var result : ArrayList<Shoe>? = null
            withContext(Dispatchers.IO){
                result = userRepository.getShoes()
            }
            _shoes.value = result
        }
    }

    fun shoeSizeString():String{
        return _newShoe?.size.toString()
    }

    fun addShoeToList(shoe: Shoe){
        _shoes.value?.add(shoe)
    }
}
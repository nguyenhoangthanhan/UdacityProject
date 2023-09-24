package com.udacity.shoestore.repository

import android.content.SharedPreferences
import com.udacity.shoestore.models.Shoe


class ShoeRepository {

    fun getShoes(): ArrayList<Shoe> {
        val listShoe =  ArrayList<Shoe>()

        listShoe.add(Shoe("Shoe Black", 29.5, "Company A", "Shoe Black description"))
        listShoe.add(Shoe("Shoe Green", 48.5, "Company G", "Shoe Green description"))
//        listShoe.add(Shoe("Shoe Blue", 33.5, "Company B", "Shoe Blue description"))

        return listShoe
    }

//    private val sharedPreferences: SharedPreferences =
//        context.getSharedPreferences(STORE_FILE_NAME, Context.MODE_PRIVATE)
//
//    private val editor = sharedPreferences.edit()
//
//    fun <T> setList(key: String?, list: List<T>?) {
//        val gson = Gson()
//        val json: String = gson.toJson(list)
//        set(key, json)
//    }
//
//    operator fun set(key: String?, value: String?) {
//        editor.putString(key, value)
//        editor.commit()
//    }

}
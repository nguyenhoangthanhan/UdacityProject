package com.udacity.shoestore.utils

import android.widget.EditText
import androidx.databinding.InverseMethod

object Converter {
    @InverseMethod("stringToLong")
    @JvmStatic fun longToString(
        view: EditText,
        value: Long
    ): String {
        // Converts long to String.
        return value.toString()
    }

    @JvmStatic fun stringToLong(
        view: EditText,
        value: String
    ): Long {
        // Converts String to long.
        if (value.isNotBlank() && value.isNotEmpty()){
            return value.toLong()
        }
        return 0
    }
}
package com.example.passwordmanagerv1

import android.util.Log
import com.example.passwordmanagerv1.utils.DATAFILE_NAME
import java.io.File

/**
 * Singleton that every activity class uses to manipulate data
 */
object Manager {
    private const val TAG = "Manager Model"
    private lateinit var datafile: File


    fun createNewDataFile(passwordInput: String): Boolean {
        Log.i(TAG, "New password set up")
        return true
        datafile.writeText("{}")
    }

    /**
     * checks if data file exists
     */
    fun checkDataFile(): Boolean {
        datafile = File(DATAFILE_NAME)
        return datafile.isFile
    }
}
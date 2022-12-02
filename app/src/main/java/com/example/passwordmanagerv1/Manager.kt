package com.example.passwordmanagerv1

import android.content.Context
import android.util.Log
import com.example.passwordmanagerv1.utils.DATAFILE_NAME
import java.io.File

/**
 * Singleton that every activity class uses to manipulate data
 */
object Manager {

    private const val TAG = "Manager Model"
    private lateinit var datafile: File
    private lateinit var applicationContext: Context
    private lateinit var applicationFilePath: File

    fun setApplicationContext(context: Context) {
        applicationContext = context
        applicationFilePath = applicationContext.filesDir
    }

    fun createNewDataFile(passwordInput: String): Boolean {
        Log.i(TAG, "New password set up")
        datafile = File(applicationFilePath, DATAFILE_NAME)
        datafile.writeText(applicationContext.resources.getString(R.string.sampleAccountJson))
        return true
    }

    /**
     * checks if data file exists
     */
    fun checkDataFile(): Boolean {
        datafile = File(applicationFilePath, DATAFILE_NAME)
        return datafile.exists()
    }
}
package com.example.passwordmanagerv1

import android.content.Context
import android.util.Log
import com.example.passwordmanagerv1.utils.DATAFILE_NAME
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Singleton that every activity class uses to manipulate data
 */
object Manager {

    private const val TAG = "debug Manager singleton"
    private lateinit var datafile: File
    private lateinit var applicationContext: Context
    private lateinit var applicationFilePath: File
    private lateinit var accountList: MutableList<Account>

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

    fun checkDataFile(): Boolean {
        datafile = File(applicationFilePath, DATAFILE_NAME)
        Log.i(TAG, "datafile exists: ${datafile.isFile}")
        return datafile.exists()
    }

    fun loadData(): Boolean {
        try {
            val lines = datafile.readLines()
            accountList = mutableListOf()
            for (line in lines) {
                accountList.add(Json.decodeFromString(line))
            }
        } catch (err: Exception) {
            Log.e(TAG, err.message.toString())
            return false
        }
        return true
    }

    fun saveData(): Boolean {
        try {
            var saveString = ""
            for (account in accountList) {
                saveString += Json.encodeToString(account) + "\n"
                datafile.writeText(saveString)
            }
        } catch (err: Exception) {
            Log.e(TAG, err.message.toString())
            return false
        }
        return true
    }

    fun getAccount(requestedAccountName: String): Account? {
        return accountList.find { acc -> acc.accountName == requestedAccountName }
    }

    fun ifAccountNameExists(name: String): Boolean {
        return accountList.any { acc -> acc.accountName == name }
    }

    fun createAccount(initialName: String): Account {
        val newAccount = Account(accountName=initialName,
            "",
            "",
            "",
            "",
            listOf(),
            mapOf(),
        )
        accountList.add(newAccount)
        saveData()
        return newAccount
    }
}
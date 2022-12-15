package com.example.passwordmanagerv1

import android.content.Context
import android.hardware.biometrics.BiometricManager.Strings
import android.util.Log
import com.example.passwordmanagerv1.utils.AccountField
import com.example.passwordmanagerv1.utils.DATAFILE_NAME
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Singleton that every activity class uses to manipulate data
 */
object Manager {

    private const val TAG = "clg:Manager"
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
        val textdata = listOf(
            applicationContext.resources.getString(R.string.sampleAccountJson),
            applicationContext.resources.getString(R.string.sampleAccountJson2),
            applicationContext.resources.getString(R.string.sampleAccountJson3),
            applicationContext.resources.getString(R.string.sampleAccountJson4),
            applicationContext.resources.getString(R.string.sampleAccountJson5),
            applicationContext.resources.getString(R.string.sampleAccountJson6),
            ).joinToString("\n")
        datafile.writeText(textdata)
        return true
    }

    fun checkDataFile(): Boolean {
        datafile = File(applicationFilePath, DATAFILE_NAME)
        Log.i(TAG, "datafile exists: ${datafile.isFile}")
        return datafile.exists()
    }

    fun loadData(debug: Boolean = false): Boolean {
        try {
            if (debug) {
                datafile.delete()
                createNewDataFile("debugging")
            }

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
                Log.i(TAG, saveString)
                datafile.writeText(saveString)
            }
        } catch (err: Exception) {
            Log.e(TAG, err.message.toString())
            return false
        }
        Log.i(TAG, "Data saved")
        return true
    }

    fun getAccount(requestedAccountName: String): Account? {
        return accountList.find { acc -> acc.accountName == requestedAccountName }
    }

    fun ifAccountNameExists(name: String): Boolean {
        return accountList.any { acc -> acc.accountName == name }
    }

    fun createAccount(initialName: String): Boolean {
        val newAccount = Account(accountName=initialName,
            "",
            "",
            "",
            "",
            mutableListOf(),
            mapOf(),
        )
        accountList.add(newAccount)
        saveData()
        return true
    }

    fun getAllAccountNames(): List<String> {
        return accountList.map { account -> account.accountName }
    }

    fun getAccountFieldValue(accountName: String, accountField: AccountField): String {
        val account = getAccount(accountName) ?: return ""
        return when (accountField) {
            AccountField.accountName -> account.accountName
            AccountField.email -> account.email
            AccountField.username -> account.username
            AccountField.phone -> account.phone
            AccountField.password -> account.password
            else -> {
                Log.e(TAG, "Getting $accountField not implemented")
                ""
            }
        }
    }

    fun editStringFieldValue(
        accountName: String,
        accountFieldToEdit: AccountField,
        newValue: String
    ) : Boolean {
        val account = getAccount(accountName) ?: return false
        when (accountFieldToEdit) {
            AccountField.accountName -> account.accountName = newValue
            AccountField.email -> account.email = newValue
            AccountField.username -> account.username = newValue
            AccountField.phone -> account.phone = newValue
            AccountField.password -> account.password = newValue
            else -> {
                Log.e(TAG, "Editing $accountFieldToEdit not implemented")
            }
        }
        return saveData()
    }

    fun addToLinkedAccounts(accountNameToEdit: String, requestedAccountName: String) : Boolean {
        // validation
        if (accountNameToEdit == requestedAccountName) return false // cannot link to itself
        getAccount(requestedAccountName) ?: return false
        val accountToEdit = getAccount(accountNameToEdit)!!
        if (requestedAccountName in accountToEdit.linkedAccounts) return false
        // edit
        accountToEdit.linkedAccounts.add(requestedAccountName)
        saveData()
        return true
    }

    fun getLinkedAccounts(accountInContext: String): List<String> {
        return getAccount(accountInContext)!!.linkedAccounts
    }

    fun removeFromLinkedAccounts(accountNameToEdit: String, requestedAccountName: String) : Boolean {
        // validation
        getAccount(requestedAccountName) ?: return false
        val accountToEdit = getAccount(accountNameToEdit)!!
        if (requestedAccountName !in accountToEdit.linkedAccounts) return false
        // edit
        accountToEdit.linkedAccounts.remove(requestedAccountName)
        saveData()
        return true
    }
}
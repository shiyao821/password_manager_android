package com.example.passwordmanagerv1

import android.content.Context
import android.util.Log
import com.example.passwordmanagerv1.utils.*
import com.macasaet.fernet.Key
import com.macasaet.fernet.StringValidator
import com.macasaet.fernet.Token
import com.macasaet.fernet.TokenValidationException
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.Duration
import java.time.Instant
import java.time.temporal.TemporalAmount
import java.util.Base64.getUrlEncoder
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Singleton that every activity class uses to manipulate data
 */
object Manager {

    private const val TAG = "clg:Manager"
    private lateinit var datafile: File
    private lateinit var applicationContext: Context
    private lateinit var applicationFilePath: File
    private lateinit var masterPassword: String
    private lateinit var accountList: MutableList<Account>

    fun setApplicationContext(context: Context) {
        applicationContext = context
        applicationFilePath = applicationContext.filesDir
    }

    fun createNewDataFile(passwordInput: String): Boolean {
        Log.i(TAG, "New password set up")
        masterPassword = passwordInput
        datafile = File(applicationFilePath, DATAFILE_NAME_AND_EXTENSION)
        val textdata = listOf(
            masterPassword,
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
        datafile = File(applicationFilePath, DATAFILE_NAME_AND_EXTENSION)
        Log.i(TAG, "datafile exists: ${datafile.isFile}")
        return datafile.exists()
    }

    fun loadData(importData: InputStream? = null, inputPassword: String, debug: Boolean = false): Boolean {
        try {
            if (debug && importData == null) {
                datafile.delete()
                Log.i(TAG, "Old datafile deleted")
                createNewDataFile(inputPassword)
            }
            val lines: List<String> = if (importData != null) {
                val dataAsString = importData.readBytes().decodeToString()
                importData.close()
                val decodedString = decryptData(dataAsString, inputPassword)
                decodedString.split("\n")
            } else {
                val lines = datafile.readLines()
                if (debug) { Log.i(TAG, "Datafile first line: ${lines[0]}") }
                if (inputPassword != lines[0]) {
                    throw TokenValidationException("Password mismatch")
                }
                lines.subList(1, lines.size)
            }
            masterPassword = inputPassword // inputPassword is correct at this stage

            accountList = mutableListOf()
            Log.i(TAG, "num lines ${lines.size}")
            for (line in lines) {
                if (line.isNotEmpty()) {
                    accountList.add(Json.decodeFromString(line))
                }
            }
        } catch (_: TokenValidationException) {
            Log.i(TAG, "Invalid Password")
            return false
        } catch (err: Exception) {
            Log.e(TAG, err.toString())
            return false
        }
        return true
    }

    private fun decryptData(encryptedData: String, inputPassword: String): String {
        val token = Token.fromString(encryptedData)
        val encodedKey = deriveKey(inputPassword)
        val fernetKey = Key(encodedKey)
        val validator = object : StringValidator {
            override fun getTimeToLive(): TemporalAmount {
                return Duration.ofSeconds(Instant.MAX.epochSecond)
            }
        }
        return token.validateAndDecrypt(fernetKey, validator)
    }

    private fun deriveKey(inputPassword: String): String? {
        val salt = KEY_SALT.toByteArray()
        val derivedKeyLength = DERIVED_KEY_LENGTH
        val iterations = KEY_ITERATIONS
        val spec = PBEKeySpec(inputPassword.toCharArray(), salt, iterations, derivedKeyLength)
        val key = SecretKeyFactory.getInstance(KEY_ALGORITHM)
            .generateSecret(spec).encoded
        return getUrlEncoder().encodeToString(key)
    }

    fun saveData(): Boolean {
        try {
            var saveString = masterPassword + "\n"
            for (account in accountList) {
                saveString += Json.encodeToString(account) + "\n"
                datafile.writeText(saveString)
            }
        } catch (err: Exception) {
            Log.e(TAG, err.message.toString())
            return false
        }
        Log.i(TAG, "Data saved")
        return true
    }

    fun encryptData(plainData: String): String {
        val key = deriveKey(masterPassword)
        val fernetKey = Key(key)
        val token = Token.generate(fernetKey, plainData)
        return token.serialise() // the Base64url encoded token
    }

    fun exportData(outputStream: OutputStream) : Boolean{
        try {
            saveData()
            var saveString = ""
            for (acc in accountList) {
                saveString += Json.encodeToString(acc) + "\n"
            }
            val encryptedData = encryptData(saveString)
            // write
            outputStream.write(encryptedData.toByteArray())
            outputStream.close()

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

    fun createAccount(initialName: String): Boolean {
        val newAccount = Account(
            accountName = initialName,
            "",
            "",
            "",
            "",
            mutableListOf(),
            mutableMapOf(),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
        accountList.add(newAccount)
        return saveData()
    }

    fun getAllAccountNames(): List<String> {
        return accountList.map { account -> account.accountName }.sorted()
    }

    fun getAccountFieldValue(accountName: String, accountFieldType: AccountFieldType): String {
        val account = getAccount(accountName) ?: return ""
        return when (accountFieldType) {
            AccountFieldType.accountName -> account.accountName
            AccountFieldType.email -> account.email
            AccountFieldType.username -> account.username
            AccountFieldType.phone -> account.phone
            AccountFieldType.password -> account.password
            else -> {
                Log.e(TAG, "Getting $accountFieldType not implemented")
                ""
            }
        }
    }

    fun editStringFieldValue(
        accountName: String,
        accountFieldTypeToEdit: AccountFieldType,
        newValue: String
    ) : Boolean {
        val account = getAccount(accountName) ?: return false
        when (accountFieldTypeToEdit) {
            AccountFieldType.accountName -> account.accountName = newValue
            AccountFieldType.email -> account.email = newValue
            AccountFieldType.username -> account.username = newValue
            AccountFieldType.phone -> account.phone = newValue
            AccountFieldType.password -> account.password = newValue
            else -> {
                Log.e(TAG, "Editing $accountFieldTypeToEdit not implemented")
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
        return saveData()
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
        return saveData()
    }

    fun getMiscValue(accountName: String, miscTitle: String): String? {
        val account = getAccount(accountName) ?: return null
        return account.misc[miscTitle]
    }

    fun editMiscValue(
        accountName: String,
        prevMiscTitle: String,
        newMiscTitle: String,
        miscValue: String
    ) : Boolean {
        val account = getAccount(accountName) ?: return false
        if (newMiscTitle == prevMiscTitle) {
            account.misc[prevMiscTitle] = miscValue
        } else {
            account.misc.remove(prevMiscTitle)
            account.misc[newMiscTitle] = miscValue
        }
        return saveData()
    }

    fun deleteMiscEntry(accountName: String, miscTitle: String) : Boolean{
        val account = getAccount(accountName) ?: return false
        account.misc.remove(miscTitle)
        return saveData()
    }

    fun hasMiscTitle(accountName: String, title: String): Boolean {
        val account = getAccount(accountName)
        return account!!.misc.containsKey(title)
    }

    fun getAccountNamesFilteredByField(accountFieldType: AccountFieldType, value: String): List<String> {
        if (accountFieldType == AccountFieldType.misc) {
            Log.i(TAG, "Filter by Misc field not implemented")
            return listOf("")
        }
        if (accountFieldType == AccountFieldType.linkedAccounts) {
            return accountList.filter { account -> account.linkedAccounts.contains(value) }
                .map { account -> account.accountName}
        }
        return accountList.filter { account ->
            when (accountFieldType) {
                AccountFieldType.username -> account.username
                AccountFieldType.email -> account.email
                AccountFieldType.phone -> account.phone
                AccountFieldType.password -> account.password
                else -> account.accountName
            } == value }.map { account -> account.accountName }
    }

    fun getAllUsernames(): Map<String, Int> {
        return accountList.groupingBy { it.username }.eachCount()
    }

    fun getAllEmails(): Map<String, Int> {
        return accountList.groupingBy { it.email }.eachCount()
    }

    fun getAllPhoneNumbers(): Map<String, Int> {
        return accountList.groupingBy { it.phone }.eachCount()
    }

    fun getAllPasswords(): Map<String, Int> {
        return accountList.groupingBy { it.password }.eachCount()
    }

    fun getAllLinkedAccounts(): Map<String, Int> {
        return accountList.map { it.linkedAccounts }.flatten().groupingBy { it }.eachCount()
    }

    fun deleteAccount(accountName: String) {
        accountList.remove(this.getAccount(accountName))
        // Remove all other accounts with reference to this account in Linked Accounts
        for (account in accountList) {
            if (account.linkedAccounts.contains(accountName)) {
                account.linkedAccounts.remove(accountName)
            }
        }
    }

    fun verifyPassword(input: String): Boolean {
        return masterPassword == input
    }
}
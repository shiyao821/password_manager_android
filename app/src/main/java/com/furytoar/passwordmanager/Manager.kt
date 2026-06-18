package com.furytoar.passwordmanager

import android.content.Context
import android.util.Log
import com.furytoar.passwordmanager.utils.*
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

    // 32-byte random salt for the current (Argon2id) file format. Null until a file
    // is created/loaded in the new format; a fresh salt is generated on the next save,
    // which is how legacy (plaintext / PBKDF2) data transparently migrates forward.
    private var salt: ByteArray? = null

    // Derived Fernet key cache so the expensive Argon2id KDF is not re-run on every save.
    private var cachedKey: String? = null
    private var cachedKeySalt: ByteArray? = null
    private var cachedKeyPassword: String? = null

    // Set by the last loadData() call when the source used the legacy key format, so the
    // UI can alert the user that their imported data has been upgraded.
    var importedLegacyFormat: Boolean = false
        private set

    fun setApplicationContext(context: Context) {
        applicationContext = context
        applicationFilePath = applicationContext.filesDir
    }

    fun createNewDataFile(passwordInput: String): Boolean {
        Log.i(TAG, "New password set up")
        masterPassword = passwordInput
        datafile = File(applicationFilePath, DATAFILE_NAME_AND_EXTENSION)
        // New vault gets a fresh random salt and is written encrypted from the start.
        salt = null
        invalidateKeyCache()
        if (!this::accountList.isInitialized) accountList = mutableListOf()
        accountList.clear()
        val sampleAccountJsons = listOf(
            R.string.sampleAccountJson,
            R.string.sampleAccountJson2,
            R.string.sampleAccountJson3,
            R.string.sampleAccountJson4,
            R.string.sampleAccountJson5,
            R.string.sampleAccountJson6,
        )
        for (resId in sampleAccountJsons) {
            val account: Account = Json.decodeFromString(applicationContext.resources.getString(resId))
            accountList.add(account)
        }
        return saveData()
    }

    fun checkDataFile(): Boolean {
        datafile = File(applicationFilePath, DATAFILE_NAME_AND_EXTENSION)
        Log.i(TAG, "datafile exists: ${datafile.isFile}")
        return datafile.exists()
    }

    /**
     * Decrypts and loads accounts. When [importData] is given, the decoded accounts are merged
     * into the current set (keeping the most recently edited copy of each). Set [wipeExisting]
     * to discard all current accounts first, so the imported file replaces them wholesale.
     */
    fun loadData(
        importData: InputStream? = null,
        inputPassword: String,
        wipeExisting: Boolean = false
    ): Boolean {
        importedLegacyFormat = false
        try {
            val lines: List<String> = if (importData != null) {
                val bytes = importData.readBytes()
                importData.close()
                decryptImportedData(bytes, inputPassword).split("\n")
            } else {
                decodeLocalFile(datafile.readBytes(), inputPassword)
            }

            Log.i(TAG, "Imported file num lines ${lines.size}")
            if (!this::accountList.isInitialized) accountList = mutableListOf()
            Log.i(TAG, "Local account List: ${accountList.size}")
            // For a wholesale import, discard existing accounts now that decryption succeeded.
            if (wipeExisting) accountList.clear()
            // Merge incoming accounts, keeping the most recently edited copy of each.
            val mergeResult = AccountMerger.merge(accountList, lines)
            Log.i(TAG, "num accounts added/updated: ${mergeResult.added}/${mergeResult.updated}")

            masterPassword = inputPassword // set as new masterPassword only after successful decoding

        } catch (_: TokenValidationException) {
            Log.i(TAG, "Invalid Password")
            return false
        } catch (err: Exception) {
            Log.e(TAG, err.toString())
            return false
        }
        return true
    }

    /**
     * Decodes the local data file. Handles both the current encrypted format (magic header
     * present) and the legacy plaintext format (master password on line 0, account JSON
     * lines after). Legacy plaintext is migrated to the encrypted format on the next save.
     */
    private fun decodeLocalFile(bytes: ByteArray, inputPassword: String): List<String> {
        if (VaultCrypto.hasMagicHeader(bytes)) {
            return decryptNewFormat(bytes, inputPassword).split("\n")
        }
        // Legacy plaintext local file.
        val legacyLines = bytes.decodeToString().split("\n")
        if (legacyLines.isEmpty() || inputPassword != legacyLines[0]) {
            throw TokenValidationException("Password mismatch")
        }
        salt = null // force a fresh salt + encrypted rewrite on next save
        invalidateKeyCache()
        return legacyLines.subList(1, legacyLines.size)
    }

    /**
     * Decrypts an imported data file. Handles the current Argon2id format (magic header) and
     * the legacy PBKDF2 Fernet format (raw token, no header). Importing legacy data sets
     * [importedLegacyFormat] and clears the salt so the vault is re-saved in the new format.
     */
    private fun decryptImportedData(bytes: ByteArray, inputPassword: String): String {
        if (VaultCrypto.hasMagicHeader(bytes)) {
            return decryptNewFormat(bytes, inputPassword)
        }
        importedLegacyFormat = true
        salt = null
        invalidateKeyCache()
        return VaultCrypto.decryptLegacyFernet(
            bytes.decodeToString(), VaultCrypto.deriveKeyLegacy(inputPassword)
        )
    }

    private fun decryptNewFormat(bytes: ByteArray, inputPassword: String): String {
        val fileSalt = VaultCrypto.saltOf(bytes)
        val plain = VaultCrypto.decryptNewFormat(bytes, cachedArgonKey(inputPassword, fileSalt))
        salt = fileSalt // reuse this salt for subsequent saves in the same session
        return plain
    }

    /** Returns a (cached) derived Argon2id key for the given password/salt, avoiding re-derivation. */
    private fun cachedArgonKey(inputPassword: String, saltBytes: ByteArray): String {
        val cached = cachedKey
        if (cached != null && cachedKeyPassword == inputPassword &&
            cachedKeySalt?.contentEquals(saltBytes) == true) {
            return cached
        }
        val key = VaultCrypto.deriveKeyArgon2(inputPassword, saltBytes)
        cachedKey = key
        cachedKeyPassword = inputPassword
        cachedKeySalt = saltBytes
        return key
    }

    private fun invalidateKeyCache() {
        cachedKey = null
        cachedKeySalt = null
        cachedKeyPassword = null
    }

    /** Serializes the in-memory accounts into the new encrypted file format. */
    private fun encryptToNewFormat(): ByteArray {
        val currentSalt = salt ?: VaultCrypto.newSalt().also { salt = it }
        val plainData = accountList.joinToString("") { Json.encodeToString(it) + "\n" }
        return VaultCrypto.encrypt(plainData, cachedArgonKey(masterPassword, currentSalt), currentSalt)
    }

    fun saveData(): Boolean {
        try {
            datafile.writeBytes(encryptToNewFormat())
        } catch (err: Exception) {
            Log.e(TAG, err.message.toString())
            return false
        }
        Log.i(TAG, "Data saved")
        return true
    }

    fun exportData(outputStream: OutputStream): Boolean {
        try {
            saveData()
            outputStream.write(encryptToNewFormat())
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
            AccountFieldType.accountName -> {
                val oldName = account.accountName
                account.accountName = newValue
                // Keep linked-account references in other accounts pointing at the new name.
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                for (other in accountList) {
                    val idx = other.linkedAccounts.indexOf(oldName)
                    if (idx >= 0) {
                        other.linkedAccounts[idx] = newValue
                        other.lastEdited = now
                    }
                }
            }
            AccountFieldType.email -> account.email = newValue
            AccountFieldType.username -> account.username = newValue
            AccountFieldType.phone -> account.phone = newValue
            AccountFieldType.password -> account.password = newValue
            else -> {
                Log.e(TAG, "Editing $accountFieldTypeToEdit not implemented")
            }
        }
        account.lastEdited = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return saveData()
    }

    fun addToLinkedAccounts(accountNameToEdit: String, requestedAccountName: String) : Boolean {
        // validation
        if (accountNameToEdit == requestedAccountName) return false // cannot link to itself
        getAccount(requestedAccountName) ?: return false
        val accountToEdit = getAccount(accountNameToEdit) ?: return false
        if (requestedAccountName in accountToEdit.linkedAccounts) return false
        // edit
        accountToEdit.linkedAccounts.add(requestedAccountName)
        accountToEdit.lastEdited = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return saveData()
    }

    fun getLinkedAccounts(accountInContext: String): List<String> {
        return getAccount(accountInContext)?.linkedAccounts ?: emptyList()
    }

    fun removeFromLinkedAccounts(accountNameToEdit: String, requestedAccountName: String) : Boolean {
        // validation
        getAccount(requestedAccountName) ?: return false
        val accountToEdit = getAccount(accountNameToEdit) ?: return false
        if (requestedAccountName !in accountToEdit.linkedAccounts) return false
        // edit
        accountToEdit.linkedAccounts.remove(requestedAccountName)
        accountToEdit.lastEdited = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
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
        account.lastEdited = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return saveData()
    }

    fun deleteMiscEntry(accountName: String, miscTitle: String) : Boolean{
        val account = getAccount(accountName) ?: return false
        account.misc.remove(miscTitle)
        return saveData()
    }

    fun hasMiscTitle(accountName: String, title: String): Boolean {
        return getAccount(accountName)?.misc?.containsKey(title) ?: false
    }

    fun getAccountNamesFilteredByField(accountFieldType: AccountFieldType, value: String): List<String> {
        if (accountFieldType == AccountFieldType.misc) {
            Log.i(TAG, "Filter by Misc field not implemented")
            return emptyList()
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

    fun deleteAccount(accountName: String): Boolean {
        accountList.remove(this.getAccount(accountName))
        // Remove all other accounts with reference to this account in Linked Accounts
        for (account in accountList) {
            if (account.linkedAccounts.contains(accountName)) {
                account.linkedAccounts.remove(accountName)
            }
        }
        return saveData() // persist the deletion
    }

    fun verifyPassword(input: String): Boolean {
        return masterPassword == input
    }

    /**
     * True when the most recent load came from a legacy source (plaintext local file or legacy
     * Fernet import), meaning no current-format salt/key is cached yet. Callers should trigger a
     * [saveData] on a background thread after login to migrate the file and warm the key cache,
     * so later edits never run the Argon2id KDF on the UI thread.
     */
    fun loadedFromLegacyStorage(): Boolean = salt == null
}
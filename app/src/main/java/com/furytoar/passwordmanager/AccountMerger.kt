package com.furytoar.passwordmanager

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Pure, Android-free merge logic for loading/importing accounts: incoming accounts are added,
 * and an existing account is replaced only when the incoming copy is newer (by [Account.lastEdited]).
 * Kept separate from [Manager] so it can be unit-tested on the JVM (see AccountMergerTest).
 */
object AccountMerger {

    data class Result(val added: Int, val updated: Int)

    /** Merges JSON-encoded account [lines] into [into], returning how many were added/updated. */
    fun merge(into: MutableList<Account>, lines: List<String>): Result {
        var added = 0
        var updated = 0
        for (line in lines) {
            if (line.isEmpty()) continue
            val newAccount: Account = Json.decodeFromString(line)
            val localAccount = into.find { it.accountName == newAccount.accountName }
            if (localAccount == null) {
                into.add(newAccount)
                added++
            } else if (newAccount.lastEdited > localAccount.lastEdited) {
                into.remove(localAccount)
                into.add(newAccount)
                updated++
            }
        }
        return Result(added, updated)
    }
}

package com.furytoar.passwordmanager

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountMergerTest {

    private fun account(name: String, lastEdited: String, password: String = "pw") =
        Account(
            accountName = name,
            username = "user",
            email = "e@mail.com",
            phone = "123",
            password = password,
            linkedAccounts = mutableListOf(),
            misc = mutableMapOf(),
            lastEdited = LocalDateTime.parse(lastEdited)
        )

    private fun line(a: Account) = Json.encodeToString(a)

    @Test
    fun merge_addsNewAccountsIntoEmptyList() {
        val into = mutableListOf<Account>()
        val result = AccountMerger.merge(
            into,
            listOf(line(account("a", "2023-01-01T00:00:00")), line(account("b", "2023-01-01T00:00:00")))
        )
        assertEquals(2, result.added)
        assertEquals(0, result.updated)
        assertEquals(2, into.size)
    }

    @Test
    fun merge_updatesWhenIncomingIsNewer() {
        val into = mutableListOf(account("a", "2023-01-01T00:00:00", password = "old"))
        val result = AccountMerger.merge(into, listOf(line(account("a", "2024-01-01T00:00:00", password = "new"))))
        assertEquals(0, result.added)
        assertEquals(1, result.updated)
        assertEquals(1, into.size)
        assertEquals("new", into[0].password)
    }

    @Test
    fun merge_keepsLocalCopyWhenIncomingIsOlder() {
        val into = mutableListOf(account("a", "2024-01-01T00:00:00", password = "current"))
        val result = AccountMerger.merge(into, listOf(line(account("a", "2023-01-01T00:00:00", password = "stale"))))
        assertEquals(0, result.added)
        assertEquals(0, result.updated)
        assertEquals("current", into[0].password)
    }

    @Test
    fun merge_skipsEmptyLines() {
        val into = mutableListOf<Account>()
        val result = AccountMerger.merge(into, listOf("", line(account("a", "2023-01-01T00:00:00")), ""))
        assertEquals(1, result.added)
        assertEquals(1, into.size)
    }
}

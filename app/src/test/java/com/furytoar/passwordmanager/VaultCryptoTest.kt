package com.furytoar.passwordmanager

import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultCryptoTest {

    // Fixed salt (0x00..0x1f) so derived keys are reproducible across runs.
    private val salt = ByteArray(32) { it.toByte() }

    @Test
    fun newFormat_roundTrips() {
        val key = VaultCrypto.deriveKeyArgon2("correct horse", salt)
        val plain = "{\"accountName\":\"a\"}\n{\"accountName\":\"b\"}\n"
        val bytes = VaultCrypto.encrypt(plain, key, salt)

        assertTrue(VaultCrypto.hasMagicHeader(bytes))
        assertArrayEquals(salt, VaultCrypto.saltOf(bytes))
        assertEquals(plain, VaultCrypto.decryptNewFormat(bytes, key))
    }

    @Test
    fun newFormat_wrongPasswordFailsToDecrypt() {
        val bytes = VaultCrypto.encrypt("secret\n", VaultCrypto.deriveKeyArgon2("right", salt), salt)
        val wrongKey = VaultCrypto.deriveKeyArgon2("wrong", salt)
        assertThrows(Exception::class.java) {
            VaultCrypto.decryptNewFormat(bytes, wrongKey)
        }
    }

    @Test
    fun legacyFernet_roundTrips() {
        // Produce a legacy raw-Fernet token the way older versions did, then decode it.
        val legacyKey = VaultCrypto.deriveKeyLegacy("pw")
        val token = Token.generate(Key(legacyKey), "legacy data\n").serialise()
        assertEquals(
            "legacy data\n",
            VaultCrypto.decryptLegacyFernet(token, VaultCrypto.deriveKeyLegacy("pw"))
        )
    }

    @Test
    fun hasMagicHeader_distinguishesNewFromLegacy() {
        val newFormat = VaultCrypto.encrypt("x", VaultCrypto.deriveKeyArgon2("p", salt), salt)
        assertTrue(VaultCrypto.hasMagicHeader(newFormat))

        val legacyToken = Token.generate(Key(VaultCrypto.deriveKeyLegacy("p")), "x").serialise()
        assertFalse(VaultCrypto.hasMagicHeader(legacyToken.toByteArray()))
    }

    @Test
    fun argon2_isDeterministicAndPinnedToParams() {
        val key = VaultCrypto.deriveKeyArgon2("password", salt)
        assertEquals(key, VaultCrypto.deriveKeyArgon2("password", salt))
        assertNotEquals(key, VaultCrypto.deriveKeyArgon2("different", salt))

        // Known-answer for password="password" + salt=0x00..0x1f. A change to the Argon2id
        // parameters would break interoperability with the companion Python tool; this pins them.
        // (Worth cross-checking this value once against the Python tool's derive_key output.)
        assertEquals("dO06LZznhrz_cDNupma5rK7kz8LOgmsoEztwtheYPVI=", key)
    }
}

package com.furytoar.passwordmanager

import com.furytoar.passwordmanager.utils.ARGON2_ITERATIONS
import com.furytoar.passwordmanager.utils.ARGON2_KEY_LENGTH
import com.furytoar.passwordmanager.utils.ARGON2_MEMORY_KB
import com.furytoar.passwordmanager.utils.ARGON2_PARALLELISM
import com.furytoar.passwordmanager.utils.FILE_MAGIC
import com.furytoar.passwordmanager.utils.LEGACY_DERIVED_KEY_LENGTH
import com.furytoar.passwordmanager.utils.LEGACY_KEY_ALGORITHM
import com.furytoar.passwordmanager.utils.LEGACY_KEY_ITERATIONS
import com.furytoar.passwordmanager.utils.LEGACY_KEY_SALT
import com.furytoar.passwordmanager.utils.SALT_LENGTH
import com.macasaet.fernet.Key
import com.macasaet.fernet.StringValidator
import com.macasaet.fernet.Token
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.time.temporal.TemporalAmount
import java.util.Base64.getUrlEncoder
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Pure, Android-free cryptography for the vault file format. Keeping this free of `Context`,
 * `Log`, resources and disk access makes it unit-testable on the JVM (see VaultCryptoTest).
 *
 * File format: [4-byte magic][32-byte random salt][Fernet ciphertext]. Key derivation is
 * Argon2id; legacy files use raw Fernet tokens (no header) with a PBKDF2 key.
 */
object VaultCrypto {

    fun newSalt(): ByteArray = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }

    fun hasMagicHeader(bytes: ByteArray): Boolean {
        if (bytes.size < FILE_MAGIC.size) return false
        for (i in FILE_MAGIC.indices) if (bytes[i] != FILE_MAGIC[i]) return false
        return true
    }

    /** Extracts the salt from a new-format file. */
    fun saltOf(bytes: ByteArray): ByteArray =
        bytes.copyOfRange(FILE_MAGIC.size, FILE_MAGIC.size + SALT_LENGTH)

    /** Current key derivation: Argon2id, matching the companion Python tooling. */
    fun deriveKeyArgon2(password: String, salt: ByteArray): String {
        val output = ByteArray(ARGON2_KEY_LENGTH)
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(ARGON2_ITERATIONS)
            .withMemoryAsKB(ARGON2_MEMORY_KB)
            .withParallelism(ARGON2_PARALLELISM)
            .withSalt(salt)
            .build()
        try {
            Argon2BytesGenerator().apply { init(params) }
                .generateBytes(passwordBytes, output)
            return getUrlEncoder().encodeToString(output)
        } finally {
            // Wipe the transient secret byte arrays so they don't linger on the heap.
            passwordBytes.fill(0)
            output.fill(0)
        }
    }

    /** Legacy key derivation: PBKDF2 with a fixed salt. Only used to read old data. */
    fun deriveKeyLegacy(password: String): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            LEGACY_KEY_SALT.toByteArray(),
            LEGACY_KEY_ITERATIONS,
            LEGACY_DERIVED_KEY_LENGTH
        )
        val key = SecretKeyFactory.getInstance(LEGACY_KEY_ALGORITHM).generateSecret(spec).encoded
        return getUrlEncoder().encodeToString(key)
    }

    /** Encrypts [plainData] into the new file format using an already-derived Fernet [key]. */
    fun encrypt(plainData: String, key: String, salt: ByteArray): ByteArray {
        val token = Token.generate(Key(key), plainData).serialise()
        return FILE_MAGIC + salt + token.toByteArray(Charsets.US_ASCII)
    }

    /** Decrypts a new-format file given the already-derived Fernet [key]. */
    fun decryptNewFormat(bytes: ByteArray, key: String): String {
        val tokenString = bytes.copyOfRange(FILE_MAGIC.size + SALT_LENGTH, bytes.size).decodeToString()
        return Token.fromString(tokenString).validateAndDecrypt(Key(key), noExpiryValidator())
    }

    /** Decrypts a legacy raw-Fernet token given the already-derived Fernet [key]. */
    fun decryptLegacyFernet(tokenString: String, key: String): String =
        Token.fromString(tokenString).validateAndDecrypt(Key(key), noExpiryValidator())

    private fun noExpiryValidator() = object : StringValidator {
        override fun getTimeToLive(): TemporalAmount = Duration.ofSeconds(Instant.MAX.epochSecond)
    }
}

package com.example.passwordmanagerv1.utils

const val DATAFILE_NAME = "accounts"
const val DATAFILE_EXTENSION = ".data"
const val DATAFILE_NAME_AND_EXTENSION = DATAFILE_NAME + DATAFILE_EXTENSION
const val DATAFILE_MIMETYPE_BINARY = "application/octet-stream"
const val DATAFILE_MIMETYPE_PLAIN = "text/plain"

// According to https://owasp.org/www-community/password-special-characters
const val VALID_SPECIAL_CHARS_WITHOUT_SPACE = "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
const val VALID_SPECIAL_CHARS_WITH_SPACE = " !\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
const val ALPHANUMERIC_CHARS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"

const val OPTION_CODE_ADD_ACCOUNT = "ADD_ACCOUNT"
const val OPTION_CODE_SEARCH_ACCOUNT_NAME = "SEARCH_ACCOUNT_NAME"
const val OPTION_CODE_SEARCH_EMAIL = "SEARCH_EMAIL"
const val OPTION_CODE_SEARCH_USERNAME = "SEARCH_USERNAME"
const val OPTION_CODE_SEARCH_PHONE = "SEARCH_PHONE"
const val OPTION_CODE_SEARCH_PASSWORD = "SEARCH_PASSWORD"
const val OPTION_CODE_SEARCH_LINKED_ACCOUNT = "SEARCH_LINKED_ACCOUNT"
const val OPTION_CODE_IMPORT_EXPORT_DATA = "IMPORT_EXPORT_DATA"

val MENU_OPTION_ORDER = listOf(
    OPTION_CODE_ADD_ACCOUNT,
    OPTION_CODE_SEARCH_ACCOUNT_NAME,
    OPTION_CODE_SEARCH_EMAIL,
    OPTION_CODE_SEARCH_USERNAME,
    OPTION_CODE_SEARCH_PHONE,
    OPTION_CODE_SEARCH_PASSWORD,
    OPTION_CODE_SEARCH_LINKED_ACCOUNT,
    OPTION_CODE_IMPORT_EXPORT_DATA
)

enum class AccountFieldType {
    accountName,
    email,
    username,
    phone,
    password,
    linkedAccounts,
    misc
}

const val EXTRA_ACCOUNT_NAME = "EXTRA_ACCOUNT_NAME"
const val EXTRA_ACCOUNT_FIELD_TYPE = "EXTRA_ACCOUNT_FIELD"
const val EXTRA_ACCOUNT_FIELD_VALUE = "EXTRA_ACCOUNT_FIELD_VALUE"
const val EXTRA_MISC_FIELD_TITLE = "EXTRA_MISC_FIELD_TITLE"
const val EXTRA_IMPORT_DATA_URI = "EXTRA_IS_IMPORTING_DATA"
const val EXTRA_VERIFICATION = "EXTRA_VERIFICATION"

const val LABEL_COPY = "LABEL_COPY"

// --- Current encryption scheme: Argon2id + Fernet ---
// File format: [4-byte magic][32-byte random salt][Fernet ciphertext]
val FILE_MAGIC = byteArrayOf('P'.code.toByte(), 'W'.code.toByte(), 'M'.code.toByte(), 0x01)
const val SALT_LENGTH = 32
const val ARGON2_KEY_LENGTH = 32
const val ARGON2_ITERATIONS = 3
const val ARGON2_MEMORY_KB = 65536
const val ARGON2_PARALLELISM = 4

// --- Legacy encryption scheme: PBKDF2 + Fernet (raw token, no header) ---
// Retained only to decrypt/import data produced by older versions. Never used for new writes.
const val LEGACY_KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
const val LEGACY_KEY_SALT = "69420"
const val LEGACY_DERIVED_KEY_LENGTH = 256
const val LEGACY_KEY_ITERATIONS = 69420

// How long a copied secret stays on the clipboard before it is auto-cleared.
const val CLIPBOARD_CLEAR_DELAY_MS = 60_000L
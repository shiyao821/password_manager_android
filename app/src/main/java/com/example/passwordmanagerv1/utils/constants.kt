package com.example.passwordmanagerv1.utils

const val DATAFILE_NAME = "accounts.data"

// According to https://owasp.org/www-community/password-special-characters
const val VALID_SPECIAL_CHARS_WITHOUT_SPACE = "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
const val VALID_SPECIAL_CHARS_WITH_SPACE = " !\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
const val ALPHANUMERIC_CHARS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"

const val OPTION_CODE_ADD_ACCOUNT:String = "ADD_ACCOUNT"
const val OPTION_CODE_SEARCH_ACCOUNT_NAME = "SEARCH_ACCOUNT_NAME"
const val OPTION_CODE_SEARCH_EMAIL = "SEARCH_EMAIL"
const val OPTION_CODE_SEARCH_USERNAME = "SEARCH_USERNAME"
const val OPTION_CODE_SEARCH_PHONE = "SEARCH_PHONE"
const val OPTION_CODE_SEARCH_PASSWORD = "SEARCH_PASSWORD"
const val OPTION_CODE_SEARCH_LINKED_ACCOUNT = "SEARCH_LINKED_ACCOUNT"

val MENU_OPTION_ORDER = listOf(
    OPTION_CODE_ADD_ACCOUNT,
    OPTION_CODE_SEARCH_ACCOUNT_NAME,
    OPTION_CODE_SEARCH_EMAIL,
    OPTION_CODE_SEARCH_USERNAME,
    OPTION_CODE_SEARCH_PHONE,
    OPTION_CODE_SEARCH_PASSWORD,
    OPTION_CODE_SEARCH_LINKED_ACCOUNT
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
const val EXTRA_ACCOUNT_NAMES_LIST = "EXTRA_ACCOUNT_NAMES_LIST"
const val EXTRA_ACCOUNT_FIELD_TYPE = "EXTRA_ACCOUNT_FIELD"
const val EXTRA_MISC_FIELD_TITLE = "EXTRA_MISC_FIELD_TITLE"
const val LABEL_COPY = "LABEL_COPY"

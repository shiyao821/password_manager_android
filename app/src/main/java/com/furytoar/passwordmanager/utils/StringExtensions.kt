package com.furytoar.passwordmanager.utils

import java.util.Locale

/** Capitalizes the first character, replacing the deprecated [String.capitalize]. */
fun String.titleCase(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

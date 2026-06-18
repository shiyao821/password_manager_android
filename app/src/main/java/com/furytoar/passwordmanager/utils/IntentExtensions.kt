package com.furytoar.passwordmanager.utils

import android.content.Intent
import android.os.Build
import java.io.Serializable

/**
 * Type-safe replacement for the deprecated [Intent.getSerializableExtra]. Uses the typed
 * API 33 overload where available and falls back to the legacy cast on older versions.
 */
inline fun <reified T : Serializable> Intent.serializableExtraCompat(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        (getSerializableExtra(name) as? T)
    }

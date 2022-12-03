package com.example.passwordmanagerv1

import kotlinx.serialization.Serializable
import java.io.Serializable as SerializableClass

@Serializable
data class Account (
    val accountName: String,
    val username: String,
    val email: String,
    val phone: String,
    val password: String,
    val linkedAccounts: List<String>,
    val misc: Map<String, String>
 ) : SerializableClass // can optimize to Parcelable
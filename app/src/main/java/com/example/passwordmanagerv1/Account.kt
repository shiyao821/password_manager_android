package com.example.passwordmanagerv1

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.io.Serializable as SerializableClass

@Serializable
data class Account (
    var accountName: String,
    var username: String,
    var email: String,
    var phone: String,
    var password: String,
    var linkedAccounts: MutableList<String>,
    var misc: MutableMap<String, String>,
    var lastEdited: LocalDateTime,
 ) : SerializableClass // can optimize to Parcelable
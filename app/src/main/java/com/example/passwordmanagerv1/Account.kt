package com.example.passwordmanagerv1

import kotlinx.serialization.Serializable
import java.io.Serializable as SerializableClass

@Serializable
data class Account (
    var accountName: String,
    var username: String,
    var email: String,
    var phone: String,
    var password: String,
    var linkedAccounts: List<String>,
    var misc: Map<String, String>
 ) : SerializableClass // can optimize to Parcelable
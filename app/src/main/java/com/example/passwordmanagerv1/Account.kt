package com.example.passwordmanagerv1

import kotlinx.serialization.Serializable

@Serializable
data class Account (
    val accountName: String,
    val username: String,
    val email: String,
    val phone: String,
    val password: String,
    val linkedAccount: String,
    val misc: Map<String, String>
)
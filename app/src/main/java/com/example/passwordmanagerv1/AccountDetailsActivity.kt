package com.example.passwordmanagerv1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT

class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        account = intent.getSerializableExtra(EXTRA_ACCOUNT) as Account


    }
}
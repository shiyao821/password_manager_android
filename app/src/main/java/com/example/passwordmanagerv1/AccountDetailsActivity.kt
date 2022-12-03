package com.example.passwordmanagerv1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AbsListView.RecyclerListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT


class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        account = intent.getSerializableExtra(EXTRA_ACCOUNT) as Account

        populateValues()

    }

    private fun populateValues() {
        val tvAccountNameValue = findViewById<TextView>(R.id.tvAccountNameValue)
        tvAccountNameValue.text = account.accountName
        val tvEmailValue = findViewById<TextView>(R.id.tvEmailValue)
        tvEmailValue.text = account.email
        val tvUserNameValue = findViewById<TextView>(R.id.tvUsernameValue)
        tvUserNameValue.text = account.email
        val tvPhoneValue = findViewById<TextView>(R.id.tvPhoneValue)
        tvPhoneValue.text = account.phone
        val tvPasswordValue = findViewById<TextView>(R.id.tvPasswordValue)
        tvPasswordValue.text = account.password

        // linked accounts uses horizontal scrollable linear layout RecyclerView
        val rvLinkedAccounts = findViewById<RecyclerView>(R.id.rvLinkedAccounts)
        rvLinkedAccounts.adapter = LinkedAccountsFieldAdapter(this, account.linkedAccount)
        rvLinkedAccounts.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        // misc info uses vertical scrollable linear layout RecyclerView
        val rvMisc = findViewById<RecyclerView>(R.id.rvMisc)
        val miscList = account.misc.entries.map { Pair(it.key, it.value) }
        rvMisc.adapter = MiscFieldsAdapter(this, miscList)
        rvMisc.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

    }
}
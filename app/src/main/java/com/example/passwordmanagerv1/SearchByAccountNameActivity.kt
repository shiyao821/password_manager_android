package com.example.passwordmanagerv1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.biometric.BiometricPrompt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.adapters.SearchByAccountNameAdapter
import com.example.passwordmanagerv1.utils.*

class SearchByAccountNameActivity : AppCompatActivity() {

    private lateinit var svSearch: SearchView
    private lateinit var rvSearchResult: RecyclerView
    private lateinit var adapter: SearchByAccountNameAdapter
    private lateinit var searchResults: List<String>
    private var accountFieldType: AccountFieldType? = null
    private var accountFieldValue: String? = null
    companion object {
        const val TAG = "clg:SearchByName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        accountFieldType = intent.getSerializableExtra(EXTRA_ACCOUNT_FIELD_TYPE) as AccountFieldType?
        accountFieldValue = intent.getStringExtra(EXTRA_ACCOUNT_FIELD_VALUE)
        if (accountFieldType != null && accountFieldValue != null) {
            title = resources.getString(R.string.activity_label_filtered_results_prefix) +
                    " " + accountFieldValue
        }
        searchResults = searchAccountNames()
        svSearch = findViewById(R.id.svSearch)
        rvSearchResult = findViewById(R.id.rvSearchResult)

        adapter = SearchByAccountNameAdapter(
            this,
            searchResults,
            object : SearchByAccountNameAdapter.OnItemClickListener {
                override fun onItemClick(accountName: String) {

                    val biometricAuthenticatorCallback = object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int,
                                                           errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            Log.e(AccountDetailsActivity.TAG, "Authentication error: $errString, $errorCode")
                        }

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)

                            val intent = Intent(
                                this@SearchByAccountNameActivity,
                                AccountDetailsActivity::class.java
                            ).putExtra(EXTRA_ACCOUNT_NAME, accountName)
                            startActivity(intent)
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Log.i(AccountDetailsActivity.TAG, "biometrics failed")
                        }
                    }
                    Authenticator.authenticate(this@SearchByAccountNameActivity, biometricAuthenticatorCallback)
                }
            }
        )
        rvSearchResult.adapter = adapter
        rvSearchResult.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        svSearch.isIconifiedByDefault = false
        svSearch.setOnQueryTextListener(object: OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                onQueryTextChange(p0)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    adapter.filter(p0)
                }
                return true
            }
        })
    }

    private fun searchAccountNames() : List<String> {
        if (accountFieldValue != null && accountFieldType != null) {
            return Manager.getAccountNamesFilteredByField(accountFieldType!!, accountFieldValue!!)
        }
        return Manager.getAllAccountNames()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        CommonUIBehaviors.focusViewAndShowKeyboard(svSearch, this)
        searchResults = searchAccountNames()
        adapter.updateData(searchResults)
    }
}

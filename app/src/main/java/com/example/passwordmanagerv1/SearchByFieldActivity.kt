package com.example.passwordmanagerv1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.utils.*
import java.util.*
import kotlin.collections.ArrayList

class SearchByFieldActivity : AppCompatActivity() {
    private lateinit var accountFieldType: AccountFieldType
    private lateinit var svSearch: SearchView
    private lateinit var rvSearchResult: RecyclerView
    private lateinit var adapter: SearchByFieldAdapter
    private lateinit var results: Map<String, Int>

    companion object {
        const val TAG = "clg:SearchByField"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        accountFieldType = intent.getSerializableExtra(EXTRA_ACCOUNT_FIELD_TYPE) as AccountFieldType
        results = when (accountFieldType) {
            AccountFieldType.username -> Manager.getAllUsernames()
            AccountFieldType.email -> Manager.getAllEmails()
            AccountFieldType.phone -> Manager.getAllPhoneNumbers()
            AccountFieldType.password -> Manager.getAllPasswords()
            AccountFieldType.linkedAccounts -> Manager.getAllLinkedAccounts()
            else -> {
                Log.e(TAG, "Error in account field type")
                mapOf()
            }
        }

        title = when (accountFieldType) {
            AccountFieldType.username -> resources.getString(R.string.SEARCH_USERNAME).capitalize()
            AccountFieldType.email -> resources.getString(R.string.SEARCH_EMAIL).capitalize()
            AccountFieldType.phone -> resources.getString(R.string.SEARCH_PHONE).capitalize()
            AccountFieldType.password -> resources.getString(R.string.SEARCH_PASSWORD).capitalize()
            AccountFieldType.linkedAccounts -> resources.getString(R.string.SEARCH_LINKED_ACCOUNT).capitalize()
            else -> {
                Log.e(TAG, "Error in account field type")
                resources.getString(R.string.error)
            }
        }

        svSearch = findViewById(R.id.svSearch)
        rvSearchResult = findViewById(R.id.rvSearchResult)
    }

    override fun onStart() {
        super.onStart()
        adapter = SearchByFieldAdapter(
            this,
            results.keys.toList().sorted(),
            object : SearchByFieldAdapter.OnItemClickListener {
                override fun onItemClick(item: String) {
                    val intent = Intent(
                        this@SearchByFieldActivity,
                        SearchByAccountNameActivity::class.java
                    )
                    val filteredAccountNames = Manager.getAccountNamesFilteredByField(accountFieldType, item) as ArrayList<String>
                    intent.putStringArrayListExtra(EXTRA_ACCOUNT_NAMES_LIST, filteredAccountNames)
                    startActivity(intent)
                }
            }
        )
        rvSearchResult.adapter = adapter
        rvSearchResult.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        svSearch.isIconifiedByDefault = false
        svSearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
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
    }
}
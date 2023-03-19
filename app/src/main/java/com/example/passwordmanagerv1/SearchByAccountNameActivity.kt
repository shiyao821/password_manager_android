package com.example.passwordmanagerv1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.utils.CommonUIBehaviors
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT_NAME
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT_NAMES_LIST

class SearchByAccountNameActivity : AppCompatActivity() {

    private lateinit var svSearch: SearchView
    private lateinit var rvSearchResult: RecyclerView
    private lateinit var adapter: SearchByAccountNameAdapter
    private lateinit var searchResults: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by)

        val listStringExtra = intent.getStringArrayListExtra(EXTRA_ACCOUNT_NAMES_LIST)
        if (listStringExtra != null) {
            // temp solution to filtered results not updating after filtered account is edited
            title = resources.getString(R.string.activity_label_filtered_results)
        }
        svSearch = findViewById(R.id.svSearch)
        rvSearchResult = findViewById(R.id.rvSearchResult)
    }

    override fun onStart() {
        super.onStart()
        searchResults = Manager.getAllAccountNames()
        adapter = SearchByAccountNameAdapter(
            this,
            searchResults,
            object : SearchByAccountNameAdapter.OnItemClickListener {
                override fun onItemClick(accountName: String) {
                    val intent = Intent(
                        this@SearchByAccountNameActivity,
                        AccountDetailsActivity::class.java
                    )
                    intent.putExtra(EXTRA_ACCOUNT_NAME, accountName)
                    startActivity(intent)
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

    override fun onResume() {
        super.onResume()
        CommonUIBehaviors.focusViewAndShowKeyboard(svSearch, this)
    }
}

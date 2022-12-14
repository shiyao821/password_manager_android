package com.example.passwordmanagerv1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT_NAME
import com.example.passwordmanagerv1.utils.EXTRA_MENU_OPTION

class SearchByActivity : AppCompatActivity() {

    private lateinit var searchByCode: String
    private lateinit var svSearch: SearchView
    private lateinit var rvSearchResult: RecyclerView
    private lateinit var adapter: SearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by)

        searchByCode = intent.getStringExtra(EXTRA_MENU_OPTION)!!
        svSearch = findViewById(R.id.svSearch)
        rvSearchResult = findViewById(R.id.rvSearchResult)
    }

    override fun onStart() {
        super.onStart()
        val results = Manager.getAllAccountNames()
        adapter = SearchResultAdapter(
            this,
            results,
            object : SearchResultAdapter.OnItemClickListener {
                override fun onItemClick(accountName: String) {
                    val intent = Intent(
                        this@SearchByActivity,
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
}

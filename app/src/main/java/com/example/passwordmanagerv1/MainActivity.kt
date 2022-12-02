package com.example.passwordmanagerv1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.utils.*

class MainActivity : AppCompatActivity() {

    private lateinit var rvMenu: RecyclerView
    private lateinit var menuAdapter: MenuAdapter

    companion object {
        const val TAG = "Debug MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val optionTextsMap = mapOf(
            OPTION_CODE_ADD_ACCOUNT to this.resources.getString(R.string.ADD_ACCOUNT),
            OPTION_CODE_SEARCH_ACCOUNT_NAME to this.resources.getString(R.string.SEARCH_ACCOUNT_NAME),
            OPTION_CODE_SEARCH_EMAIL to this.resources.getString(R.string.SEARCH_EMAIL),
            OPTION_CODE_SEARCH_USERNAME to this.resources.getString(R.string.SEARCH_USERNAME),
            OPTION_CODE_SEARCH_PHONE to this.resources.getString(R.string.SEARCH_PHONE),
            OPTION_CODE_SEARCH_PASSWORD to this.resources.getString(R.string.SEARCH_PASSWORD),
            OPTION_CODE_SEARCH_LINKED_ACCOUNT to this.resources.getString(R.string.SEARCH_LINKED_ACCOUNT),
        )

        this.applicationContext
        rvMenu = findViewById(R.id.rvMenu)
        menuAdapter = MenuAdapter(this, MENU_OPTION_ORDER,
            optionTextsMap, object: MenuAdapter.onOptionClickListener {
            override fun onOptionClicked(position: Int) {
                Log.i(TAG, "Option code ${MENU_OPTION_ORDER[position]} activated")
                when (MENU_OPTION_ORDER[position]) {
                    // different new activity for adding and searching
                    OPTION_CODE_ADD_ACCOUNT -> {

                    }
                    else -> {

                    }
                }
            }
        })
        rvMenu.adapter = menuAdapter
        rvMenu.layoutManager = LinearLayoutManager(this)
    }
}
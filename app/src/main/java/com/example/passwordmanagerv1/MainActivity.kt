package com.example.passwordmanagerv1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.adapters.MenuAdapter
import com.example.passwordmanagerv1.utils.*

class MainActivity : AppCompatActivity() {

    private val manager = Manager
    private lateinit var rvMenu: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private var hasBackBeenPressed = false

    companion object {
        const val TAG = "clg:Main"
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
            OPTION_CODE_IMPORT_EXPORT_DATA to resources.getString(R.string.IMPORT_EXPORT_DATA)
        )

        rvMenu = findViewById(R.id.rvMenu)
        menuAdapter = MenuAdapter(this, MENU_OPTION_ORDER,
            optionTextsMap, object : MenuAdapter.onOptionClickListener {
                override fun onOptionClicked(position: Int) {
                    Log.i(TAG, "Option code ${MENU_OPTION_ORDER[position]} activated")
                    when (MENU_OPTION_ORDER[position]) {
                        // different new activity for adding and searching
                        OPTION_CODE_ADD_ACCOUNT -> addAccount()
                        OPTION_CODE_SEARCH_ACCOUNT_NAME -> {
                            val intent = Intent(this@MainActivity, SearchByAccountNameActivity::class.java)
                            startActivity(intent)
                        }
                        OPTION_CODE_SEARCH_EMAIL -> startSearchByField(AccountFieldType.email)
                        OPTION_CODE_SEARCH_USERNAME -> startSearchByField(AccountFieldType.username)
                        OPTION_CODE_SEARCH_PHONE -> startSearchByField(AccountFieldType.phone)
                        OPTION_CODE_SEARCH_PASSWORD -> startSearchByField(AccountFieldType.password)
                        OPTION_CODE_SEARCH_LINKED_ACCOUNT -> startSearchByField(AccountFieldType.linkedAccounts)
                        OPTION_CODE_IMPORT_EXPORT_DATA -> {
                            val intent = Intent(this@MainActivity, ImportExportActivity::class.java)
                            startActivity(intent)
                        }
                        else -> {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Not Implemented")
                        }
                    }
                }
            })
        rvMenu.adapter = menuAdapter
        rvMenu.layoutManager = LinearLayoutManager(this)
    }

    private fun startSearchByField(accountFieldType: AccountFieldType) {
        val intent = Intent(this@MainActivity, SearchByFieldActivity::class.java)
        intent.putExtra(EXTRA_ACCOUNT_FIELD_TYPE, accountFieldType)
        startActivity(intent)
    }

    private fun addAccount() {
        val newAccountDialogView = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.dialog_single_line_text_input, null)
        val ettpInitialAccountName =
            newAccountDialogView.findViewById<EditText>(R.id.etDialogSingleLineTextInput)

        AlertDialog.Builder(this@MainActivity)
            .setTitle("New Account")
            .setMessage("Create a name for the new account")
            .setView(newAccountDialogView)
            .setPositiveButton(R.string.button_acknowledge) { _, _ ->
                // check duplicates
                val initialName = ettpInitialAccountName.text.toString()
                if (manager.ifAccountNameExists(initialName)) {
                    Toast.makeText(
                        this@MainActivity,
                        "The account name '$initialName' already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // create account and pass to account details activity
                    if (manager.createAccount(initialName)) {
                        val intent = Intent(
                            this@MainActivity,
                            AccountDetailsActivity::class.java
                        )
                        // used for UI testing
                        intent.putExtra(EXTRA_ACCOUNT_NAME, initialName)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .show()
    }

    override fun onBackPressed() {
        if (hasBackBeenPressed) {
            finish(); // finish activity
        } else {
            Toast.makeText(
                this, this.resources.getString(R.string.toast_back_press_again_log_out),
                Toast.LENGTH_SHORT
            ).show();
            hasBackBeenPressed = true;
            Handler(Looper.getMainLooper()).postDelayed({
                hasBackBeenPressed = false;
            }, this.resources.getInteger(R.integer.double_back_press_max_interval_ms).toLong());
        }
        super.onBackPressed()
    }
}
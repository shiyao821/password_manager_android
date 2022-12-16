package com.example.passwordmanagerv1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.utils.CommonUIBehaviors
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT_NAME

class EditLinkedAccountsActivity : AppCompatActivity() {

    private lateinit var accountInContext: String
    private lateinit var linkedAccounts: List<String>

    private lateinit var rvLinkedAccounts: RecyclerView
    private lateinit var adapter: RecyclerView.Adapter<LinkedAccountsAdapter.ViewHolder>
    private lateinit var clAddNewLink: ConstraintLayout
    private lateinit var ivIcon: ImageView
    private lateinit var etAddLinkedAccount: EditText
    private lateinit var tvAddNewLink: TextView
    private lateinit var btnConfirmLink: Button
    private lateinit var btnDone: Button

    companion object {
        const val TAG = "clg:EditLinkedAcc"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_linked_accounts)

        accountInContext = intent.getStringExtra(EXTRA_ACCOUNT_NAME)!!
        linkedAccounts = Manager.getLinkedAccounts(accountInContext)
        Log.i(TAG, "Received Linked Accounts $linkedAccounts")

        clAddNewLink = findViewById(R.id.clAddNewLink)
        ivIcon = findViewById(R.id.ivLinkedAccountAdd)
        etAddLinkedAccount = findViewById(R.id.etAddLinkedAccount)
        tvAddNewLink = findViewById(R.id.tvAddNewLink)
        btnConfirmLink = findViewById(R.id.btnAddLink)
        btnDone = findViewById(R.id.btnDone)

        clAddNewLink.setOnClickListener {
            addNewLinkClick()
        }

        etAddLinkedAccount.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    requestNewLink(accountInContext, etAddLinkedAccount.text.toString())
                    true
                } else -> false
            }
        }

        btnConfirmLink.setOnClickListener {
            requestNewLink(accountInContext, etAddLinkedAccount.text.toString())
        }

        btnDone.setOnClickListener {
            this.finish()
        }
    }

    private fun addNewLinkClick() {
        Log.i(LinkedAccountsAdapter.TAG, "Add linked accounts bar clicked")
        if (etAddLinkedAccount.visibility == View.VISIBLE) {
            ivIcon.setImageResource(R.drawable.ic_baseline_add_circle_24)
            etAddLinkedAccount.visibility = View.INVISIBLE
            btnConfirmLink.visibility = View.GONE
            tvAddNewLink.visibility = View.VISIBLE
            btnDone.visibility = View.VISIBLE
            CommonUIBehaviors.hideKeyboard(etAddLinkedAccount, this)
        } else {
            ivIcon.setImageResource(R.drawable.ic_baseline_cancel_24)
            etAddLinkedAccount.visibility = View.VISIBLE
            btnConfirmLink.visibility = View.VISIBLE
            tvAddNewLink.visibility = View.GONE
            btnDone.visibility = View.GONE
            CommonUIBehaviors.focusViewAndShowKeyboard(etAddLinkedAccount, this)
        }
    }

    private fun requestNewLink(currentAccountName: String, requestedAccountName: String) {
        Log.i(TAG, "New account link requested")
        // duplication check - decided to do at UI level instead of Managers
        // 1) to avoid using exceptions
        // 2) relevant information already brought over to UI level
        if (requestedAccountName in linkedAccounts) {
            AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.alert_title_account_already_linked))
                .setPositiveButton(resources.getString(R.string.button_acknowledge)) { _,_ ->
                    etAddLinkedAccount.requestFocus()
                }
                .show()
            return
        }
        if (!Manager.addToLinkedAccounts(currentAccountName, requestedAccountName)) {
            val toast = resources.getString(R.string.alert_title_account_not_found)
            Log.i(TAG, "account $requestedAccountName not found")
            AlertDialog.Builder(this)
                .setTitle(toast)
                .setPositiveButton(resources.getString(R.string.button_acknowledge)) { _,_ ->
                    etAddLinkedAccount.requestFocus()
                }
                .show()
            return
        }
        addNewLinkClick()
        adapter.notifyItemInserted(adapter.itemCount) // refresh RecyclerView
        Toast.makeText(this, resources.getString(R.string.toast_account_link_success), Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        rvLinkedAccounts = findViewById(R.id.rvEditLinkedAccounts)
        adapter = LinkedAccountsAdapter(this, linkedAccounts,
        object : LinkedAccountsAdapter.OnLinkedAccountClickListener {
            override fun onButtonClick(linkedAccountName: String) {
                val intent = Intent(this@EditLinkedAccountsActivity, AccountDetailsActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_NAME, linkedAccountName)
                startActivity(intent)
            }
        },
        object: LinkedAccountsAdapter.OnUnlinkAccountClickListener {
            override fun onButtonClick(linkedAccountName: String) {
                removeLinkClick(linkedAccountName)
            }
        })
        rvLinkedAccounts.adapter = adapter
        rvLinkedAccounts.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)
    }

    private fun removeLinkClick(linkedAccountName: String) {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.alert_title_unlink_account_confirm))
            .setPositiveButton(resources.getString(R.string.button_delete)) { _,_ ->
                if (!Manager.removeFromLinkedAccounts(accountInContext, linkedAccountName)) {
                    Log.e(TAG, "Error in removing linked account")
                    Toast.makeText(this, resources.getString(R.string.toast_error), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                Toast.makeText(this, resources.getString(R.string.toast_account_unlink_success), Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton(resources.getString(R.string.button_cancel)) { _,_ -> }
            .show()
    }
}

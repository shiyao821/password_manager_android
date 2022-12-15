package com.example.passwordmanagerv1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.utils.CommonUIBehaviors
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT_NAME
import com.example.passwordmanagerv1.utils.EXTRA_LINKED_ACCOUNTS_NAMES

class EditLinkedAccountsActivity : AppCompatActivity() {

    private lateinit var rvLinkedAccounts: RecyclerView
    private lateinit var adapter: RecyclerView.Adapter<LinkedAccountsAdapter.ViewHolder>
    private lateinit var linkedAccounts: List<String>

    companion object {
        const val TAG = "debug EditLinkedAcc"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_linked_accounts)

        linkedAccounts = intent.getStringArrayListExtra(EXTRA_LINKED_ACCOUNTS_NAMES) as List<String>
        Log.i(TAG, "Received Linked Accounts $linkedAccounts")

        val clAddNewLink = findViewById<ConstraintLayout>(R.id.clAddNewLink)
        val ivIcon = findViewById<ImageView>(R.id.ivLinkedAccountAdd)
        val etAddLinkedAccount = findViewById<EditText>(R.id.etAddLinkedAccount)
        val tvAddNewLink = findViewById<TextView>(R.id.tvAddNewLink)
        val btnConfirmLink = findViewById<Button>(R.id.btnAddLink)

        clAddNewLink.setOnClickListener {
            Log.i(LinkedAccountsAdapter.TAG, "Add linked accounts bar clicked")
            if (etAddLinkedAccount.visibility == View.VISIBLE) {
                ivIcon.setImageResource(R.drawable.ic_baseline_add_circle_24)
                etAddLinkedAccount.visibility = View.INVISIBLE
                btnConfirmLink.visibility = View.GONE
                tvAddNewLink.visibility = View.VISIBLE
                CommonUIBehaviors.hideKeyboard(etAddLinkedAccount, this)
            } else {
                ivIcon.setImageResource(R.drawable.ic_baseline_cancel_24)
                etAddLinkedAccount.visibility = View.VISIBLE
                btnConfirmLink.visibility = View.VISIBLE
                tvAddNewLink.visibility = View.GONE
                CommonUIBehaviors.focusViewAndShowKeyboard(etAddLinkedAccount, this)
            }
        }
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
        })
        rvLinkedAccounts.adapter = adapter
        rvLinkedAccounts.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)
    }
}
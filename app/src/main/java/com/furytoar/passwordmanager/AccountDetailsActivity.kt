package com.furytoar.passwordmanager

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furytoar.passwordmanager.adapters.LinkedAccountsFieldAdapter
import com.furytoar.passwordmanager.adapters.MiscFieldsAdapter
import com.furytoar.passwordmanager.databinding.ActivityAccountDetailsBinding
import com.furytoar.passwordmanager.utils.*


class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var account: Account
    private lateinit var res: Resources
    private lateinit var binding: ActivityAccountDetailsBinding

    companion object {
        const val TAG = "clg:AccDetails"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CommonUIBehaviors.applySecureFlag(this)
        res = this.resources
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)
        val fetchedAccount = accountName?.let { Manager.getAccount(it) }
        if (fetchedAccount == null) {
            Toast.makeText(this, R.string.toast_account_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        account = fetchedAccount
    }

    override fun onStart() {
        super.onStart()
        populateValues()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_account_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miDeleteAccount -> {
                AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.alert_title_account_delete_confirmation))
                    .setMessage(resources.getString(R.string.alert_message_account_delete_confirmation))
                    .setPositiveButton(resources.getString(R.string.button_delete)) { _,_ ->
                        this.deleteAccount(account.accountName)
                    }
                    .setNegativeButton(resources.getString(R.string.button_cancel)) { _,_ -> }
                    .show()
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
            }
            else -> {}
        }
        return true
    }

    private fun deleteAccount(accountName :String) {

        Manager.deleteAccount(account.accountName)
        this@AccountDetailsActivity.finish()
    }


    private fun populateValues() {
        binding.tvAccountNameValue.text = account.accountName
        binding.tvEmailValue.text = account.email
        binding.tvUsernameValue.text = account.username
        binding.tvPhoneValue.text = account.phone
        binding.tvPasswordValue.text = account.password
        binding.tvLastEditedValue.text = account.lastEdited.toString().split('T').joinToString(" ")

        // linked accounts uses horizontal scrollable linear layout RecyclerView
        binding.rvEditLinkedAccounts.adapter = LinkedAccountsFieldAdapter(this,
            account.linkedAccounts,
            object: LinkedAccountsFieldAdapter.OnLinkedAccountClickListener {
                override fun onButtonClick(linkedAccountName: String) {
                    val intent = Intent(this@AccountDetailsActivity, AccountDetailsActivity::class.java)
                    intent.putExtra(EXTRA_ACCOUNT_NAME, linkedAccountName)
                    startActivity(intent)
                }
            }
        )
        binding.rvEditLinkedAccounts.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        // misc info uses vertical scrollable linear layout RecyclerView
        val miscList = account.misc.entries.map { Pair(it.key, it.value) }
        binding.rvMisc.adapter = MiscFieldsAdapter(this, miscList,
        object: MiscFieldsAdapter.OnEditMiscClickListener {
            override fun onClick(fieldTitle: String) {
                val intent = Intent(this@AccountDetailsActivity, EditMiscActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_NAME, account.accountName)
                intent.putExtra(EXTRA_MISC_FIELD_TITLE, fieldTitle)
                startActivity(intent)
            }
        })
        binding.rvMisc.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        // add onClickListeners
        binding.ivAccountNameEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.accountName))
        binding.ivEmailEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.email))
        binding.ivUsernameEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.username))
        binding.ivPhoneEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.phone))
        binding.ivPasswordEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.password))
        binding.ivLinkedAccountsEdit.setOnClickListener {
            val intent = Intent(this, EditLinkedAccountsActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_NAME, account.accountName)
            startActivity(intent)
        }
        binding.clAddNewMiscField.setOnClickListener {
            val intent = Intent(this, EditMiscActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_NAME, account.accountName)
            startActivity(intent)
        }

        binding.ivAccountNameCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.accountName,
                res.getString(R.string.title_account_name)) }
        binding.ivUsernameCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.username,
                res.getString(R.string.title_username)) }
        binding.ivEmailCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.email,
                res.getString(R.string.title_email)) }
        binding.ivPhoneCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.phone,
                res.getString(R.string.title_phone)) }
        binding.ivPasswordCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.password,
                res.getString(R.string.title_password)) }
    }

    inner class EditStringOnClickListener(
        val accountFieldType: AccountFieldType,
        ): OnClickListener {
        override fun onClick(p0: View) {
            Log.i(TAG, "Edit Button for $accountFieldType clicked")
            val intent = Intent(this@AccountDetailsActivity, EditStringActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_FIELD_TYPE, accountFieldType)
            intent.putExtra(EXTRA_ACCOUNT_NAME, account.accountName)
            startActivity(intent)
        }
    }
}

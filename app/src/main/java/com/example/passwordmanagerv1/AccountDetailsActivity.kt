package com.example.passwordmanagerv1

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.utils.*


class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var account: Account
    private lateinit var res: Resources

    companion object {
        const val TAG = "clg:AccDetails"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)
        res = this.resources

        val accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)!!
        account = Manager.getAccount(accountName)!!
    }

    override fun onStart() {
        super.onStart()
        populateValues()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
            else -> {}
        }
        return true
    }

    private fun deleteAccount(accountName :String) {

        Manager.deleteAccount(account.accountName)
        this@AccountDetailsActivity.finish()
    }

    private fun populateValues() {
        val tvAccountNameValue = findViewById<TextView>(R.id.tvAccountNameValue)
        tvAccountNameValue.text = account.accountName
        val tvEmailValue = findViewById<TextView>(R.id.tvEmailValue)
        tvEmailValue.text = account.email
        val tvUserNameValue = findViewById<TextView>(R.id.tvUsernameValue)
        tvUserNameValue.text = account.username
        val tvPhoneValue = findViewById<TextView>(R.id.tvPhoneValue)
        tvPhoneValue.text = account.phone
        val tvPasswordValue = findViewById<TextView>(R.id.tvPasswordValue)
        tvPasswordValue.text = account.password

        // linked accounts uses horizontal scrollable linear layout RecyclerView
        val rvLinkedAccounts = findViewById<RecyclerView>(R.id.rvEditLinkedAccounts)
        rvLinkedAccounts.adapter = LinkedAccountsFieldAdapter(this,
            account.linkedAccounts,
            object: LinkedAccountsFieldAdapter.OnLinkedAccountClickListener {
                override fun onButtonClick(linkedAccountName: String) {
                    val intent = Intent(this@AccountDetailsActivity, AccountDetailsActivity::class.java)
                    intent.putExtra(EXTRA_ACCOUNT_NAME, linkedAccountName)
                    startActivity(intent)
                }
            }
        )
        rvLinkedAccounts.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        // misc info uses vertical scrollable linear layout RecyclerView
        val rvMisc = findViewById<RecyclerView>(R.id.rvMisc)
        val miscList = account.misc.entries.map { Pair(it.key, it.value) }
        rvMisc.adapter = MiscFieldsAdapter(this, miscList,
        object: MiscFieldsAdapter.OnEditMiscClickListener {
            override fun onClick(fieldTitle: String) {
                val intent = Intent(this@AccountDetailsActivity, EditMiscActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_NAME, account.accountName)
                intent.putExtra(EXTRA_MISC_FIELD_TITLE, fieldTitle)
                startActivity(intent)
            }
        })
        rvMisc.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        // add onClickListeners
        val ivAccountNameEdit = findViewById<ImageView>(R.id.ivAccountNameEdit)
        val ivEmailEdit = findViewById<ImageView>(R.id.ivEmailEdit)
        val ivUsernameEdit = findViewById<ImageView>(R.id.ivUsernameEdit)
        val ivPhoneEdit = findViewById<ImageView>(R.id.ivPhoneEdit)
        val ivPasswordEdit = findViewById<ImageView>(R.id.ivPasswordEdit)
        val ivLinkedAccountsEdit = findViewById<ImageView>(R.id.ivLinkedAccountsEdit)
        val clAddNewMiscField = findViewById<ConstraintLayout>(R.id.clAddNewMiscField)

        ivAccountNameEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.accountName))
        ivEmailEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.email))
        ivUsernameEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.username))
        ivPhoneEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.phone))
        ivPasswordEdit.setOnClickListener(EditStringOnClickListener(AccountFieldType.password))
        ivLinkedAccountsEdit.setOnClickListener {
            val intent = Intent(this, EditLinkedAccountsActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_NAME, account.accountName)
            startActivity(intent)
        }
        clAddNewMiscField.setOnClickListener {
            val intent = Intent(this, EditMiscActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_NAME, account.accountName)
            startActivity(intent)
        }

        val ivAccountNameCopy = findViewById<ImageView>(R.id.ivAccountNameCopy)
        val ivEmailCopy = findViewById<ImageView>(R.id.ivEmailCopy)
        val ivUsernameCopy = findViewById<ImageView>(R.id.ivUsernameCopy)
        val ivPhoneCopy = findViewById<ImageView>(R.id.ivPhoneCopy)
        val ivPasswordCopy = findViewById<ImageView>(R.id.ivPasswordCopy)

        ivAccountNameCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.accountName,
                res.getString(R.string.title_account_name)) }
        ivUsernameCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.username,
                res.getString(R.string.title_username)) }
        ivEmailCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.email,
                res.getString(R.string.title_email)) }
        ivPhoneCopy.setOnClickListener {
            CommonUIBehaviors.copyToClipboard(this, account.phone,
                res.getString(R.string.title_phone)) }
        ivPasswordCopy.setOnClickListener {
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

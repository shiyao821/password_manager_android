package com.furytoar.passwordmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.furytoar.passwordmanager.databinding.ActivityEditStringBinding
import com.furytoar.passwordmanager.utils.AccountFieldType
import com.furytoar.passwordmanager.utils.CommonUIBehaviors
import com.furytoar.passwordmanager.utils.EXTRA_ACCOUNT_NAME
import com.furytoar.passwordmanager.utils.EXTRA_ACCOUNT_FIELD_TYPE
import com.furytoar.passwordmanager.utils.serializableExtraCompat

class EditStringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditStringBinding
    private lateinit var etNewValue: EditText
    private lateinit var accountName: String
    private lateinit var accountFieldTypeToEdit: AccountFieldType

    companion object {
        const val TAG = "clg:EditString"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStringBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CommonUIBehaviors.applySecureFlag(this)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)!!
        accountFieldTypeToEdit = intent.serializableExtraCompat<AccountFieldType>(EXTRA_ACCOUNT_FIELD_TYPE)!!

        title = resources.getString(R.string.activity_label_edit_entry_prefix) + " " +
                when (accountFieldTypeToEdit) {
                    AccountFieldType.accountName -> resources.getString(R.string.title_account_name)
                    AccountFieldType.username -> resources.getString(R.string.title_username)
                    AccountFieldType.email -> resources.getString(R.string.title_email)
                    AccountFieldType.phone -> resources.getString(R.string.title_phone)
                    AccountFieldType.password -> resources.getString(R.string.title_password)
                    else -> resources.getString(R.string.error)
                } + " for $accountName"

        etNewValue = binding.etNewValue
        // For passwords, use a visible-password input type so the soft keyboard does not
        // store the value in its suggestion dictionary (the field stays readable on screen).
        if (accountFieldTypeToEdit == AccountFieldType.password) {
            etNewValue.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            etNewValue.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        binding.tvPreviousValue.text = Manager.getAccountFieldValue(accountName, accountFieldTypeToEdit)
        binding.btnSubmit.setOnClickListener {
            handleInputConfirmation()
        }

        etNewValue.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> handleInputConfirmation()
                else -> false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleInputConfirmation() : Boolean {
        if (saveNewValue()) {
            this@EditStringActivity.finish()
        }
        return true
    }

    private fun saveNewValue(): Boolean {
        val newValue = etNewValue.text.toString()
        // Reject renaming an account onto an existing account name (would create a duplicate).
        if (accountFieldTypeToEdit == AccountFieldType.accountName &&
            newValue != accountName && Manager.ifAccountNameExists(newValue)) {
            Toast.makeText(
                this,
                getString(R.string.toast_account_name_exists, newValue),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return Manager.editStringFieldValue(accountName, accountFieldTypeToEdit, newValue)
    }

    override fun onResume() {
        super.onResume()
        CommonUIBehaviors.focusViewAndShowKeyboard(etNewValue, this)
    }
}
package com.example.passwordmanagerv1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.passwordmanagerv1.utils.AccountField
import com.example.passwordmanagerv1.utils.CommonUIBehaviors
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT_NAME
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT_FIELD

class EditStringActivity : AppCompatActivity() {

    private lateinit var etNewValue: EditText
    private lateinit var accountName: String
    private lateinit var accountFieldToEdit: AccountField

    companion object {
        const val TAG = "debug EditString"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_string)

        accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)!!
        accountFieldToEdit = intent.getSerializableExtra(EXTRA_ACCOUNT_FIELD) as AccountField

        etNewValue = findViewById(R.id.etNewValue)
        val tvPreviousValue = findViewById<TextView>(R.id.tvPreviousValue)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        tvPreviousValue.text = Manager.getAccountFieldValue(accountName, accountFieldToEdit)
        btnSubmit.setOnClickListener {
            handleInputConfirmation()
        }

        etNewValue.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> handleInputConfirmation()
                else -> false
            }
        }
    }

    private fun handleInputConfirmation() : Boolean {
        saveNewValue()
        this@EditStringActivity.finish()
        return true
    }

    private fun saveNewValue() {
        val newValue = etNewValue.text.toString()
        Manager.editStringFieldValue(accountName, accountFieldToEdit, newValue)
    }

    override fun onResume() {
        super.onResume()
        CommonUIBehaviors.focusViewAndShowKeyboard(etNewValue, this)
    }
}
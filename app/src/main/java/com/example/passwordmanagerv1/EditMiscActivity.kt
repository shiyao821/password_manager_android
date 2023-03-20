package com.example.passwordmanagerv1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.passwordmanagerv1.utils.CommonUIBehaviors
import com.example.passwordmanagerv1.utils.EXTRA_ACCOUNT_NAME
import com.example.passwordmanagerv1.utils.EXTRA_MISC_FIELD_TITLE

class EditMiscActivity : AppCompatActivity() {

    private lateinit var accountName: String
    private lateinit var miscTitle: String

    private lateinit var etNewTitle: EditText
    private lateinit var etNewValue: EditText
    private lateinit var btnSubmit: Button

    companion object {
        const val TAG = "clg:EditMisc"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_misc)

        accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)!!
        miscTitle = intent.getStringExtra(EXTRA_MISC_FIELD_TITLE) ?: ""

        etNewTitle = findViewById(R.id.etNewTitle)
        etNewValue = findViewById(R.id.etNewValue)
        btnSubmit = findViewById(R.id.btnSubmit)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val previousValue = Manager.getMiscValue(accountName, miscTitle)
        etNewTitle.setText(miscTitle)
        etNewValue.setText(previousValue)
        etNewTitle.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    etNewValue.requestFocus()
                    true
                } else -> false
            }
        }
        btnSubmit.setOnClickListener {
            onSubmit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_misc, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miDeleteMisc -> {
                AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.alert_title_misc_delete_confirmation))
                    .setPositiveButton(resources.getString(R.string.button_delete)) { _,_ ->
                        Manager.deleteMiscEntry(accountName, miscTitle)
                        this@EditMiscActivity.finish()
                    }
                    .setNegativeButton(resources.getString(R.string.button_cancel)) { _,_ -> }
                    .show()
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {}
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        if (miscTitle != "") {
            CommonUIBehaviors.focusViewAndShowKeyboard(etNewValue, this)
        } else {
            CommonUIBehaviors.focusViewAndShowKeyboard(etNewTitle, this)
        }
    }

    private fun onSubmit() {
        // validation
        val newTitle = etNewTitle.text.toString()
        val newValue = etNewValue.text.toString()

        if (newTitle == "" || newValue == "") {
            AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.alert_title_misc_title_value_empty))
                .setMessage(resources.getString(R.string.alert_message_misc_title_value_empty))
                .setPositiveButton(resources.getString(R.string.button_delete)) { _,_ ->
                    Manager.deleteMiscEntry(accountName, miscTitle)
                    this@EditMiscActivity.finish()
                }
                .setNegativeButton(resources.getString(R.string.button_cancel)) { _,_ -> }
                .show()
            return
        }

        if (newTitle != miscTitle && Manager.hasMiscTitle(accountName, newTitle)) {
            val message = "To edit $newTitle, go back and select it from the account's overview page"
            AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.alert_title_misc_title_exists))
                .setMessage(message)
                .setPositiveButton(resources.getString(R.string.button_acknowledge)) { _,_ -> }
                .show()
            return
        }

        Manager.editMiscValue(accountName, miscTitle,
            etNewTitle.text.toString(),
            etNewValue.text.toString())
        this@EditMiscActivity.finish()
    }
}
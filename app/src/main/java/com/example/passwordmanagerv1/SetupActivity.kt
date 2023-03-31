package com.example.passwordmanagerv1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class SetupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "clg:Setup"
    }

    private lateinit var ettpAppPasswordSetup: EditText
    private lateinit var ettpAppPasswordSetupConfirmation: EditText
    private lateinit var btnSetup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        ettpAppPasswordSetup = findViewById(R.id.ettpAppPasswordSetup)
        ettpAppPasswordSetupConfirmation = findViewById(R.id.ettpAppPasswordSetupConfirmation)
        btnSetup = findViewById(R.id.btnSetup)

        btnSetup.setOnClickListener{
            validatePasswordInputs()
        }

        ettpAppPasswordSetup.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    ettpAppPasswordSetupConfirmation.requestFocus()
                    true
                } else -> false
            }
        }
        ettpAppPasswordSetupConfirmation.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    validatePasswordInputs()
                    true
                } else -> false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validatePasswordInputs() {
        val passwordInput = ettpAppPasswordSetup.text.toString()
        val confirmPasswordInput = ettpAppPasswordSetup.text.toString()
        if (!isValidPassword(passwordInput)) {
            Toast.makeText(this, R.string.toast_invalid_password, Toast.LENGTH_SHORT).show()
        }
        if (passwordInput != confirmPasswordInput) {
            Toast.makeText(this, R.string.toast_mismatching_password, Toast.LENGTH_SHORT).show()
        }

        if (Manager.createNewDataFile(passwordInput)) {
            Log.i(TAG, "New password set up")
            AlertDialog.Builder(this)
                .setTitle(R.string.alert_title_master_password_created)
                .setMessage(R.string.alert_message_master_password_created)
                .setPositiveButton(R.string.button_acknowledge){ _, _ ->
                    val resultData = Intent()
                    setResult(Activity.RESULT_OK, resultData)
                    finish()
            }.show()
        }
    }

    private fun isValidPassword(password: String): Boolean {
        return true
        TODO("add regex")
    }
}
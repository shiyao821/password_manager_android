package com.furytoar.passwordmanager

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
import com.furytoar.passwordmanager.databinding.ActivitySetupBinding
import com.furytoar.passwordmanager.utils.CommonUIBehaviors

class SetupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "clg:Setup"
    }

    private lateinit var binding: ActivitySetupBinding
    private lateinit var ettpAppPasswordSetup: EditText
    private lateinit var ettpAppPasswordSetupConfirmation: EditText
    private lateinit var btnSetup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CommonUIBehaviors.applySecureFlag(this)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        ettpAppPasswordSetup = binding.ettpAppPasswordSetup
        ettpAppPasswordSetupConfirmation = binding.ettpAppPasswordSetupConfirmation
        btnSetup = binding.btnSetup

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
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validatePasswordInputs() {
        val passwordInput = ettpAppPasswordSetup.text.toString()
        val confirmPasswordInput = ettpAppPasswordSetupConfirmation.text.toString()
        if (!isValidPassword(passwordInput)) {
            Toast.makeText(this, R.string.toast_invalid_password, Toast.LENGTH_SHORT).show()
            return
        }
        if (passwordInput != confirmPasswordInput) {
            Toast.makeText(this, R.string.toast_mismatching_password, Toast.LENGTH_SHORT).show()
            return
        }

        // Creating the vault derives the Argon2id key, which is expensive: run it off the UI
        // thread to avoid an ANR.
        val originalButtonText = btnSetup.text
        btnSetup.isEnabled = false
        btnSetup.text = getString(R.string.status_working)
        Thread {
            val created = Manager.createNewDataFile(passwordInput)
            runOnUiThread {
                if (created) {
                    Log.i(TAG, "New password set up")
                    AlertDialog.Builder(this)
                        .setTitle(R.string.alert_title_master_password_created)
                        .setMessage(R.string.alert_message_master_password_created)
                        .setPositiveButton(R.string.button_acknowledge) { _, _ ->
                            setResult(Activity.RESULT_OK, Intent())
                            finish()
                        }.show()
                } else {
                    btnSetup.isEnabled = true
                    btnSetup.text = originalButtonText
                    Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun isValidPassword(password: String): Boolean {
        // TODO: enforce password strength requirements (length / character classes).
        return true
    }
}
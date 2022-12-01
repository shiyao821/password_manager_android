package com.example.passwordmanagerv1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class SetupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Setup_Activity"
    }

    private lateinit var ettpAppPasswordSetup: EditText
    private lateinit var ettpAppPasswordSetupConfirmation: EditText
    private lateinit var btnSetup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

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

    private fun validatePasswordInputs() {
        val passwordInput = ettpAppPasswordSetup.text.toString()
        val confirmPasswordInput = ettpAppPasswordSetup.text.toString()
        if (isValidPassword(passwordInput) &&
            passwordInput == confirmPasswordInput) {
            if (Manager.createNewDataFile(passwordInput)) {
                Log.i(TAG, "New password set up")
                AlertDialog.Builder(this)
                    .setTitle("A new data file and corresponding master password has been created")
                    .setPositiveButton("OK"){ _,_ ->
                        val resultData = Intent()
                        setResult(Activity.RESULT_OK, resultData)
                        finish()
                    }.show()
            }
        }
    }

    private fun isValidPassword(password: String): Boolean {
        return true
        TODO("add regex")
    }
}
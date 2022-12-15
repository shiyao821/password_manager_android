package com.example.passwordmanagerv1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.passwordmanagerv1.utils.CommonUIBehaviors

class SecurityActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var ettpAppPassword: EditText
    private lateinit var manager: Manager

    companion object {
        private const val TAG = "clg:Security"
        private const val SETUP_PASSWORD_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        btnLogin = findViewById(R.id.btnLogin)
        ettpAppPassword = findViewById(R.id.ettpAppPasswordLogin)

        btnLogin.setOnClickListener{
            Log.i(TAG, "btnLogin clicked")
        }

        manager = Manager
        manager.setApplicationContext(this.applicationContext)
        btnLogin.setOnClickListener{
            login(ettpAppPassword.text.toString())
        }
        ettpAppPassword.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    login(ettpAppPassword.text.toString())
                    true
                }
                else -> false
            }
        }
        login("debugging")
    }

    private fun passwordValidation(): Boolean {
        return true
        TODO("Not yet implemented")
    }

    private fun login(password: String) {
        Log.i(TAG, "Attempting to log in")
        if (!passwordValidation()) {
            Log.i(TAG, "Incorrect password")
        }
        if (!manager.checkDataFile()) {
            // Likely first time starting app
            // setup app master password
            val intent = Intent(this, SetupActivity::class.java)
            startActivityForResult(intent, SETUP_PASSWORD_CODE)
            return
        }
        if (!manager.loadData(true)) {
            Toast.makeText(this, R.string.toast_data_load_failure, Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETUP_PASSWORD_CODE && resultCode != Activity.RESULT_OK || data == null) {
            Log.w(TAG, "Did not receive data from launched setup activity")
        }
    }

    override fun onResume() {
        super.onResume()
        CommonUIBehaviors.focusViewAndShowKeyboard(ettpAppPassword, this)
    }
}
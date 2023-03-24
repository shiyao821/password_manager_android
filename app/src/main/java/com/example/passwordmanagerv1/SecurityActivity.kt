package com.example.passwordmanagerv1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.passwordmanagerv1.utils.CommonUIBehaviors
import com.example.passwordmanagerv1.utils.EXTRA_IMPORT_DATA_URI
import com.example.passwordmanagerv1.utils.EXTRA_VERIFICATION

class SecurityActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var ettpAppPassword: EditText
    private lateinit var tvEnterPassword: TextView
    private lateinit var manager: Manager
    private var importingData: Uri? = null
    private var isVerificationOnly = false

    companion object {
        private const val TAG = "clg:Security"
        private const val SETUP_PASSWORD_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        importingData = intent.getParcelableExtra(EXTRA_IMPORT_DATA_URI)
        isVerificationOnly = intent.getBooleanExtra(EXTRA_VERIFICATION, false) == true
        Log.i(TAG, "is $isVerificationOnly")

        btnLogin = findViewById(R.id.btnLogin)
        ettpAppPassword = findViewById(R.id.ettpAppPasswordLogin)
        tvEnterPassword = findViewById(R.id.tvEnterPassword)

        btnLogin.setOnClickListener{
            Log.i(TAG, "btnLogin clicked")
        }

        manager = Manager
        manager.setApplicationContext(this.applicationContext)
        btnLogin.setOnClickListener{
            login(ettpAppPassword.text.toString())
        }
        ettpAppPassword.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    login(ettpAppPassword.text.toString())
                    true
                }
                else -> false
            }
        }
        if (importingData != null) {
            tvEnterPassword.text = resources.getString(R.string.prompt_import_data_password_enter)
        }

    }

    private fun login(password: String) {
        if (isVerificationOnly) {
            Log.i(TAG, "verifying password")
            val verificationResult = Manager.verifyPassword(password)
            val returningData = Intent().putExtra(EXTRA_VERIFICATION, verificationResult)
            setResult(Activity.RESULT_OK, returningData)
            this@SecurityActivity.finish()
            return
        }

        Log.i(TAG, "Attempting to log in")
        if (!manager.checkDataFile()) {
            // Likely first time starting app
            // setup app master password
            val intent = Intent(this, SetupActivity::class.java)
            startActivityForResult(intent, SETUP_PASSWORD_CODE)
            return
        }
        if (importingData != null) {
            val inputStream = contentResolver.openInputStream(importingData!!)
            if (manager.loadData(inputStream, password, false)) {
                Toast.makeText(this, resources.getString(R.string.toast_import_data_success), Toast.LENGTH_SHORT).show()
                manager.saveData()
            } else {
                Toast.makeText(this, resources.getString(R.string.toast_import_data_failure), Toast.LENGTH_SHORT).show()
            }
            inputStream?.close()
            finish()
        } else {
            if (!manager.loadData(null, password, false)) {
                Toast.makeText(this, R.string.toast_invalid_password, Toast.LENGTH_LONG).show()
                return
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
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
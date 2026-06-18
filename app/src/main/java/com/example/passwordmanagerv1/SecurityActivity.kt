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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.IntentCompat
import com.example.passwordmanagerv1.utils.CommonUIBehaviors
import com.example.passwordmanagerv1.utils.EXTRA_IMPORT_DATA_URI
import com.example.passwordmanagerv1.utils.EXTRA_VERIFICATION


class SecurityActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var ettpAppPassword: EditText
    private lateinit var tvEnterPassword: TextView
    private lateinit var manager: Manager
    private lateinit var biometricAuthenticatorCallback: BiometricPrompt.AuthenticationCallback
    private var importingData: Uri? = null
    private var isVerificationOnly = false

    companion object {
        private const val TAG = "clg:Security"
        private const val SETUP_PASSWORD_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)
        CommonUIBehaviors.applySecureFlag(this)

        // This activity is exported (it is the launcher). Only honor the import/verification
        // extras when we were started from within the app, so another app cannot drive these
        // flows by sending crafted extras.
        val launchedInternally = callingPackage == packageName
        importingData = if (launchedInternally) {
            IntentCompat.getParcelableExtra(intent, EXTRA_IMPORT_DATA_URI, Uri::class.java)
        } else null
        isVerificationOnly = launchedInternally && intent.getBooleanExtra(EXTRA_VERIFICATION, false)
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

        biometricAuthenticatorCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int,
                                               errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(TAG, "Authentication error: $errString, $errorCode")
            }

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                dataDecryption(password)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@SecurityActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "biometrics failed")
                moveTaskToBack(true)
            }
        }
        Authenticator.authenticate(this, biometricAuthenticatorCallback)
    }

    private fun dataDecryption(password: String) {
        if (!manager.checkDataFile()) {
            // Likely first time starting app
            // setup app master password
            val intent = Intent(this, SetupActivity::class.java)
            startActivityForResult(intent, SETUP_PASSWORD_CODE)
            return
        }
        // Key derivation (Argon2id) and file I/O are expensive and must not run on the UI
        // thread, or the app will ANR. Do the work on a background thread and update UI on
        // completion.
        setLoading(true)
        if (importingData != null) {
            runImport(password)
        } else {
            runLogin(password)
        }
    }

    private fun runImport(password: String) {
        Thread {
            val inputStream = contentResolver.openInputStream(importingData!!)
            val success = manager.loadData(inputStream, password)
            if (success) {
                manager.saveData() // persist merged data in the current encrypted format
            }
            inputStream?.close()
            val wasLegacy = manager.importedLegacyFormat
            runOnUiThread {
                setLoading(false)
                when {
                    success && wasLegacy -> {
                        AlertDialog.Builder(this)
                            .setTitle(R.string.alert_title_legacy_import)
                            .setMessage(R.string.alert_message_legacy_import)
                            .setPositiveButton(R.string.button_acknowledge) { _, _ -> finish() }
                            .setOnDismissListener { finish() }
                            .show()
                    }
                    success -> {
                        Toast.makeText(this, R.string.toast_import_data_success, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else -> {
                        Toast.makeText(this, R.string.toast_import_data_failure, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }.start()
    }

    private fun runLogin(password: String) {
        Thread {
            val success = manager.loadData(null, password)
            if (success && manager.loadedFromLegacyStorage()) {
                // Migrate legacy data to the current format and warm the key cache now, so
                // subsequent edits don't run Argon2id on the UI thread.
                manager.saveData()
            }
            runOnUiThread {
                if (!success) {
                    setLoading(false)
                    Toast.makeText(this, R.string.toast_invalid_password, Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }.start()
    }

    private fun setLoading(loading: Boolean) {
        btnLogin.isEnabled = !loading
        ettpAppPassword.isEnabled = !loading
        btnLogin.text = getString(if (loading) R.string.button_unlocking else R.string.button_confirm)
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
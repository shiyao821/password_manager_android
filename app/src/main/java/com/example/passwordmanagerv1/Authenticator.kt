package com.example.passwordmanagerv1

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor


object Authenticator {
    private const val TAG = "clg:Auth"
    private const val REQUEST_CODE_BIOMETRIC = 2
    private var isBiometricsSetUp = false

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun isBiometricAuthenticationSetup(context : Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")
                isBiometricsSetUp = true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e(TAG, "No biometric features available on this device.")
                isBiometricsSetUp = false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e(TAG, "Biometric features are currently unavailable.")
                isBiometricsSetUp = false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.alert_title_biometric_setup))
                    .setMessage(context.getString(R.string.alert_message_biometric_setup))
                    .setNegativeButton(context.getString(R.string.button_cancel)) { _,_ -> }
                    .setPositiveButton(context.getString(R.string.button_acknowledge)) { _,_ ->
                        val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                            )
                        }
                        startActivityForResult(context as Activity,
                            enrollIntent, REQUEST_CODE_BIOMETRIC, null)
                    }
                    .show()
            }
        }
        return isBiometricsSetUp
    }

    fun authenticate(context: Context, callback: BiometricPrompt.AuthenticationCallback) {
        if (!isBiometricAuthenticationSetup(context)) {
            Toast.makeText(context, context.getString(R.string.toast_reminder_biometrics_setup),
                Toast.LENGTH_SHORT).show()
        }
        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(context as FragmentActivity, executor, callback)

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.alert_title_biometric_verification))
            .setSubtitle(context.getString(R.string.alert_subtitle_biometric_verification))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText(context.getString(R.string.button_cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

}
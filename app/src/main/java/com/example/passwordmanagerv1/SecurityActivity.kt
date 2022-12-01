package com.example.passwordmanagerv1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.passwordmanagerv1.utils.MANAGER

class SecurityActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var ettpAppPassword: EditText
    private lateinit var manager: Manager

    companion object {
        private const val TAG = "Security Activity"
        private const val SETUP_PASSWORD_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        btnLogin = findViewById(R.id.btnLogin)
        ettpAppPassword = findViewById(R.id.ettpAppPasswordLogin)

        ettpAppPassword.setOnClickListener{
            Log.i(TAG, "btnLogin clicked")
        }

        manager = Manager
        // Likely first time starting app
        if (!manager.checkDataFile()) {
            // setup app master password
            val intent = Intent(this, SetupActivity::class.java)
            startActivityForResult(intent, SETUP_PASSWORD_CODE)


        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()

        ettpAppPassword.requestFocus()
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }
}
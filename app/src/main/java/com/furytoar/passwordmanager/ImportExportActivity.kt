package com.furytoar.passwordmanager

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.furytoar.passwordmanager.databinding.ActivityImportExportBinding
import com.furytoar.passwordmanager.utils.*


class ImportExportActivity : AppCompatActivity() {

    companion object {
        const val TAG = "clg:ImportExport"
    }

    private lateinit var binding: ActivityImportExportBinding
    private lateinit var btnImportData: Button
    private lateinit var btnExportData: Button
    private lateinit var verificationLauncherForImport: ActivityResultLauncher<Intent>
    private lateinit var verificationLauncherForExport: ActivityResultLauncher<Intent>
    private lateinit var importDecryptionLauncher: ActivityResultLauncher<Intent>
    private lateinit var chooseFileLauncher: ActivityResultLauncher<Intent>
    private lateinit var createFileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportExportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CommonUIBehaviors.applySecureFlag(this)

        btnImportData = binding.btnImportData
        btnExportData = binding.btnExportData

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        btnImportData.setOnClickListener {
            verifyIdentityForImport()
        }
        btnExportData.setOnClickListener {
            verifyIdentityForExport()
        }
        verificationLauncherForImport = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.i(TAG, "returned from verification")
            if (it.resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "result OK")
                if(it.data?.getBooleanExtra(EXTRA_VERIFICATION, false) == true) {
                    launchIntentForFileImport()
                } else {
                    Toast.makeText(this, R.string.toast_invalid_password, Toast.LENGTH_SHORT).show()
                }
            }
        }
        verificationLauncherForExport = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                if(it.data?.getBooleanExtra(EXTRA_VERIFICATION, false) == true) {
                    launchIntentForFileExport()
                } else {
                    Toast.makeText(this, R.string.toast_invalid_password, Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Launched for-result (rather than plain startActivity) so SecurityActivity sees a
        // non-null callingPackage and will honor the import-data extra. Result is unused.
        importDecryptionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { }
        chooseFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                Log.w(TAG, "File choose cancelled: ${result.resultCode}")
                return@registerForActivityResult
            }
            val uri = result.data?.data
            if (uri == null) {
                Log.w(TAG, "Did not receive chosen file from launched activity")
                return@registerForActivityResult
            }
            importData(uri)
        }
        createFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                Log.w(TAG, "File create cancelled: ${result.resultCode}")
                return@registerForActivityResult
            }
            val uri = result.data?.data
            if (uri == null) {
                Log.w(TAG, "Did not receive created file from launched activity")
                return@registerForActivityResult
            }
            exportData(uri)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchIntentForFileImport() {
        Log.i(TAG, "Launching file import intent")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        val chooserIntent: Intent = Intent.createChooser(intent, "Open file")

        try {
            chooseFileLauncher.launch(chooserIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                applicationContext,
                "No suitable File Manager was found.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun launchIntentForFileExport() {
        Log.i(TAG, "Starting file export")
        val uri = MediaStore.VOLUME_EXTERNAL_PRIMARY.toUri()
        createFile(uri)
    }

    private fun createFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = DATAFILE_MIMETYPE_BINARY
            putExtra(Intent.EXTRA_TITLE, DATAFILE_NAME_AND_EXTENSION)

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        createFileLauncher.launch(intent)
    }

    private fun exportData(uri: Uri) {
        val outputStream = contentResolver.openOutputStream(uri)
        if (outputStream == null) {
            Log.e(TAG, "Error in opening output stream")
            Toast.makeText(this, resources.getString(R.string.toast_export_data_failure), Toast.LENGTH_SHORT).show()
            return
        }
        // Export encrypts (Argon2id KDF) and writes the file; do it off the UI thread.
        Thread {
            val success = Manager.exportData(outputStream)
            outputStream.close()
            runOnUiThread {
                val message = if (success) R.string.toast_export_data_success
                else R.string.toast_export_data_failure
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun importData(uri: Uri) {
        // check file type
        val mimeType: String? = uri.let { returnUri ->
            contentResolver.getType(returnUri)
        }
        if (mimeType != DATAFILE_MIMETYPE_BINARY && mimeType != DATAFILE_MIMETYPE_PLAIN) {
            Log.w(TAG, "MIME type of chosen file $mimeType does not match" +
                    " expected $DATAFILE_MIMETYPE_BINARY or $DATAFILE_MIMETYPE_PLAIN")
            return
        }
        launchSecurityActivityForDecryption(uri)
    }

    private fun launchSecurityActivityForDecryption(uri: Uri) {
        val intent = Intent(this, SecurityActivity::class.java)
        intent.putExtra(EXTRA_IMPORT_DATA_URI, uri)
        importDecryptionLauncher.launch(intent)
    }

    private fun verifyIdentityForImport() {
        Log.i(TAG, "before intent for verification launch")
        verificationLauncherForImport.launch(
            Intent(this, SecurityActivity::class.java)
                .putExtra(EXTRA_VERIFICATION, true))
        Log.i(TAG, "intent for verification launched")
    }

    private fun verifyIdentityForExport() {
        Log.i(TAG, "before intent for verification launch")
        verificationLauncherForExport.launch(
            Intent(this, SecurityActivity::class.java)
                .putExtra(EXTRA_VERIFICATION, true))
        Log.i(TAG, "intent for verification launched")
    }
}
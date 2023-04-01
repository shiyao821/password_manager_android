package com.example.passwordmanagerv1

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.passwordmanagerv1.utils.*


class ImportExportActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 20
        const val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 30
        const val INTENT_REQUEST_CHOOSE_FILE = 40
        const val INTENT_REQUEST_CREATE_FILE = 50
        const val TAG = "clg:ImportExport"
    }

    private lateinit var btnImportData: Button
    private lateinit var btnExportData: Button
    private lateinit var verificationLauncherForImport: ActivityResultLauncher<Intent>
    private lateinit var verificationLauncherForExport: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_export)

        btnImportData = findViewById(R.id.btnImportData)
        btnExportData = findViewById(R.id.btnExportData)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        btnImportData.setOnClickListener {
            if (isPermissionGranted(this@ImportExportActivity, READ_EXTERNAL_STORAGE)) {
                verifyIdentityForImport()
            } else {
                Log.i(TAG, "Requesting Permission")
                requestPermission(this@ImportExportActivity,
                    READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
            }
        }
        btnExportData.setOnClickListener {
            if (isPermissionGranted(this@ImportExportActivity, WRITE_EXTERNAL_STORAGE)) {
                verifyIdentityForExport()
            } else {
                Log.i(TAG, "Requesting Permission")
                requestPermission(this@ImportExportActivity,
                    WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)
            }
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchIntentForFileImport() {
        Log.i(TAG, "Launching file import intent")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // special intent for Samsung file manager
        val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
        sIntent.addCategory(Intent.CATEGORY_DEFAULT)

        val chooserIntent: Intent
        if (packageManager.resolveActivity(sIntent, 0) != null) {
            // it is device with Samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intent))
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file")
        }

        try {
            startActivityForResult(chooserIntent, INTENT_REQUEST_CHOOSE_FILE)
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
        startActivityForResult(intent, INTENT_REQUEST_CREATE_FILE)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE -> verifyIdentityForImport()
                PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE -> verifyIdentityForExport()
                else -> {}
            }
        } else {
            Log.w(TAG, "$requestCode, ${permissions[0]}, ${grantResults[0]}")
            AlertDialog.Builder(this)
                .setTitle(R.string.alert_title_permission_denied)
                .setMessage(R.string.alert_message_external_storage_permissions_needed)
                .setPositiveButton(R.string.button_acknowledge) { _,_ -> }
                .show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            Log.w(TAG, "Result gone wrong: $resultCode | Data: $data")
            return
        }
        if (requestCode != INTENT_REQUEST_CHOOSE_FILE && requestCode != INTENT_REQUEST_CREATE_FILE) {
            Log.w(TAG, "Error with request code: $requestCode")
            return
        }
        if (requestCode == INTENT_REQUEST_CHOOSE_FILE) {
            val uri = data.data
            if (uri == null) {
                Log.w(TAG, "Did not receive chosen file from launched activity: $resultCode")
                return
            }
            importData(uri)
        }
        if (requestCode == INTENT_REQUEST_CREATE_FILE) {
            val uri = data.data
            if (uri == null) {
                Log.w(TAG, "Did not receive file from launched activity: $resultCode")
                return
            }
            exportData(uri)
        }
    }

    private fun exportData(uri: Uri) {
        val outputStream = contentResolver.openOutputStream(uri)
        if (outputStream == null) {
            Log.e(TAG, "Error in opening output stream")
        }
        if (Manager.exportData(outputStream!!)) {
            Toast.makeText(this, resources.getString(R.string.toast_export_data_success), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, resources.getString(R.string.toast_export_data_failure), Toast.LENGTH_SHORT).show()
        }
        outputStream.close()
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
        startActivity(intent)
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
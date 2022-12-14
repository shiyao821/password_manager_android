package com.example.passwordmanagerv1.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.passwordmanagerv1.R

class CommonUIBehaviors {
    companion object {
        fun focusViewAndShowKeyboard(view: View, activity: Activity) {
            if (view.requestFocus()) {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fun copyToClipboard(context: Context, textToCopy: String, textName: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText(LABEL_COPY, textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context,
                textName + " " + context.resources.getString(R.string.toast_copy_suffix),
                Toast.LENGTH_SHORT).show()
        }
    }
}
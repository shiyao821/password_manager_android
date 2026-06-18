package com.furytoar.passwordmanager.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.furytoar.passwordmanager.R

class CommonUIBehaviors {
    companion object {
        fun focusViewAndShowKeyboard(view: View, context: Context) {
            if (view.requestFocus()) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fun hideKeyboard(view: View, context: Context) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * Prevents the window contents (which may display secrets) from appearing in
         * screenshots, screen recordings and the recent-apps thumbnail.
         */
        fun applySecureFlag(activity: Activity) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        fun copyToClipboard(context: Context, textToCopy: String, textName: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText(LABEL_COPY, textToCopy)
            // Flag the content as sensitive so the OS does not show a clipboard preview.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                clip.description.extras = PersistableBundle().apply {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                }
            }
            clipboard.setPrimaryClip(clip)

            // Auto-clear the clipboard after a delay, but only if it still holds our value.
            Handler(Looper.getMainLooper()).postDelayed({
                val current = clipboard.primaryClip
                val stillOurs = current != null && current.itemCount > 0 &&
                        current.getItemAt(0).text?.toString() == textToCopy
                if (stillOurs) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        clipboard.clearPrimaryClip()
                    } else {
                        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                    }
                }
            }, CLIPBOARD_CLEAR_DELAY_MS)

            Toast.makeText(context,
                textName + " " + context.resources.getString(R.string.toast_copy_suffix),
                Toast.LENGTH_SHORT).show()
        }
    }
}

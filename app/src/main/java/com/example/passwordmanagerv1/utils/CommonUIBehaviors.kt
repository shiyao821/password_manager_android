package com.example.passwordmanagerv1.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService

class CommonUIBehaviors {
    companion object {
        fun focusViewAndShowKeyboard(view: View, activity: Activity) {
            if (view.requestFocus()) {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}
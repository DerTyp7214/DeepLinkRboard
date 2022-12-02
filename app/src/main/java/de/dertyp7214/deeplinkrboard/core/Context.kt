package de.dertyp7214.deeplinkrboard.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.os.postDelayed

fun Context.getAttr(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

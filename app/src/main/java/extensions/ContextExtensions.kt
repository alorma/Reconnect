package extensions

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

fun Context.preferences(): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(this)
}

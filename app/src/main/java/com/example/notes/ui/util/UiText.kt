package com.example.notes.ui.util

import android.content.Context
import androidx.annotation.StringRes

/** Текст, который ViewModel может передать в UI, не зная про Context. */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText

    fun asString(context: Context): String = when (this) {
        is Dynamic -> value
        is StringResource -> context.getString(resId, *args)
    }
}

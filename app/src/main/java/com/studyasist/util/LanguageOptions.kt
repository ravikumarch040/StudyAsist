package com.studyasist.util

import com.studyasist.R

object LanguageOptions {
    /** List of (language code, string resource ID for display name) */
    val LIST: List<Pair<String, Int>> = listOf(
        "en" to R.string.lang_english,
        "hi" to R.string.lang_hindi,
        "es" to R.string.lang_spanish,
        "fr" to R.string.lang_french,
        "de" to R.string.lang_german
    )
}

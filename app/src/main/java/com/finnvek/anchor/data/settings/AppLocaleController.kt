package com.finnvek.anchor.data.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject

const val ANCHOR_LOCALE_ENGLISH = "en"
const val ANCHOR_LOCALE_FINNISH = "fi"

interface AppLocaleController {
    fun applyLocale(localeTag: String?)
}

class AppCompatAppLocaleController @Inject constructor() : AppLocaleController {

    override fun applyLocale(localeTag: String?) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(localeTag.orEmpty()),
        )
    }
}

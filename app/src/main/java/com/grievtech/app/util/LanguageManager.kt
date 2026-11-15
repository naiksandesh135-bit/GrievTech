package com.grievtech.app.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.grievtech.app.data.preference.UserPreferencesRepository
import com.grievtech.app.data.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
)

@Singleton
class LanguageManager @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val profileRepository: ProfileRepository
) {
    val supportedLanguages = listOf(
        AppLanguage("en", "English", "English"),
        AppLanguage("kn", "ಕನ್ನಡ", "Kannada"),
        AppLanguage("hi", "हिन्दी", "Hindi"),
        AppLanguage("ta", "தமிழ்", "Tamil"),
        AppLanguage("te", "తెలుగు", "Telugu")
    )

    private val externalScope = CoroutineScope(Dispatchers.Main)

    val currentLanguageCode: Flow<String> = preferencesRepository.preferredLanguage

    val hasSeenOnboarding: Flow<Boolean> = preferencesRepository.hasSeenOnboarding

    fun setLanguage(code: String) {
        externalScope.launch {
            val appLocales = LocaleListCompat.forLanguageTags(code)
            AppCompatDelegate.setApplicationLocales(appLocales)

            preferencesRepository.saveLanguage(code)

            try {
                profileRepository.updateLanguage(code).first()
            } catch (_: Exception) {}
        }
    }

    fun applySavedLanguage() {
        externalScope.launch {
            val code = preferencesRepository.preferredLanguage.first()
            val appLocales = LocaleListCompat.forLanguageTags(code)
            AppCompatDelegate.setApplicationLocales(appLocales)
        }
    }

    fun setHasSeenOnboarding(hasSeen: Boolean) {
        externalScope.launch {
            preferencesRepository.saveHasSeenOnboarding(hasSeen)
        }
    }
}

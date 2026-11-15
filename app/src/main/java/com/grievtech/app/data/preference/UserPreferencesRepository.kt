package com.grievtech.app.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyLanguage = stringPreferencesKey("preferred_language")
    private val keyHasSeenOnboarding = booleanPreferencesKey("has_seen_onboarding")

    val preferredLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[keyLanguage] ?: "en"
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyHasSeenOnboarding] ?: false
    }

    suspend fun saveLanguage(code: String) {
        context.dataStore.edit { prefs ->
            prefs[keyLanguage] = code
        }
    }

    suspend fun saveHasSeenOnboarding(hasSeen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[keyHasSeenOnboarding] = hasSeen
        }
    }
}

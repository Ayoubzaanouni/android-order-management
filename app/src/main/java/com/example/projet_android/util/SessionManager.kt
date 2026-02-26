package com.example.projet_android.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

sealed class SessionState {
    data object NoSession : SessionState()
    data class LoggedIn(val companyId: Int) : SessionState()
}

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_COMPANY_ID = intPreferencesKey("company_id")
    }

    val sessionFlow: Flow<SessionState> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_COMPANY_ID]
        if (id != null) SessionState.LoggedIn(id) else SessionState.NoSession
    }
    suspend fun saveSession(companyId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_COMPANY_ID] = companyId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_COMPANY_ID)
        }
    }
}

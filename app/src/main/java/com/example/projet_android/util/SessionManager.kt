package com.example.projet_android.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

sealed class SessionState {
    data object NoSession : SessionState()
    data class LoggedInAdmin(val adminId: Int) : SessionState()
    data class LoggedInCompany(val companyId: Int) : SessionState()
    data class LoggedInEmployee(val employeeId: Int, val companyId: Int) : SessionState()
}

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_ID = intPreferencesKey("id")
        private val KEY_COMPANY_ID = intPreferencesKey("company_id")
    }

    val sessionFlow: Flow<SessionState> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_ROLE]) {
            "ADMIN" -> {
                val id = prefs[KEY_ID]
                if (id != null) SessionState.LoggedInAdmin(id) else SessionState.NoSession
            }
            "COMPANY" -> {
                val id = prefs[KEY_ID]
                if (id != null) SessionState.LoggedInCompany(id) else SessionState.NoSession
            }
            "EMPLOYEE" -> {
                val id = prefs[KEY_ID]
                val companyId = prefs[KEY_COMPANY_ID]
                if (id != null && companyId != null) SessionState.LoggedInEmployee(id, companyId)
                else SessionState.NoSession
            }
            else -> SessionState.NoSession
        }
    }

    suspend fun saveAdminSession(adminId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROLE] = "ADMIN"
            prefs[KEY_ID] = adminId
        }
    }

    suspend fun saveCompanySession(companyId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROLE] = "COMPANY"
            prefs[KEY_ID] = companyId
        }
    }

    suspend fun saveEmployeeSession(employeeId: Int, companyId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROLE] = "EMPLOYEE"
            prefs[KEY_ID] = employeeId
            prefs[KEY_COMPANY_ID] = companyId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_ID)
            prefs.remove(KEY_COMPANY_ID)
        }
    }
}


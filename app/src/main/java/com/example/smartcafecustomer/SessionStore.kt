package com.example.smartcafecustomer

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "cafe_session")

class SessionStore(private val context: Context) {

    private val emailKey = stringPreferencesKey("user_email")
    private val termsKey = booleanPreferencesKey("accepted_terms")

    val userEmailFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[emailKey]
    }

    val acceptedTermsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[termsKey] ?: false
    }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { prefs ->
            prefs[emailKey] = email
        }
    }

    suspend fun clearUserEmail() {
        context.dataStore.edit { prefs ->
            prefs.remove(emailKey)
        }
    }

    suspend fun saveAcceptedTerms(accepted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[termsKey] = accepted
        }
    }
}
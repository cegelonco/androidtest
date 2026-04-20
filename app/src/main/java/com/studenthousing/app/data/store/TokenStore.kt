package com.studenthousing.app.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_store")

class TokenStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("jwt_token")
    private val userTypeKey = stringPreferencesKey("user_type")

    @Volatile
    var cachedToken: String? = null
        private set

    @Volatile
    var cachedUserType: String? = null
        private set

    suspend fun initialize() {
        cachedToken = context.dataStore.data.map { it[tokenKey] }.first()
        cachedUserType = context.dataStore.data.map { it[userTypeKey] }.first()
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
        }
        cachedToken = token
    }

    suspend fun saveUserType(userType: String) {
        context.dataStore.edit { prefs ->
            prefs[userTypeKey] = userType
        }
        cachedUserType = userType
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(userTypeKey)
        }
        cachedToken = null
        cachedUserType = null
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[tokenKey] }.first()
    }
}

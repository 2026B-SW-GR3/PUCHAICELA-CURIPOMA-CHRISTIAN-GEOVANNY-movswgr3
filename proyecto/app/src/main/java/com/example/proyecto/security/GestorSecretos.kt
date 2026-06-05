package com.example.proyecto.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

// Instancia global reactiva
val Context.dataStore by preferencesDataStore(name = "secretos_modernos")

@Singleton
class GestorSecretos @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 1. SharedPreferences (Texto Plano)
    private val sharedPrefs = context.getSharedPreferences("prefs_planas", Context.MODE_PRIVATE)

    // 2. EncryptedSharedPreferences (AES-256 SIV & AES-128 GCM)
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "prefs_cifradas",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun guardarShared(key: String, value: String) = sharedPrefs.edit().putString(key, value).apply()
    fun guardarEncrypted(key: String, value: String) = encryptedPrefs.edit().putString(key, value).apply()
    suspend fun guardarDataStore(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { prefs -> prefs[prefKey] = value }
    }

    fun leerShared(key: String): String? = sharedPrefs.getString(key, null)
    fun leerEncrypted(key: String): String? = encryptedPrefs.getString(key, null)
    suspend fun leerDataStore(key: String): String? {
        val prefKey = stringPreferencesKey(key)
        return context.dataStore.data.map { prefs -> prefs[prefKey] }.firstOrNull()
    }
}
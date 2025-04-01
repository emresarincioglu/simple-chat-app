package com.example.simplechat.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreSource @Inject constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val IV_KEY = stringPreferencesKey("iv")
        private val PUBLIC_KEY = stringPreferencesKey("public_key")
        private val PRIVATE_KEY = stringPreferencesKey("private_key")
    }

    val privateKeyStream = dataStore.data.map { preferences -> preferences[PRIVATE_KEY] }
    val publicKeyStream = dataStore.data.map { preferences -> preferences[PUBLIC_KEY] }
    val ivStream = dataStore.data.map { preferences -> preferences[IV_KEY] }

    suspend fun setPrivateKey(privateKey: String?) {
        dataStore.edit { preferences ->
            if (privateKey == null) {
                preferences.remove(PRIVATE_KEY)
            } else {
                preferences[PRIVATE_KEY] = privateKey
            }
        }
    }

    suspend fun setPublicKey(publicKey: String?) {
        dataStore.edit { preferences ->
            if (publicKey == null) {
                preferences.remove(PUBLIC_KEY)
            } else {
                preferences[PUBLIC_KEY] = publicKey
            }
        }
    }

    suspend fun setIv(iv: String?) {
        dataStore.edit { preferences ->
            if (iv == null) {
                preferences.remove(IV_KEY)
            } else {
                preferences[IV_KEY] = iv
            }
        }
    }

    suspend fun deleteData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
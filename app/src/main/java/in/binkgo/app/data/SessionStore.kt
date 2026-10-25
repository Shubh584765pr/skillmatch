package `in`.binkgo.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")
private val TOKEN = stringPreferencesKey("token")

/** Keeps the login token on disk so the user stays logged in across app restarts. */
class SessionStore(private val context: Context) {
    suspend fun loadToken(): String? = context.dataStore.data.map { it[TOKEN] }.first()

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(TOKEN) }
    }
}

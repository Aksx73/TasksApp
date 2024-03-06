package com.absut.tasksapp.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SortOrder { BY_DUE_DATE, BY_CREATED_DATE, BY_COMPLETED_DATE }

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    val sortOrderFlow: Flow<SortOrder> = dataStore.data
        .map { preferences ->
            SortOrder.valueOf(preferences[SORT_ORDER] ?: SortOrder.BY_CREATED_DATE.name)
        }
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emptyPreferences()
            } else {
                throw exception
            }
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[SORT_ORDER] = sortOrder.name
        }
    }

    companion object {
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }
}
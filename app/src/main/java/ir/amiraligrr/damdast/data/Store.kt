package ir.amiraligrr.damdast.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.damdastDataStore: DataStore<Preferences> by preferencesDataStore(name = "damdast")

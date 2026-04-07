package com.valentincolombat.upnews.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Instance unique du DataStore pour toute l'app — équivalent @AppStorage iOS
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "upnews_prefs")

val ONBOARDING_COMPLETED  = booleanPreferencesKey("has_completed_onboarding")
val REGISTRATION_DATE     = stringPreferencesKey("registration_date")

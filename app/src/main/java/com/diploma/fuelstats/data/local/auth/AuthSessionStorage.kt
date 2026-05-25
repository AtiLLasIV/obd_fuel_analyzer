package com.diploma.fuelstats.data.local.auth

import android.content.Context

class AuthSessionStorage(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )

    fun saveAccessToken(token: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveEmail(email: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun isAuthorized(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_EMAIL)
            .apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_EMAIL = "email"
    }
}
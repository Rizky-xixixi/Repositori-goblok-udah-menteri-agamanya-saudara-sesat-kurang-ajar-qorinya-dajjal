package com.example.ritamesa.data.pref

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ritamesa_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_TOKEN = "auth_token"
        const val KEY_ROLE = "user_role"
        const val KEY_NAME = "user_name"
        const val KEY_ID = "user_id"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }
    
    fun saveUserDetails(id: String, name: String) {
        prefs.edit()
            .putString(KEY_ID, id)
            .putString(KEY_NAME, name)
            .apply()
    }

    fun savePhotoUrl(url: String?) {
        prefs.edit().putString(KEY_PHOTO_URL, url).apply()
    }

    fun getPhotoUrl(): String? {
        return prefs.getString(KEY_PHOTO_URL, null)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val KEY_TOKEN = "auth_token"
        const val KEY_ROLE = "user_role"
        const val KEY_NAME = "user_name"
        const val KEY_ID = "user_id"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_PHOTO_URL = "photo_url"
    }
}

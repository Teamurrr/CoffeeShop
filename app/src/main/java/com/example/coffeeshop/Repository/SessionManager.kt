package com.example.coffeeshop.Repository

import android.content.Context
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Domain.UserSession
import java.util.UUID

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSession(): UserSession? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val role = prefs.getString(KEY_ROLE, null) ?: return null
        val name = prefs.getString(KEY_NAME, null).orEmpty()
        val email = prefs.getString(KEY_EMAIL, null).orEmpty()
        return UserSession(userId = userId, role = role, name = name, email = email)
    }

    fun saveCustomer(name: String, email: String) {
        prefs.edit()
            .putString(KEY_USER_ID, "cust-${UUID.randomUUID()}")
            .putString(KEY_ROLE, UserRole.CUSTOMER)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun saveStaff(role: String, name: String) {
        prefs.edit()
            .putString(KEY_USER_ID, role)
            .putString(KEY_ROLE, role)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, "")
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "coffee_shop_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ROLE = "role"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"

        const val CASHIER_PIN = "1111"
        const val BARISTA_PIN = "2222"
        const val ADMIN_PIN = "3333"
    }
}

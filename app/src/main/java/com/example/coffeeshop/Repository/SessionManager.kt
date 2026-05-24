package com.example.coffeeshop.Repository

import android.content.Context
import com.example.coffeeshop.Domain.ReservationContext
import com.example.coffeeshop.Domain.UserSession

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSession(): UserSession? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val role = prefs.getString(KEY_ROLE, null) ?: return null
        val name = prefs.getString(KEY_NAME, null).orEmpty()
        val email = prefs.getString(KEY_EMAIL, null).orEmpty()
        return UserSession(userId = userId, role = role, name = name, email = email)
    }

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_ROLE, session.role)
            .putString(KEY_NAME, session.name)
            .putString(KEY_EMAIL, session.email)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun saveReservationContext(context: ReservationContext) {
        prefs.edit()
            .putString(KEY_RESERVATION_ID, context.reservationId)
            .putString(KEY_TABLE_ID, context.tableId)
            .putInt(KEY_TABLE_NUMBER, context.tableNumber)
            .putString(KEY_RESERVATION_TIME, context.reservationTime)
            .apply()
    }

    fun getReservationContext(): ReservationContext? {
        val reservationId = prefs.getString(KEY_RESERVATION_ID, null) ?: return null
        val tableId = prefs.getString(KEY_TABLE_ID, null) ?: return null
        val tableNumber = prefs.getInt(KEY_TABLE_NUMBER, -1)
        val reservationTime = prefs.getString(KEY_RESERVATION_TIME, null) ?: return null
        if (tableNumber <= 0) return null
        return ReservationContext(
            reservationId = reservationId,
            tableId = tableId,
            tableNumber = tableNumber,
            reservationTime = reservationTime
        )
    }

    fun clearReservationContext() {
        prefs.edit()
            .remove(KEY_RESERVATION_ID)
            .remove(KEY_TABLE_ID)
            .remove(KEY_TABLE_NUMBER)
            .remove(KEY_RESERVATION_TIME)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "coffee_shop_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ROLE = "role"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_RESERVATION_ID = "reservation_id"
        private const val KEY_TABLE_ID = "table_id"
        private const val KEY_TABLE_NUMBER = "table_number"
        private const val KEY_RESERVATION_TIME = "reservation_time"

        const val CASHIER_PIN = "1111"
        const val BARISTA_PIN = "2222"
        const val ADMIN_PIN = "3333"
    }
}

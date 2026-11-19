package com.example.smartcafecustomer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class CafeDatabase(context: Context) : SQLiteOpenHelper(context, "smart_cafe.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE users (email TEXT PRIMARY KEY, password_hash TEXT NOT NULL)")
        db.execSQL("CREATE TABLE menu_items (id TEXT PRIMARY KEY, name TEXT NOT NULL, category TEXT NOT NULL, description TEXT NOT NULL, price REAL NOT NULL, available INTEGER NOT NULL DEFAULT 1)")
        db.execSQL("CREATE TABLE orders (order_id TEXT PRIMARY KEY, customer_email TEXT NOT NULL, items_summary TEXT NOT NULL, total REAL NOT NULL, status TEXT NOT NULL, timestamp INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE feedback (id TEXT PRIMARY KEY, order_id TEXT NOT NULL, rating INTEGER NOT NULL, comment TEXT NOT NULL, sentiment_label TEXT NOT NULL, confidence REAL)")

        addDefaultMenu(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS menu_items")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS feedback")
        onCreate(db)
    }

    private fun addDefaultMenu(db: SQLiteDatabase) {
        val items = listOf(
            MenuItem("M01", "Flat White", "Drinks", "Double espresso with steamed milk", 3.40),
            MenuItem("M02", "Iced Caramel Latte", "Drinks", "Chilled espresso, milk and caramel syrup", 3.95),
            MenuItem("M03", "Matcha Green Tea", "Drinks", "Matcha whisked with oat milk", 3.80),
            MenuItem("M04", "Hot Chocolate", "Drinks", "Rich cocoa with steamed milk", 3.50),
            MenuItem("M05", "Victoria Sponge", "Cakes", "Vanilla sponge with jam and cream", 3.25),
            MenuItem("M06", "Chocolate Brownie", "Cakes", "Warm dark chocolate brownie", 2.95)
        )

        for (item in items) {
            val values = ContentValues()
            values.put("id", item.id)
            values.put("name", item.name)
            values.put("category", item.category)
            values.put("description", item.description)
            values.put("price", item.price)
            values.put("available", if (item.available) 1 else 0)
            db.insert("menu_items", null, values)
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        var result = ""
        for (b in bytes) {
            result += String.format("%02x", b)
        }
        return result
    }

    fun registerUser(email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("email", email.lowercase())
        values.put("password_hash", hashPassword(password))
        val rowId = db.insert("users", null, values)
        return rowId != -1L
    }

    fun validateLogin(email: String, password: String): Boolean {
        val db = readableDatabase
        val hash = hashPassword(password)
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email = ? AND password_hash = ?",
            arrayOf(email.lowercase(), hash)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun getMenu(): List<MenuItem> {
        val menuList = mutableListOf<MenuItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM menu_items", null)

        while (cursor.moveToNext()) {
            val item = MenuItem(
                id = cursor.getString(0),
                name = cursor.getString(1),
                category = cursor.getString(2),
                description = cursor.getString(3),
                price = cursor.getDouble(4),
                available = cursor.getInt(5) == 1
            )
            menuList.add(item)
        }
        cursor.close()
        return menuList
    }

    fun insertOrder(order: CafeOrder) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("order_id", order.orderId)
        values.put("customer_email", order.customerEmail)
        values.put("items_summary", order.itemsSummary)
        values.put("total", order.total)
        values.put("status", order.status)
        values.put("timestamp", order.timestamp)
        db.insert("orders", null, values)
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("status", newStatus)
        db.update("orders", values, "order_id = ?", arrayOf(orderId))
    }

    fun getOrdersForCustomer(email: String): List<CafeOrder> {
        val orderList = mutableListOf<CafeOrder>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM orders WHERE customer_email = ? ORDER BY timestamp DESC",
            arrayOf(email.lowercase())
        )

        while (cursor.moveToNext()) {
            val order = CafeOrder(
                orderId = cursor.getString(0),
                customerEmail = cursor.getString(1),
                itemsSummary = cursor.getString(2),
                total = cursor.getDouble(3),
                status = cursor.getString(4),
                timestamp = cursor.getLong(5)
            )
            orderList.add(order)
        }
        cursor.close()
        return orderList
    }

    fun insertFeedback(fb: FeedbackItem) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("id", fb.id)
        values.put("order_id", fb.orderId)
        values.put("rating", fb.rating)
        values.put("comment", fb.comment)
        values.put("sentiment_label", fb.sentimentLabel)
        values.put("confidence", fb.confidence)
        db.insert("feedback", null, values)
    }

    fun getAllFeedback(): List<FeedbackItem> {
        val feedbackList = mutableListOf<FeedbackItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM feedback", null)

        while (cursor.moveToNext()) {
            val confidenceValue = if (cursor.isNull(5)) null else cursor.getDouble(5)
            val fb = FeedbackItem(
                id = cursor.getString(0),
                orderId = cursor.getString(1),
                rating = cursor.getInt(2),
                comment = cursor.getString(3),
                sentimentLabel = cursor.getString(4),
                confidence = confidenceValue
            )
            feedbackList.add(fb)
        }
        cursor.close()
        return feedbackList
    }
}
package com.example.smartcafecustomer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class CafeDatabase(context: Context) : SQLiteOpenHelper(context, "smart_cafe.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                email TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE menu_items (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT,
                price REAL NOT NULL,
                available INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE orders (
                order_id TEXT PRIMARY KEY,
                customer_email TEXT NOT NULL,
                items_summary TEXT NOT NULL,
                total REAL NOT NULL,
                status TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE feedback (
                id TEXT PRIMARY KEY,
                order_id TEXT NOT NULL,
                rating INTEGER NOT NULL,
                comment TEXT,
                sentiment_label TEXT NOT NULL,
                confidence REAL
            )
            """.trimIndent()
        )

        seedMenu(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DELETE FROM menu_items")
        seedMenu(db)
    }

    private fun seedMenu(db: SQLiteDatabase) {
        val items = listOf(
            MenuItem("M01", "Signature Flat White", "Drinks", "Velvety steamed milk over a double ristretto shot", 3.40, true),
            MenuItem("M02", "Iced Matcha Oat Latte", "Drinks", "Ceremonial-grade green matcha whisked with chilled oat milk", 4.25, true),
            MenuItem("M03", "Caramel Macchiato", "Drinks", "Rich espresso layered with vanilla syrup and caramel drizzle", 3.95, true),
            MenuItem("M04", "Fresh Mint & Honey Tea", "Drinks", "Soothing organic green tea infused with garden mint and honey", 2.90, true),
            MenuItem("M05", "Pistachio & Rose Croissant", "Cakes", "Flaky butter croissant filled with roasted pistachio cream", 3.75, true),
            MenuItem("M06", "Sicilian Lemon Drizzle Loaf", "Cakes", "Zesty sponge cake glazed with tangy lemon icing", 3.30, true),
            MenuItem("M07", "Triple Chocolate Brownie", "Cakes", "Warm fudgy dark chocolate brownie with sea salt flakes", 3.60, true),
            MenuItem("M08", "Avocado & Feta Sourdough", "Snacks", "Smashed avocado, crumbled feta, and chilli flakes on toasted sourdough", 5.80, true),
            MenuItem("M09", "Pesto Mozzarella Panini", "Snacks", "Toasted ciabatta with basil pesto, fresh mozzarella, and sun-dried tomatoes", 5.50, true)
        )

        for (item in items) {
            val values = ContentValues().apply {
                put("id", item.id)
                put("name", item.name)
                put("category", item.category)
                put("description", item.description)
                put("price", item.price)
                put("available", if (item.available) 1 else 0)
            }
            db.insertWithOnConflict("menu_items", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun registerUser(email: String, password: String): Boolean {
        val values = ContentValues().apply {
            put("email", email.trim().lowercase())
            put("password_hash", sha256(password))
        }
        return writableDatabase.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getMenu(): List<MenuItem> {
        val list = mutableListOf<MenuItem>()
        readableDatabase.rawQuery("SELECT * FROM menu_items WHERE available = 1", null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    MenuItem(
                        id = cursor.getString(0),
                        name = cursor.getString(1),
                        category = cursor.getString(2),
                        description = cursor.getString(3) ?: "",
                        price = cursor.getDouble(4),
                        available = cursor.getInt(5) == 1
                    )
                )
            }
        }
        return list
    }

    fun insertOrder(order: CafeOrder) {
        val values = ContentValues().apply {
            put("order_id", order.orderId)
            put("customer_email", order.customerEmail)
            put("items_summary", order.itemsSummary)
            put("total", order.total)
            put("status", order.status)
            put("timestamp", order.timestamp)
        }
        writableDatabase.insertWithOnConflict("orders", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        val values = ContentValues().apply {
            put("status", newStatus)
        }
        writableDatabase.update("orders", values, "order_id = ?", arrayOf(orderId))
    }

    fun getOrdersForCustomer(email: String): List<CafeOrder> {
        val list = mutableListOf<CafeOrder>()
        readableDatabase.rawQuery(
            "SELECT * FROM orders WHERE customer_email = ? ORDER BY timestamp DESC",
            arrayOf(email)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    CafeOrder(
                        orderId = cursor.getString(0),
                        customerEmail = cursor.getString(1),
                        itemsSummary = cursor.getString(2),
                        total = cursor.getDouble(3),
                        status = cursor.getString(4),
                        timestamp = cursor.getLong(5)
                    )
                )
            }
        }
        return list
    }

    fun insertFeedback(fb: FeedbackItem) {
        val values = ContentValues().apply {
            put("id", fb.id)
            put("order_id", fb.orderId)
            put("rating", fb.rating)
            put("comment", fb.comment)
            put("sentiment_label", fb.sentimentLabel)
            if (fb.confidence != null) {
                put("confidence", fb.confidence)
            } else {
                putNull("confidence")
            }
        }
        writableDatabase.insertWithOnConflict("feedback", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllFeedback(): List<FeedbackItem> {
        val list = mutableListOf<FeedbackItem>()
        readableDatabase.rawQuery("SELECT * FROM feedback", null).use { cursor ->
            while (cursor.moveToNext()) {
                val confIndex = 5
                val conf = if (cursor.isNull(confIndex)) null else cursor.getDouble(confIndex)
                list.add(
                    FeedbackItem(
                        id = cursor.getString(0),
                        orderId = cursor.getString(1),
                        rating = cursor.getInt(2),
                        comment = cursor.getString(3) ?: "",
                        sentimentLabel = cursor.getString(4),
                        confidence = conf
                    )
                )
            }
        }
        return list
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
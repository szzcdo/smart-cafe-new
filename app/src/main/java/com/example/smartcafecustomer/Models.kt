package com.example.smartcafecustomer

data class MenuItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val price: Double,
    val available: Boolean = true
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val subtotal: Double get() = menuItem.price * quantity
}

data class CafeOrder(
    val orderId: String,
    val customerEmail: String,
    val itemsSummary: String,
    val total: Double,
    val status: String,
    val timestamp: Long
)

data class FeedbackItem(
    val id: String,
    val orderId: String,
    val rating: Int,
    val comment: String,
    val sentimentLabel: String,
    val confidence: Double?
)
package com.example.smartcafecustomer

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CafeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CafeDatabase(application)
    private val sessionStore = SessionStore(application)
    private val sentimentModel = CafeSentimentModel()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    private val _hasAcceptedTerms = MutableStateFlow(false)
    val hasAcceptedTerms: StateFlow<Boolean> = _hasAcceptedTerms

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    private val _orders = MutableStateFlow<List<CafeOrder>>(emptyList())
    val orders: StateFlow<List<CafeOrder>> = _orders

    private val _feedbackList = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackItem>> = _feedbackList

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage

    init {
        createNotificationChannel()
        _menuItems.value = db.getMenu()
        _feedbackList.value = db.getAllFeedback()

        viewModelScope.launch {
            sessionStore.acceptedTermsFlow.collect { accepted ->
                _hasAcceptedTerms.value = accepted
            }
        }

        viewModelScope.launch {
            sessionStore.userEmailFlow.collect { savedEmail ->
                _currentUserEmail.value = savedEmail
                if (savedEmail != null) {
                    _orders.value = db.getOrdersForCustomer(savedEmail)
                }
            }
        }
    }

    fun acceptTerms() {
        viewModelScope.launch {
            sessionStore.saveAcceptedTerms(true)
        }
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            _authError.value = "Please enter both email and password."
            return
        }

        val valid = db.validateLogin(cleanEmail, password)
        if (valid) {
            _authError.value = null
            viewModelScope.launch {
                sessionStore.saveUserEmail(cleanEmail)
            }
        } else {
            _authError.value = "Invalid email or password."
        }
    }

    fun register(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _authError.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _authError.value = "Password must be at least 6 characters."
            return
        }

        val created = db.registerUser(cleanEmail, password)
        if (created) {
            _authError.value = null
            viewModelScope.launch {
                sessionStore.saveUserEmail(cleanEmail)
            }
        } else {
            _authError.value = "An account with this email already exists."
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionStore.clearUserEmail()
            _cart.value = emptyList()
            _orders.value = emptyList()
        }
    }

    fun addToCart(item: MenuItem) {
        val updatedList = mutableListOf<CartItem>()
        var found = false

        for (existing in _cart.value) {
            if (existing.menuItem.id == item.id) {
                updatedList.add(CartItem(item, existing.quantity + 1))
                found = true
            } else {
                updatedList.add(existing)
            }
        }

        if (!found) {
            updatedList.add(CartItem(item, 1))
        }

        _cart.value = updatedList
        _bannerMessage.value = "${item.name} added to cart"
    }

    fun decreaseQuantity(itemId: String) {
        val updatedList = mutableListOf<CartItem>()
        for (existing in _cart.value) {
            if (existing.menuItem.id == itemId) {
                if (existing.quantity > 1) {
                    updatedList.add(CartItem(existing.menuItem, existing.quantity - 1))
                }
            } else {
                updatedList.add(existing)
            }
        }
        _cart.value = updatedList
    }

    fun getCartTotal(): Double {
        var total = 0.0
        for (item in _cart.value) {
            total += item.subtotal
        }
        return total
    }

    fun placeOrder() {
        val email = _currentUserEmail.value ?: return
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return

        var summary = ""
        for (i in currentCart.indices) {
            val c = currentCart[i]
            summary += "${c.quantity}x ${c.menuItem.name}"
            if (i < currentCart.size - 1) {
                summary += ", "
            }
        }

        val orderId = "ORD-" + UUID.randomUUID().toString().take(6).uppercase()
        val newOrder = CafeOrder(
            orderId = orderId,
            customerEmail = email,
            itemsSummary = summary,
            total = getCartTotal(),
            status = "Pending",
            timestamp = System.currentTimeMillis()
        )

        db.insertOrder(newOrder)
        _cart.value = emptyList()
        _orders.value = db.getOrdersForCustomer(email)
        _bannerMessage.value = "Order $orderId placed!"
        sendNotification("Order Received", "Order $orderId is now Pending.")

        // Simulate café kitchen updating the order status automatically
        viewModelScope.launch {
            delay(8000)
            db.updateOrderStatus(orderId, "Preparing")
            _orders.value = db.getOrdersForCustomer(email)
            sendNotification("Order Preparing", "Baristas are making order $orderId.")

            delay(8000)
            db.updateOrderStatus(orderId, "Ready for Collection")
            _orders.value = db.getOrdersForCustomer(email)
            sendNotification("Order Ready!", "Order $orderId is ready at the counter.")
        }
    }

    fun submitFeedback(orderId: String, rating: Int, comment: String) {
        val prediction = sentimentModel.predict(comment)
        val label = prediction?.label ?: "Neutral"
        val confidence = prediction?.score

        val fb = FeedbackItem(
            id = UUID.randomUUID().toString(),
            orderId = orderId,
            rating = rating,
            comment = comment.trim(),
            sentimentLabel = label,
            confidence = confidence
        )

        db.insertFeedback(fb)
        _feedbackList.value = db.getAllFeedback()
        _bannerMessage.value = "Feedback saved (Sentiment: $label)"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "cafe_orders",
                "Cafe Order Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, message: String) {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notification = NotificationCompat.Builder(context, "cafe_orders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
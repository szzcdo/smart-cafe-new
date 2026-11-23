package com.example.smartcafecustomer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartCafeApp()
                }
            }
        }
    }
}

@Composable
fun SmartCafeApp(vm: CafeViewModel = viewModel()) {
    val hasAcceptedTerms by vm.hasAcceptedTerms.collectAsState()
    val currentUserEmail by vm.currentUserEmail.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (!hasAcceptedTerms) {
        TermsScreen(onAccept = { vm.acceptTerms() })
    } else if (currentUserEmail == null) {
        AuthScreen(vm = vm)
    } else {
        CustomerHomeScreen(vm = vm, userEmail = currentUserEmail!!)
    }
}

@Composable
fun TermsScreen(onAccept: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Cafe Privacy & Terms",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app stores your email, hashed password, orders, and feedback locally on your device in SQLite to process your café orders. Feedback comments are analysed locally on-device to estimate customer sentiment.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Accept & Continue")
        }
    }
}

@Composable
fun AuthScreen(vm: CafeViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    val authError by vm.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Cafe Customer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isRegisterMode) "Create an account" else "Sign in to order",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 6 chars)") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (authError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = authError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (isRegisterMode) {
                    vm.register(email, password)
                } else {
                    vm.login(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegisterMode) "Register" else "Login")
        }

        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(
                if (isRegisterMode) "Already have an account? Login"
                else "New customer? Create an account"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(vm: CafeViewModel, userEmail: String) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val bannerMessage by vm.bannerMessage.collectAsState()
    val cart by vm.cart.collectAsState()

    var totalCartCount = 0
    for (item in cart) {
        totalCartCount += item.quantity
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Cafe") },
                actions = {
                    TextButton(onClick = { vm.logout() }) {
                        Text("Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("Menu") },
                    label = { Text("Browse") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("Cart ($totalCartCount)") },
                    label = { Text("Basket") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("Orders") },
                    label = { Text("Track") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("Review") },
                    label = { Text("Feedback") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Signed in as: $userEmail",
                style = MaterialTheme.typography.labelMedium
            )

            if (bannerMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(bannerMessage!!, modifier = Modifier.weight(1f))
                        TextButton(onClick = { vm.clearBanner() }) {
                            Text("OK")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> MenuTab(vm)
                1 -> CartTab(vm, onOrderPlaced = { selectedTab = 2 })
                2 -> OrdersTab(vm)
                3 -> FeedbackTab(vm)
            }
        }
    }
}

@Composable
fun MenuTab(vm: CafeViewModel) {
    val allItems by vm.menuItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredItems = mutableListOf<MenuItem>()
    for (item in allItems) {
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        if (matchesCategory && matchesSearch) {
            filteredItems.add(item)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search drinks or cakes...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categories = listOf("All", "Drinks", "Cakes")
            for (cat in categories) {
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredItems) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text(item.description, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = String.format(Locale.UK, "£%.2f", item.price),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(onClick = { vm.addToCart(item) }) {
                            Text("+ Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartTab(vm: CafeViewModel, onOrderPlaced: () -> Unit) {
    val cart by vm.cart.collectAsState()
    val total = vm.getCartTotal()

    if (cart.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Your cart is empty. Add items from the Menu tab.")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cart) { cartItem ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cartItem.menuItem.name, fontWeight = FontWeight.Bold)
                            Text(
                                String.format(
                                    Locale.UK,
                                    "%d x £%.2f = £%.2f",
                                    cartItem.quantity,
                                    cartItem.menuItem.price,
                                    cartItem.subtotal
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = { vm.decreaseQuantity(cartItem.menuItem.id) }) {
                                Text("-")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { vm.addToCart(cartItem.menuItem) }) {
                                Text("+")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = String.format(Locale.UK, "Total: £%.2f", total),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                vm.placeOrder()
                onOrderPlaced()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Place Order")
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun OrdersTab(vm: CafeViewModel) {
    val orders by vm.orders.collectAsState()

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No orders yet. Place an order from your Cart!")
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(orders) { order ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(order.orderId, fontWeight = FontWeight.Bold)
                        Text(
                            text = order.status,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(order.itemsSummary)
                    Text(
                        text = String.format(Locale.UK, "Total: £%.2f", order.total),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun FeedbackTab(vm: CafeViewModel) {
    val orders by vm.orders.collectAsState()
    val feedbackList by vm.feedbackList.collectAsState()

    var selectedOrderId by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    if (selectedOrderId.isEmpty() && orders.isNotEmpty()) {
        selectedOrderId = orders.first().orderId
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Leave Order Feedback", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = selectedOrderId,
            onValueChange = { selectedOrderId = it },
            label = { Text("Order ID (e.g. ORD-123ABC)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Rating: ")
            for (star in 1..5) {
                FilterChip(
                    selected = rating == star,
                    onClick = { rating = star },
                    label = { Text("$star★") },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment (e.g. Great hot coffee and fast service)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (selectedOrderId.isNotBlank() && comment.isNotBlank()) {
                    vm.submitFeedback(selectedOrderId, rating, comment)
                    comment = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Review")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Recent Reviews & ML Sentiment", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(feedbackList) { fb ->
                val confText = if (fb.confidence != null) {
                    String.format(Locale.UK, " (%.0f%%)", fb.confidence * 100)
                } else {
                    ""
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${fb.orderId} • ${fb.rating}/5 ★", fontWeight = FontWeight.Bold)
                        Text(fb.comment)
                        Text(
                            text = "ML Sentiment: ${fb.sentimentLabel}$confText",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
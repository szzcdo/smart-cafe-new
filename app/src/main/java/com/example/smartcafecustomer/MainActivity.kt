package com.example.smartcafecustomer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcafecustomer.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCafeCustomerTheme {
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

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(currentUserEmail) {
        if (currentUserEmail != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    when {
        !hasAcceptedTerms -> TermsScreen(onAccept = { vm.acceptTerms() })
        currentUserEmail == null -> AuthScreen(vm = vm)
        else -> MainScaffold(vm = vm, userEmail = currentUserEmail!!)
    }
}

@Composable
fun TermsScreen(onAccept: () -> Unit) {
    var agreed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeWhite)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(
                color = CafeGreenContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "PRIVACY & TERMS",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = CafeGreenPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Welcome to Smart Café",
                style = MaterialTheme.typography.headlineMedium,
                color = CafeGreenPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Before you grab your first coffee, here is a quick, honest look at how we handle your information:",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = CafeDarkText
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CafeGreenContainer),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CafeGreenSecondary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PolicyPoint(
                        title = "1. Signing In Safely",
                        body = "When you create an account or log in, we use Google Firebase Authentication to verify your email and password over a secure, encrypted connection."
                    )
                    PolicyPoint(
                        title = "2. Your Orders Stay on Your Phone",
                        body = "Your order history, cart items, and star ratings are saved locally on your device, with automatic device backups turned off."
                    )
                    PolicyPoint(
                        title = "3. Private Feedback Checks",
                        body = "When you leave a written review, a small built-in model on your phone checks if it sounds positive or negative. Your comments are never sent to external AI servers."
                    )
                    PolicyPoint(
                        title = "4. Order Alerts & Your Choices",
                        body = "We only send notifications to let you know when your order is being made or ready to collect. You can turn off alerts, sign out anytime, or wipe local data in your phone's settings."
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = CafeGreenPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sounds good — I agree to the Smart Café terms and privacy notice.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = CafeDarkText
                )
            }

            Button(
                onClick = onAccept,
                enabled = agreed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CafeGreenPrimary,
                    contentColor = CafeWhite
                )
            ) {
                Text(
                    text = "Let's Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PolicyPoint(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = CafeGreenPrimary
        )
        Text(
            text = body,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = CafeDarkText
        )
    }
}

@Composable
fun AuthScreen(vm: CafeViewModel) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authError by vm.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeWhite)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocalCafe,
            contentDescription = "Smart Café Logo",
            tint = CafeGreenPrimary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Smart Café",
            style = MaterialTheme.typography.headlineLarge,
            color = CafeGreenPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = if (isRegister) "Create Your Customer Account" else "Customer Sign In",
            style = MaterialTheme.typography.titleMedium,
            color = CafeGreenSecondary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 6 characters)", fontWeight = FontWeight.Bold) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
        )

        if (authError != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = authError!!,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (isRegister) vm.register(email, password) else vm.login(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CafeGreenPrimary,
                contentColor = CafeWhite
            )
        ) {
            Text(
                text = if (isRegister) "Register Account" else "Sign In",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { isRegister = !isRegister }) {
            Text(
                text = if (isRegister) "Already have an account? Sign In" else "New customer? Create an account",
                color = CafeGreenPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(vm: CafeViewModel, userEmail: String) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val cart by vm.cart.collectAsState()
    val bannerMessage by vm.bannerMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Café",
                            fontWeight = FontWeight.ExtraBold,
                            color = CafeWhite,
                            fontSize = 20.sp
                        )
                        Text(
                            text = userEmail,
                            fontWeight = FontWeight.Bold,
                            color = CafeGreenContainer,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CafeGreenPrimary,
                    titleContentColor = CafeWhite
                ),
                actions = {
                    IconButton(onClick = { vm.logout() }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = CafeWhite
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CafeWhite,
                tonalElevation = 6.dp
            ) {
                val cartCount = cart.sumOf { it.quantity }
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                    label = { Text("Menu", fontWeight = FontWeight.ExtraBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CafeWhite,
                        indicatorColor = CafeGreenPrimary,
                        selectedTextColor = CafeGreenPrimary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart ($cartCount)", fontWeight = FontWeight.ExtraBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CafeWhite,
                        indicatorColor = CafeGreenPrimary,
                        selectedTextColor = CafeGreenPrimary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                    label = { Text("Orders", fontWeight = FontWeight.ExtraBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CafeWhite,
                        indicatorColor = CafeGreenPrimary,
                        selectedTextColor = CafeGreenPrimary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.RateReview, contentDescription = "Feedback") },
                    label = { Text("Feedback", fontWeight = FontWeight.ExtraBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CafeWhite,
                        indicatorColor = CafeGreenPrimary,
                        selectedTextColor = CafeGreenPrimary
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CafeOffWhite)
                .padding(paddingValues)
        ) {
            if (bannerMessage != null) {
                Surface(
                    color = CafeGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bannerMessage!!,
                            color = CafeGreenPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = { vm.clearBanner() }) {
                            Text("OK", color = CafeGreenPrimary, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> MenuTab(vm = vm)
                1 -> CartTab(vm = vm, onOrderPlaced = { selectedTab = 2 })
                2 -> OrdersTab(vm = vm)
                3 -> FeedbackTab(vm = vm)
            }
        }
    }
}

@Composable
fun MenuTab(vm: CafeViewModel) {
    val menuItems by vm.menuItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Drinks", "Cakes", "Snacks")

    val filtered = menuItems.filter { item ->
        val matchesCat = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search drinks, cakes, snacks...", fontWeight = FontWeight.Bold) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CafeGreenPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CafeGreenPrimary,
                        selectedLabelColor = CafeWhite,
                        containerColor = CafeWhite,
                        labelColor = CafeGreenPrimary
                    )
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CafeWhite),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CafeGreenContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CafeGreenSecondary
                            )
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = CafeDarkText
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334735)
                            )
                            Text(
                                text = String.format(Locale.UK, "£%.2f", item.price),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CafeGreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { vm.addToCart(item) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CafeGreenPrimary,
                                contentColor = CafeWhite
                            )
                        ) {
                            Text("Add", fontWeight = FontWeight.ExtraBold)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your cart is empty. Add items from the Menu!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CafeGreenPrimary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cart) { cartItem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CafeWhite),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CafeGreenContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cartItem.menuItem.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = CafeDarkText
                                )
                                Text(
                                    text = String.format(
                                        Locale.UK,
                                        "£%.2f each • Subtotal: £%.2f",
                                        cartItem.menuItem.price,
                                        cartItem.subtotal
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = CafeGreenSecondary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { vm.decreaseQuantity(cartItem.menuItem.id) },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("-", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = CafeGreenPrimary)
                                }
                                Text(
                                    text = "${cartItem.quantity}",
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Button(
                                    onClick = { vm.addToCart(cartItem.menuItem) },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CafeGreenPrimary)
                                ) {
                                    Text("+", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = CafeWhite)
                                }
                            }
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CafeGreenContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Amount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CafeDarkText
                        )
                        Text(
                            text = String.format(Locale.UK, "£%.2f", vm.getCartTotal()),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CafeGreenPrimary
                        )
                    }

                    Button(
                        onClick = {
                            vm.placeOrder()
                            onOrderPlaced()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CafeGreenPrimary,
                            contentColor = CafeWhite
                        )
                    ) {
                        Text("Place Order Now", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersTab(vm: CafeViewModel) {
    val orders by vm.orders.collectAsState()
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.UK) }

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No orders placed yet.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CafeGreenPrimary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CafeWhite),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CafeGreenSecondary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.orderId,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = CafeGreenPrimary
                            )
                            Surface(
                                color = CafeGreenContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = order.status,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = CafeGreenPrimary
                                )
                            }
                        }
                        Text(
                            text = order.itemsSummary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CafeDarkText
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatter.format(Date(order.timestamp)),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = String.format(Locale.UK, "Total: £%.2f", order.total),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = CafeGreenPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackTab(vm: CafeViewModel) {
    val feedbackList by vm.feedbackList.collectAsState()
    val orders by vm.orders.collectAsState()
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CafeWhite),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CafeGreenSecondary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Leave Café Feedback",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = CafeGreenPrimary
                    )
                    Text(
                        text = "Your comment is automatically analysed on-device by our ML sentiment classifier.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CafeDarkText
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (star in 1..5) {
                            FilterChip(
                                selected = rating == star,
                                onClick = { rating = star },
                                label = { Text("$star ★", fontWeight = FontWeight.ExtraBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CafeGreenPrimary,
                                    selectedLabelColor = CafeWhite
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Write your review...", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                    )

                    Button(
                        onClick = {
                            if (comment.isNotBlank()) {
                                val latestOrderId = orders.firstOrNull()?.orderId ?: "GENERAL"
                                vm.submitFeedback(latestOrderId, rating, comment)
                                comment = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CafeGreenPrimary,
                            contentColor = CafeWhite
                        )
                    ) {
                        Text("Submit Review", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        items(feedbackList) { fb ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CafeWhite),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CafeGreenContainer)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rating: ${fb.rating}/5 ★",
                            fontWeight = FontWeight.ExtraBold,
                            color = CafeDarkText
                        )
                        Surface(
                            color = CafeGreenContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ML Sentiment: ${fb.sentimentLabel}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = CafeGreenPrimary
                            )
                        }
                    }
                    Text(
                        text = fb.comment,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CafeDarkText
                    )
                }
            }
        }
    }
}
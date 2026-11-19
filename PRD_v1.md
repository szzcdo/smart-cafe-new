# Smart Cafe Customer App

## User Role
Customer (main role)
Admin (simulated using a timer to update order status)

## Problem
Customers often queue before ordering and cannot easily browse menu items or check when their order is ready.

## Goal
Allow customers to register, login, browse and search products, manage a cart, place orders, track status updates and leave feedback.

## MVP Features
- Register and login with email and password
- View, search and filter menu items (drinks and cakes)
- Add, update and remove items in cart
- Place order and view order history
- Receive notifications when order status changes
- Submit rating and review for completed orders
- Sentiment check on feedback text (positive or negative)
- Logout

## Data
- MenuItem: id, name, category, price, available
- Order: orderId, customerUid, status, total, timestamp
- OrderItem: id, orderId, menuItemId, quantity, price
- Feedback: id, orderId, rating, comment, sentiment

## Constraints & Technologies
- Kotlin (mandatory)
- Jetpack Compose
- Firebase Authentication (no passwords saved locally)
- Room Database (offline menu and order storage)

## Acceptance Criteria
- AC-01: Given a registered customer, when they login with valid details, the menu screen is displayed.
- AC-02: Given invalid login details or empty fields, an error message is shown and access is blocked.
- AC-03: Given a logged out user, protected screens cannot be opened.
- AC-04: When searching or filtering the menu, only matching items are shown.
- AC-05: When adding or changing items in the cart, the total price updates correctly.
- AC-06: When an order is placed, it saves to Room and still shows after restarting the app.
- AC-07: When feedback is submitted, the rating is saved and sentiment is displayed.

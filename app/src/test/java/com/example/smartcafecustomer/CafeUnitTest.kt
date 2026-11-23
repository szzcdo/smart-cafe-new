package com.example.smartcafecustomer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CafeUnitTest {

    @Test
    fun testCartItemSubtotalCalculation() {
        val latte = MenuItem("M02", "Iced Caramel Latte", "Drinks", "Chilled espresso", 3.95)
        val cartItem = CartItem(menuItem = latte, quantity = 2)

        assertEquals(7.90, cartItem.subtotal, 0.001)
    }

    @Test
    fun testSentimentModelPositiveReview() {
        val model = CafeSentimentModel()
        val result = model.predict("Great coffee and fast friendly service")

        assertNotNull(result)
        assertEquals("Positive", result?.label)
    }

    @Test
    fun testSentimentModelNegativeReview() {
        val model = CafeSentimentModel()
        val result = model.predict("Cold coffee and slow wait in a long queue")

        assertNotNull(result)
        assertEquals("Negative", result?.label)
    }

    @Test
    fun testSentimentModelUnknownWordsReturnsNull() {
        val model = CafeSentimentModel()
        val result = model.predict("12345 xyz")

        assertNull(result)
    }
}
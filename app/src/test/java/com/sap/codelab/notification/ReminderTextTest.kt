package com.sap.codelab.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderTextTest {

    @Test
    fun `keeps a text shorter than 140 characters`() {
        assertEquals("Buy milk", ReminderText.of("Buy milk"))
    }

    @Test
    fun `keeps a text of exactly 140 characters`() {
        val text = "a".repeat(140)

        assertEquals(text, ReminderText.of(text))
    }

    @Test
    fun `cuts a longer text to its first 140 characters`() {
        val text = "a".repeat(140) + "bcd"

        assertEquals("a".repeat(140), ReminderText.of(text))
    }

    @Test
    fun `counts an emoji as one character`() {
        // 140 characters, but 141 UTF-16 units: the emoji is a surrogate pair
        val text = "a".repeat(139) + "😀"

        assertEquals(text, ReminderText.of(text))
    }

    @Test
    fun `never cuts an emoji in half`() {
        val text = "a".repeat(139) + "😀😀"

        assertEquals("a".repeat(139) + "😀", ReminderText.of(text))
    }
}

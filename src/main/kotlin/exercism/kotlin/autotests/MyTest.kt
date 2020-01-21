package exercism.kotlin.autotests

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MyTest {

    @Test
    fun `test a`() {
        hello()
        assertEquals(4, 2 + 2)
    }

    @Test
    @Disabled
    fun `test b`() {
        hello2()
        assertTrue(false)
    }
}

fun hello() {
    println("I'm here!")
}

fun hello2() {
    println("And I!")
}

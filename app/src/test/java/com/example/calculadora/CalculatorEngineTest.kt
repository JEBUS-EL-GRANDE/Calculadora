package com.example.calculadora

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    @Test
    fun testBasicArithmetic() {
        assertEquals("5", engine.evaluate("2 + 3"))
        assertEquals("6", engine.evaluate("10 - 4"))
        assertEquals("20", engine.evaluate("4 × 5"))
        assertEquals("4", engine.evaluate("12 ÷ 3"))
        assertEquals("14", engine.evaluate("2 + 3 × 4"))
        assertEquals("20", engine.evaluate("(2 + 3) × 4"))
    }

    @Test
    fun testImplicitMultiplication() {
        assertEquals("24", engine.evaluate("8(3)"))
        assertEquals("25", engine.evaluate("(2+3)(4+1)"))
        assertEquals("15", engine.evaluate("(5)3"))
        assertEquals("24", engine.evaluate("8√9"))
        assertEquals("4", engine.evaluate("8sin(30)"))
    }

    @Test
    fun testTrigonometryDegrees() {
        engine.isDegreeMode = true
        assertEquals("0.5", engine.evaluate("sin(30)"))
        assertEquals("0.5", engine.evaluate("cos(60)"))
        assertEquals("1", engine.evaluate("tan(45)"))
        assertEquals("0", engine.evaluate("sin(180)"))
        assertEquals("0", engine.evaluate("cos(90)"))
    }

    @Test
    fun testTrigonometryRadians() {
        engine.isDegreeMode = false
        assertEquals("0", engine.evaluate("sin(0)"))
        assertEquals("1", engine.evaluate("cos(0)"))
    }

    @Test
    fun testFractions() {
        assertEquals("0.75", engine.evaluate("3 / 4"))
        assertEquals("3/4", CalculatorEngine.doubleToFraction("0.75"))
        assertEquals("1/2", CalculatorEngine.doubleToFraction("0.5"))
        assertEquals("5/2", CalculatorEngine.doubleToFraction("2.5"))
        assertEquals("1/3", CalculatorEngine.doubleToFraction("0.33333333"))
    }

    @Test
    fun testExponentiation() {
        assertEquals("8", engine.evaluate("2 ^ 3"))
        assertEquals("25", engine.evaluate("5 ^ 2"))
        assertEquals("1", engine.evaluate("10 ^ 0"))
        assertEquals("512", engine.evaluate("2 ^ 3 ^ 2"))
    }

    @Test
    fun testSquareRoot() {
        assertEquals("4", engine.evaluate("√16"))
        assertEquals("3", engine.evaluate("√9"))
        assertEquals("5", engine.evaluate("√(9 + 16)"))
        assertEquals("19", engine.evaluate("√9 + 16"))
    }

    @Test
    fun testErrors() {
        assertEquals("Error", engine.evaluate("5 ÷ 0"))
        assertEquals("Error", engine.evaluate("√(-4)"))
        assertEquals("Error", engine.evaluate("2 + × 3"))
        assertEquals("Error", engine.evaluate("5 +"))
        assertEquals("Error", engine.evaluate("((2 + 3)"))
    }
}

package com.heitoranglada.coffeeapp.domain

import com.heitoranglada.coffeeapp.data.journal.JournalRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalculatorDomainTest {

    @Test
    fun `water from coffee and coffee from water`() {
        val byCoffee = CalcState(ratio = 16.0).withCoffee(15.0)
        assertEquals(240.0, byCoffee.waterGrams, 1e-6)
        val byWater = CalcState(ratio = 16.0).withWater(300.0)
        assertEquals(18.8, byWater.coffeeGrams, 1e-6)
        assertEquals(CalcAnchor.WATER, byWater.anchor)
    }

    @Test
    fun `changing the ratio keeps the side the person set`() {
        val s = CalcState(ratio = 16.0).withWater(300.0).withRatio(15.0)
        assertEquals(300.0, s.waterGrams, 1e-6)
        assertEquals(20.0, s.coffeeGrams, 1e-6)
        val t = CalcState(ratio = 16.0).withCoffee(20.0).withRatio(17.0)
        assertEquals(20.0, t.coffeeGrams, 1e-6)
        assertEquals(340.0, t.waterGrams, 1e-6)
    }

    @Test
    fun `steps snap to round numbers`() {
        val s = CalcState(ratio = 16.0).withWater(300.0) // café 18,8
        assertEquals(19.0, s.stepCoffee(up = true).coffeeGrams, 1e-6)
        assertEquals(18.5, s.stepCoffee(up = false).coffeeGrams, 1e-6)
        assertEquals(310.0, s.stepWater(up = true).waterGrams, 1e-6)
        assertEquals(16.5, s.stepRatio(up = true).ratio, 1e-6)
        assertEquals(15.5, s.stepRatio(up = false).ratio, 1e-6)
    }

    @Test
    fun `cup yield discounts what the grounds hold`() {
        val s = CalcState(ratio = 16.0).withWater(300.0)
        assertEquals(262.4, s.cupGrams, 1e-6)
    }

    @Test
    fun `espresso ratio uses the drink in the cup`() {
        val s = CalcState().withRatio(2.0).withCoffee(18.0)
        assertTrue(s.isEspresso)
        assertEquals(36.0, s.waterGrams, 1e-6)
        assertEquals(36.0, s.cupGrams, 1e-6)
        assertEquals(2.1, s.stepRatio(up = true).ratio, 1e-6)
    }

    @Test
    fun `ratio label`() {
        assertEquals("1:16", formatRatioValue(16.0))
        assertEquals("1:16,5", formatRatioValue(16.5))
    }

    @Test
    fun `days since roast`() {
        val now = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 5, 12, 0) }.timeInMillis
        assertEquals(12, JournalRepository.daysSinceRoast("24/10/2026", now))
        assertEquals(12, JournalRepository.daysSinceRoast("24/10/26", now))
        assertEquals("12 dias", JournalRepository.roastAgeLabel("24/10/2026", now))
        assertNull(JournalRepository.daysSinceRoast("ontem", now))
        assertNull(JournalRepository.daysSinceRoast("", now))
    }
}

package com.heitoranglada.coffeeapp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainTest {

    @Test
    fun `duration formats and parses`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("1:35", formatDuration(95))
        assertEquals("10:05", formatDuration(605))
        assertEquals(95, parseDuration("1:35"))
        assertEquals(95, parseDuration("95"))
        assertEquals(65, parseDuration("1:5"))
        assertNull(parseDuration("1:75"))
        assertNull(parseDuration("abc"))
        assertNull(parseDuration(""))
    }

    @Test
    fun `decimal accepts comma`() {
        assertEquals(15.5, parseDecimal("15,5")!!, 1e-9)
        assertEquals(15.5, parseDecimal("15.5")!!, 1e-9)
        assertNull(parseDecimal("-1"))
        assertNull(parseDecimal("x"))
    }

    @Test
    fun `ratio uses one decimal`() {
        assertEquals("1:16,7", formatRatio(15.0, 250.0))
        assertEquals("1:15", formatRatio(20.0, 300.0))
        assertNull(formatRatio(0.0, 250.0))
        assertNull(formatRatio(null, 250.0))
    }

    @Test
    fun `pour codec round trips`() {
        val pours = listOf(Pour(0, 45.0), Pour(45, 105.5), Pour(75, 100.0))
        assertEquals(pours, PourCodec.decode(PourCodec.encode(pours)))
        assertEquals(emptyList<Pour>(), PourCodec.decode(""))
        assertEquals(listOf(Pour(0, 10.0)), PourCodec.decode("0@10.0;lixo"))
        assertEquals(250.5, pours.totalGrams(), 1e-9)
        assertEquals(150.5, pours.cumulativeGramsAt(1), 1e-9)
    }

    @Test
    fun `guided progress finds current and next pour`() {
        val pours = listOf(Pour(0, 45.0), Pour(45, 105.0), Pour(75, 100.0))
        assertEquals(GuidedProgress(0, 1, 45), guidedProgress(pours, 0))
        assertEquals(GuidedProgress(0, 1, 5), guidedProgress(pours, 40))
        assertEquals(GuidedProgress(1, 2, 30), guidedProgress(pours, 45))
        assertEquals(GuidedProgress(2, null, null), guidedProgress(pours, 200))
        assertEquals(GuidedProgress(-1, 0, 5), guidedProgress(listOf(Pour(5, 10.0)), 0))
    }

    @Test
    fun `step codec keeps kinds and old format`() {
        val steps = listOf(Pour(0, 100.0), Pour(20, 0.0, StepKind.STIR), Pour(40, 0.0, StepKind.FLIP), Pour(60, 0.0, StepKind.PRESS), Pour(90, 100.0, StepKind.DILUTE))
        assertEquals(steps, PourCodec.decode(PourCodec.encode(steps)))
        assertEquals("0@100.0;20@0.0@STIR", PourCodec.encode(steps.take(2)))
        assertEquals(listOf(Pour(0, 45.0)), PourCodec.decode("0@45.0;5@1.0@NADA"))
        assertEquals(200.0, steps.totalGrams(), 1e-9)
        assertEquals(100.0, steps.cumulativeGramsAt(3), 1e-9)
    }

    @Test
    fun `espresso style tip and speed`() {
        assertEquals("normale", espressoStyle(18.0, 36.0))
        assertEquals("ristretto", espressoStyle(18.0, 25.0))
        assertEquals("lungo", espressoStyle(18.0, 54.0))
        assertNull(espressoStyle(null, 36.0))
        assertEquals(ShotSpeed.TOO_FAST, shotSpeed(18.0, 25, 30))
        assertEquals(ShotSpeed.FAST, shotSpeed(22.0, 25, 30))
        assertEquals(ShotSpeed.IN_WINDOW, shotSpeed(27.4, 25, 30))
        assertEquals(ShotSpeed.SLOW, shotSpeed(33.0, 25, 30))
        assertEquals(ShotSpeed.TOO_SLOW, shotSpeed(40.0, 25, 30))
        assertEquals("Moagem no ponto", espressoTip(27.0, 25, 30).title)
        assertEquals("27,4", formatShotSeconds(27.43))
    }
}

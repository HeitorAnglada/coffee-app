package com.heitoranglada.coffeeapp.domain

import kotlin.math.roundToInt

/** Qual lado da calculadora a pessoa definiu; o outro é calculado. */
enum class CalcAnchor { COFFEE, WATER }

/**
 * Calculadora de proporção café:água. [ratio] é quantas partes de água para uma de café
 * (1:16 -> 16). Abaixo de [ESPRESSO_MAX_RATIO] vale a conta do espresso: o "água" é a bebida na xícara.
 */
data class CalcState(
    val ratio: Double = 16.0,
    val anchor: CalcAnchor = CalcAnchor.COFFEE,
    val coffeeGrams: Double = 15.0,
    val waterGrams: Double = 240.0,
) {
    val isEspresso: Boolean get() = ratio <= ESPRESSO_MAX_RATIO

    /** Quanto chega à xícara: no filtrado, o pó segura cerca de 2 g de água por grama. */
    val cupGrams: Double get() = if (isEspresso) waterGrams else (waterGrams - ABSORPTION * coffeeGrams).coerceAtLeast(0.0)

    fun withRatio(value: Double): CalcState = copy(ratio = roundTo(value.coerceIn(MIN_RATIO, MAX_RATIO), 0.1)).recalc()
    fun withCoffee(grams: Double): CalcState =
        copy(anchor = CalcAnchor.COFFEE, coffeeGrams = roundTo(grams.coerceIn(0.0, MAX_COFFEE), 0.1)).recalc()
    fun withWater(grams: Double): CalcState =
        copy(anchor = CalcAnchor.WATER, waterGrams = roundTo(grams.coerceIn(0.0, MAX_WATER), 1.0)).recalc()

    /** Passos dos botões − e +: proporção de 0,5 em 0,5 (espresso de 0,1), café de 0,5 g, água de 10 g (espresso 1 g). */
    fun stepRatio(up: Boolean): CalcState = withRatio(stepFrom(ratio, if (isEspresso) 0.1 else 0.5, up))
    fun stepCoffee(up: Boolean): CalcState = withCoffee(stepFrom(coffeeGrams, 0.5, up).coerceAtLeast(0.5))
    fun stepWater(up: Boolean): CalcState = withWater(stepFrom(waterGrams, if (isEspresso) 1.0 else 10.0, up).coerceAtLeast(1.0))

    private fun recalc(): CalcState = when (anchor) {
        CalcAnchor.COFFEE -> copy(waterGrams = roundTo(coffeeGrams * ratio, if (isEspresso) 0.1 else 1.0))
        CalcAnchor.WATER -> copy(coffeeGrams = if (ratio > 0) roundTo(waterGrams / ratio, 0.1) else coffeeGrams)
    }

    companion object {
        const val MIN_RATIO = 1.0
        const val MAX_RATIO = 25.0
        const val ESPRESSO_MAX_RATIO = 3.0
        const val ABSORPTION = 2.0
        const val MAX_COFFEE = 500.0
        const val MAX_WATER = 5000.0

        /** Atalhos de proporção. */
        val presets: List<Pair<String, Double>> = listOf("1:15" to 15.0, "1:16" to 16.0, "1:17" to 17.0, "1:2 espresso" to 2.0)

        fun roundTo(value: Double, step: Double): Double = (value / step).roundToInt() * step

        /** Próximo múltiplo do passo (para cima ou para baixo), mesmo partindo de um valor quebrado. */
        fun stepFrom(value: Double, step: Double, up: Boolean): Double {
            val k = value / step
            val snapped = if (up) kotlin.math.floor(k + 1e-6) + 1 else kotlin.math.ceil(k - 1e-6) - 1
            return roundTo(snapped * step, step / 10)
        }
    }
}

/** 16.0 -> "1:16", 16.5 -> "1:16,5". */
fun formatRatioValue(ratio: Double): String = "1:${formatNumber(ratio)}"

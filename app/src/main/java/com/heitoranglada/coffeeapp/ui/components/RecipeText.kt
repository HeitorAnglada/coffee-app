package com.heitoranglada.coffeeapp.ui.components

import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.data.waterGrams
import com.heitoranglada.coffeeapp.domain.EspressoWindows
import com.heitoranglada.coffeeapp.domain.StepKind
import com.heitoranglada.coffeeapp.domain.espressoStyle
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.domain.formatRatio

/** "25–30 s". */
fun windowText(min: Int, max: Int): String = "$min–$max s"

val Recipe.windowMin: Int get() = windowMinSeconds ?: EspressoWindows.DEFAULT_MIN
val Recipe.windowMax: Int get() = windowMaxSeconds ?: EspressoWindows.DEFAULT_MAX

/** "18 g → 36 g". */
fun doseToYield(dose: Double?, yield: Double?): String? = when {
    dose != null && yield != null -> "${formatNumber(dose)} g → ${formatGrams(yield)}"
    dose != null -> formatGrams(dose)
    else -> null
}

/** Resumo curto da receita para pílulas (lista e cartões). */
fun Recipe.shortPills(): List<String> = if (method.isEspresso) {
    listOfNotNull(
        doseToYield(doseGrams, yieldGrams),
        formatRatio(doseGrams, yieldGrams),
        windowText(windowMin, windowMax),
    )
} else {
    val dilution = pours.filter { it.kind == StepKind.DILUTE }.sumOf { it.grams }
    listOfNotNull(
        doseGrams?.let(::formatGrams),
        when {
            dilution > 0 -> "${formatNumber(waterGrams - dilution)} + ${formatGrams(dilution)}"
            waterGrams > 0 -> formatGrams(waterGrams)
            else -> null
        },
        targetTimeSeconds?.let(::formatDuration),
    )
}

/** Pílulas do topo da receita, mais completas. */
fun Recipe.heroPills(): List<String> = if (method.isEspresso) {
    listOfNotNull(
        listOfNotNull(formatRatio(doseGrams, yieldGrams), espressoStyle(doseGrams, yieldGrams)).joinToString(" · ").ifBlank { null },
        windowText(windowMin, windowMax),
        waterTempC?.let { "$it °C" },
    )
} else {
    val dilution = pours.filter { it.kind == StepKind.DILUTE }.sumOf { it.grams }
    listOfNotNull(
        doseGrams?.let { "${formatGrams(it)} de café" },
        when {
            dilution > 0 -> "${formatNumber(waterGrams - dilution)} g + ${formatGrams(dilution)} de água"
            waterGrams > 0 -> "${formatGrams(waterGrams)} de água"
            else -> null
        },
        formatRatio(doseGrams, waterGrams),
        targetTimeSeconds?.let(::formatDuration),
        if (inverted) "Invertida" else null,
    )
}

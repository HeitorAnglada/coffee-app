package com.heitoranglada.coffeeapp.ui.components

import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.EspressoWindows
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.StepKind
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.domain.parseDecimal
import com.heitoranglada.coffeeapp.domain.parseDuration

/** Linha editável de etapa (texto cru digitado pela pessoa). */
data class PourInput(val startText: String, val gramsText: String, val kind: StepKind = StepKind.POUR) {
    fun toPour(): Pour? {
        val start = parseDuration(startText) ?: return null
        val grams = if (kind.addsWater) parseDecimal(gramsText) ?: return null else 0.0
        return Pour(start, grams, kind)
    }

    /** Etapas com água ainda sem gramas. */
    val missingGrams: Boolean get() = kind.addsWater && gramsText.isBlank()

    companion object {
        fun from(pour: Pour) = PourInput(
            startText = formatDuration(pour.startSeconds),
            gramsText = if (pour.grams > 0) formatNumber(pour.grams) else "",
            kind = pour.kind,
        )
    }
}

/** Campos comuns entre o cadastro de receita e o registro de preparo. */
data class CoffeeForm(
    val name: String = "",
    val method: BrewMethod = BrewMethod.V60,
    val coffee: String = "",
    val grinder: String = "",
    val grindSetting: String = "",
    val doseText: String = "",
    val pours: List<PourInput> = listOf(PourInput("0:00", "")),
    val waterTempText: String = "",
    /** Tempo-alvo (receita) ou tempo total medido (preparo), em "m:ss". */
    val timeText: String = "",
    val rating: Int? = null,
    val notes: String = "",
    /** AeroPress de cabeça para baixo. */
    val inverted: Boolean = false,
    /** Espresso: gramas de bebida na xícara. */
    val yieldText: String = "",
    val windowMin: Int = EspressoWindows.DEFAULT_MIN,
    val windowMax: Int = EspressoWindows.DEFAULT_MAX,
    val preInfusionText: String = "",
) {
    val dose: Double? get() = parseDecimal(doseText)
    val parsedPours: List<Pour> get() = if (method.isEspresso) emptyList() else pours.mapNotNull { it.toPour() }.sortedBy { it.startSeconds }
    val waterGrams: Double get() = parsedPours.filter { it.kind.addsWater }.sumOf { it.grams }
    val waterTemp: Int? get() = waterTempText.trim().toIntOrNull()
    val timeSeconds: Int? get() = parseDuration(timeText)
    val yieldGrams: Double? get() = parseDecimal(yieldText)
    val preInfusion: Int? get() = preInfusionText.trim().toIntOrNull()

    /** Lista de problemas que impedem salvar (vazia = ok). */
    fun validate(requireName: Boolean): List<String> = buildList {
        if (requireName && name.isBlank()) add("Dê um nome à receita.")
        if (doseText.isNotBlank() && dose == null) add("Dose de café inválida.")
        if (method.isEspresso) {
            if (yieldText.isNotBlank() && yieldGrams == null) add("Rendimento inválido.")
            if (preInfusionText.isNotBlank() && preInfusion == null) add("Pré-infusão inválida (em segundos).")
        } else {
            if (pours.none { it.kind.addsWater }) add("Adicione pelo menos um despejo.")
            pours.forEachIndexed { i, p ->
                if (parseDuration(p.startText) == null) add("Etapa ${i + 1}: tempo inválido (use m:ss).")
                if (p.kind.addsWater && parseDecimal(p.gramsText) == null) add("Etapa ${i + 1}: informe as gramas.")
            }
        }
        if (waterTempText.isNotBlank() && waterTemp == null) add("Temperatura inválida.")
        if (timeText.isNotBlank() && timeSeconds == null) add("Tempo inválido (use m:ss).")
    }
}

/**
 * Troca o método no cadastro e, se a pessoa ainda não mexeu nos números, já sugere um ponto de
 * partida do método novo (espresso 18 g → 36 g; AeroPress com mexer e prensar).
 */
fun CoffeeForm.withMethod(m: BrewMethod): CoffeeForm {
    val untouchedPours = pours.all { it.gramsText.isBlank() && it.kind == StepKind.POUR } && pours.size <= 1
    return when {
        m == BrewMethod.ESPRESSO && yieldText.isBlank() -> copy(method = m, doseText = "18", yieldText = "36")
        m == BrewMethod.AEROPRESS && untouchedPours -> copy(
            method = m,
            doseText = if (doseText.isBlank() || method.isEspresso) "16" else doseText,
            pours = listOf(
                PourInput("0:00", "200"),
                PourInput("0:10", "", StepKind.STIR),
                PourInput("1:00", "", StepKind.PRESS),
            ),
            timeText = timeText.ifBlank { "1:30" },
        )
        method.isEspresso && !m.isEspresso && doseText == "18" -> copy(method = m, doseText = "15")
        else -> copy(method = m)
    }
}

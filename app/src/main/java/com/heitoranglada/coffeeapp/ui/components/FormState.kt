package com.heitoranglada.coffeeapp.ui.components

import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.domain.parseDecimal
import com.heitoranglada.coffeeapp.domain.parseDuration

/** Linha editável de despejo (texto cru digitado pela pessoa). */
data class PourInput(val startText: String, val gramsText: String) {
    fun toPour(): Pour? {
        val start = parseDuration(startText) ?: return null
        val grams = parseDecimal(gramsText) ?: return null
        return Pour(start, grams)
    }

    companion object {
        fun from(pour: Pour) = PourInput(
            startText = formatDuration(pour.startSeconds),
            gramsText = if (pour.grams > 0) formatNumber(pour.grams) else "",
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
) {
    val dose: Double? get() = parseDecimal(doseText)
    val parsedPours: List<Pour> get() = pours.mapNotNull { it.toPour() }.sortedBy { it.startSeconds }
    val waterGrams: Double get() = parsedPours.sumOf { it.grams }
    val waterTemp: Int? get() = waterTempText.trim().toIntOrNull()
    val timeSeconds: Int? get() = parseDuration(timeText)

    /** Lista de problemas que impedem salvar (vazia = ok). */
    fun validate(requireName: Boolean): List<String> = buildList {
        if (requireName && name.isBlank()) add("Dê um nome à receita.")
        if (doseText.isNotBlank() && dose == null) add("Dose de café inválida.")
        if (pours.isEmpty()) add("Adicione pelo menos um despejo.")
        pours.forEachIndexed { i, p ->
            if (parseDuration(p.startText) == null) add("Despejo ${i + 1}: tempo inválido (use m:ss).")
            if (parseDecimal(p.gramsText) == null) add("Despejo ${i + 1}: informe as gramas.")
        }
        if (waterTempText.isNotBlank() && waterTemp == null) add("Temperatura inválida.")
        if (timeText.isNotBlank() && timeSeconds == null) add("Tempo inválido (use m:ss).")
    }
}

package com.heitoranglada.coffeeapp.data

import android.content.Context
import com.heitoranglada.coffeeapp.domain.CalcAnchor
import com.heitoranglada.coffeeapp.domain.CalcState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Guarda a última conta da calculadora (proporção e quantidades) e, quando a pessoa toca em
 * "Preparar livre com isso", o plano que o preparo livre usa para preencher a dose.
 */
class CalculatorStore(context: Context) {
    private val prefs = context.getSharedPreferences("coffee_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(load())
    val state: StateFlow<CalcState> = _state.asStateFlow()

    private val _plan = MutableStateFlow<CalcState?>(null)
    /** Conta escolhida para o próximo preparo livre (null = nenhuma). */
    val plan: StateFlow<CalcState?> = _plan.asStateFlow()

    fun update(transform: (CalcState) -> CalcState) {
        val next = transform(_state.value)
        _state.value = next
        prefs.edit()
            .putFloat(KEY_RATIO, next.ratio.toFloat())
            .putFloat(KEY_COFFEE, next.coffeeGrams.toFloat())
            .putFloat(KEY_WATER, next.waterGrams.toFloat())
            .putString(KEY_ANCHOR, next.anchor.name)
            .apply()
    }

    fun usePlan() { _plan.value = _state.value }
    fun clearPlan() { _plan.value = null }

    private fun load(): CalcState {
        if (!prefs.contains(KEY_RATIO)) return CalcState()
        return CalcState(
            ratio = prefs.getFloat(KEY_RATIO, 16f).toDouble(),
            anchor = CalcAnchor.entries.firstOrNull { it.name == prefs.getString(KEY_ANCHOR, null) } ?: CalcAnchor.COFFEE,
            coffeeGrams = prefs.getFloat(KEY_COFFEE, 15f).toDouble(),
            waterGrams = prefs.getFloat(KEY_WATER, 240f).toDouble(),
        )
    }

    private companion object {
        const val KEY_RATIO = "calc_ratio"
        const val KEY_COFFEE = "calc_coffee"
        const val KEY_WATER = "calc_water"
        const val KEY_ANCHOR = "calc_anchor"
    }
}

package com.heitoranglada.coffeeapp.domain

/** Janelas de tempo de extração mais usadas, em segundos. */
object EspressoWindows {
    const val DEFAULT_MIN = 25
    const val DEFAULT_MAX = 30
    val presets: List<Pair<Int, Int>> = listOf(22 to 26, 25 to 30, 28 to 35)
}

/** Estilo pela proporção café → bebida: ristretto (até 1:1,5), normale, lungo (a partir de 1:2,6). */
fun espressoStyle(doseGrams: Double?, yieldGrams: Double?): String? {
    if (doseGrams == null || yieldGrams == null || doseGrams <= 0 || yieldGrams <= 0) return null
    val r = yieldGrams / doseGrams
    return when {
        r < 1.6 -> "ristretto"
        r < 2.6 -> "normale"
        else -> "lungo"
    }
}

/** Como o shot saiu em relação à janela. */
enum class ShotSpeed(val label: String) {
    TOO_FAST("muito rápido"),
    FAST("rápido"),
    IN_WINDOW("na janela"),
    SLOW("lento"),
    TOO_SLOW("muito lento"),
}

fun shotSpeed(seconds: Double, windowMin: Int, windowMax: Int): ShotSpeed = when {
    seconds < windowMin - 5 -> ShotSpeed.TOO_FAST
    seconds < windowMin -> ShotSpeed.FAST
    seconds <= windowMax + 0.5 -> ShotSpeed.IN_WINDOW
    seconds <= windowMax + 5 -> ShotSpeed.SLOW
    else -> ShotSpeed.TOO_SLOW
}

data class DialTip(val title: String, val body: String)

/** Dica de ajuste depois do shot, pela tabela clássica de calibragem. */
fun espressoTip(seconds: Double, windowMin: Int, windowMax: Int): DialTip = when (shotSpeed(seconds, windowMin, windowMax)) {
    ShotSpeed.TOO_FAST -> DialTip("Saiu rápido demais", "Moa 1 a 2 pontos mais fino e confira se a dose está nivelada.")
    ShotSpeed.FAST -> DialTip("Quase lá", "Moa 1 ponto mais fino para entrar na janela.")
    ShotSpeed.IN_WINDOW -> DialTip("Moagem no ponto", "Tempo certo. Se ficou azedo, tire 2 a 4 g a mais; se amargou, 2 a 4 g a menos.")
    ShotSpeed.SLOW -> DialTip("Saiu devagar", "Moa 1 ponto mais grosso.")
    ShotSpeed.TOO_SLOW -> DialTip("Saiu devagar demais", "Moa 2 pontos mais grosso.")
}

/** 27.43 -> "27,4". */
fun formatShotSeconds(seconds: Double): String = String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.1f", seconds)

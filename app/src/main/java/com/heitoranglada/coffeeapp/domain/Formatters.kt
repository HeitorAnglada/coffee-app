package com.heitoranglada.coffeeapp.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

private val ptBR: Locale = Locale.forLanguageTag("pt-BR")

/** 95 -> "1:35". */
fun formatDuration(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

/** "1:35" -> 95, "95" -> 95, "1:5" -> 65. Retorna null se inválido. */
fun parseDuration(text: String): Int? {
    val t = text.trim()
    if (t.isEmpty()) return null
    val parts = t.split(":")
    return when (parts.size) {
        1 -> parts[0].toIntOrNull()?.takeIf { it >= 0 }
        2 -> {
            val m = parts[0].ifEmpty { "0" }.toIntOrNull() ?: return null
            val s = parts[1].toIntOrNull() ?: return null
            if (m < 0 || s !in 0..59) null else m * 60 + s
        }
        else -> null
    }
}

/** Aceita vírgula ou ponto como separador decimal. */
fun parseDecimal(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }

/** 15.0 -> "15", 15.5 -> "15,5". */
fun formatNumber(value: Double): String {
    val rounded = (value * 10).roundToLong() / 10.0
    return if (abs(rounded - rounded.roundToLong()) < 1e-9) {
        rounded.roundToLong().toString()
    } else {
        String.format(ptBR, "%.1f", rounded)
    }
}

fun formatGrams(value: Double): String = "${formatNumber(value)} g"

/** Proporção café:água, ex.: dose 15 e água 250 -> "1:16,7". */
fun formatRatio(doseGrams: Double?, waterGrams: Double?): String? {
    if (doseGrams == null || waterGrams == null || doseGrams <= 0.0 || waterGrams <= 0.0) return null
    return "1:${formatNumber(waterGrams / doseGrams)}"
}

fun formatDateTime(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", ptBR).format(Date(millis))

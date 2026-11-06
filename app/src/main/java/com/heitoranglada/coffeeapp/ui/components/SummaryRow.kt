package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.domain.formatRatio

/** Faixa com os números principais: dose, água, proporção e tempo. */
@Composable
fun SummaryRow(dose: Double?, water: Double, timeSeconds: Int?, timeLabel: String = "Tempo") {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Stat("Café", dose?.let { formatGrams(it) } ?: "—")
        Stat("Água", if (water > 0) formatGrams(water) else "—")
        Stat("Proporção", formatRatio(dose, water) ?: "—")
        Stat(timeLabel, timeSeconds?.takeIf { it > 0 }?.let { formatDuration(it) } ?: "—")
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

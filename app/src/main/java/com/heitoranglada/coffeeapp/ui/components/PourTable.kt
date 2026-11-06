package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.cumulativeGramsAt
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams

/** Tabela de despejos: nº, início, gramas do despejo e total acumulado na balança. */
@Composable
fun PourTable(pours: List<Pour>, highlightIndex: Int = -1) {
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
            HeaderCell("#", 0.5f)
            HeaderCell("Início", 1f)
            HeaderCell("Despejo", 1f)
            HeaderCell("Balança", 1f)
        }
        pours.forEachIndexed { i, p ->
            val highlighted = i == highlightIndex
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                val weight = if (highlighted) FontWeight.Bold else FontWeight.Normal
                Cell("${i + 1}", 0.5f, weight)
                Cell(formatDuration(p.startSeconds), 1f, weight)
                Cell(if (p.grams > 0) formatGrams(p.grams) else "—", 1f, weight)
                Cell(formatGrams(pours.cumulativeGramsAt(i)), 1f, weight)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start,
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Cell(text: String, weight: Float, fontWeight: FontWeight) {
    Text(text, modifier = Modifier.weight(weight), style = MaterialTheme.typography.bodyMedium, fontWeight = fontWeight)
}

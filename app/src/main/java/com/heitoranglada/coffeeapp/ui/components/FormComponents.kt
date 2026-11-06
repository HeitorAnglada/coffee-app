package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.domain.formatRatio
import com.heitoranglada.coffeeapp.domain.parseDuration

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
fun MethodSelector(selected: BrewMethod, onSelect: (BrewMethod) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BrewMethod.entries.forEach { m ->
                FilterChip(
                    selected = m == selected,
                    onClick = { onSelect(m) },
                    label = { Text(m.label) },
                    leadingIcon = { Icon(painterResource(m.iconRes()), contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
            }
        }
        Text(selected.tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Campo de texto com sugestões de valores já usados (ex.: cafés e moedores). */
@Composable
fun SuggestField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    placeholder: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FormTextField(value, onValueChange, label, placeholder = placeholder)
        val filtered = suggestions.filter { it != value && it.contains(value.trim(), ignoreCase = true) }.take(8)
        if (filtered.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { s -> SuggestionChip(onClick = { onValueChange(s) }, label = { Text(s) }) }
            }
        }
    }
}

/** Editor da lista de despejos: tempo de início + gramas de cada um. */
@Composable
fun PoursEditor(pours: List<PourInput>, onChange: (List<PourInput>) -> Unit, dose: Double?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pours.forEachIndexed { index, pour ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${index + 1}.",
                    modifier = Modifier.width(24.dp),
                    style = MaterialTheme.typography.titleSmall,
                )
                OutlinedTextField(
                    value = pour.startText,
                    onValueChange = { v -> onChange(pours.toMutableList().also { it[index] = pour.copy(startText = v) }) },
                    label = { Text("Início") },
                    placeholder = { Text("0:45") },
                    singleLine = true,
                    isError = pour.startText.isNotBlank() && parseDuration(pour.startText) == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = pour.gramsText,
                    onValueChange = { v -> onChange(pours.toMutableList().also { it[index] = pour.copy(gramsText = v) }) },
                    label = { Text("Água") },
                    suffix = { Text("g") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onChange(pours.toMutableList().also { it.removeAt(index) }) },
                    enabled = pours.size > 1,
                ) { Icon(Icons.Filled.Close, contentDescription = "Remover despejo") }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                val last = pours.lastOrNull()?.toPour()
                val nextStart = (last?.startSeconds ?: -30) + 30
                onChange(pours + PourInput(formatDuration(nextStart), ""))
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Adicionar despejo")
            }
            Spacer(Modifier.weight(1f))
            val total = pours.mapNotNull { it.toPour() }.sumOf { it.grams }
            Column(horizontalAlignment = Alignment.End) {
                Text("Água total: ${formatGrams(total)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                formatRatio(dose, total)?.let {
                    Text("Proporção $it", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Seção recolhível para opções que iniciantes podem ignorar. */
@Composable
fun AdvancedSection(title: String = "Opções avançadas", content: @Composable () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
        }
        AnimatedVisibility(expanded) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { content() }
        }
    }
}

@Composable
fun RatingBar(rating: Int?, onChange: ((Int?) -> Unit)? = null) {
    Row {
        (1..5).forEach { star ->
            val filled = rating != null && star <= rating
            val icon = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder
            if (onChange != null) {
                IconButton(onClick = { onChange(if (rating == star) null else star) }) {
                    Icon(icon, contentDescription = "$star estrelas", tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** Linha "rótulo: valor" usada nas telas de detalhe. */
@Composable
fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

/** Mostra problemas de validação do formulário. */
@Composable
fun ErrorCard(errors: List<String>) {
    if (errors.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            errors.forEach {
                Text("• $it", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

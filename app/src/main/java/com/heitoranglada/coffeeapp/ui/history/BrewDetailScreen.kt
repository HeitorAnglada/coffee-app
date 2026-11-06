package com.heitoranglada.coffeeapp.ui.history

import com.heitoranglada.coffeeapp.data.waterGrams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.domain.formatDateTime
import com.heitoranglada.coffeeapp.ui.components.InfoRow
import com.heitoranglada.coffeeapp.ui.components.PourTable
import com.heitoranglada.coffeeapp.ui.components.RatingBar
import com.heitoranglada.coffeeapp.ui.components.SectionCard
import com.heitoranglada.coffeeapp.ui.components.SummaryRow
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewDetailScreen(
    brewId: Long,
    onBack: () -> Unit,
    onSaveAsRecipe: (Long) -> Unit,
    onBrewAgain: (Long) -> Unit,
    vm: BrewDetailViewModel = viewModel(
        key = "brew-detail-$brewId",
        factory = repoViewModelFactory { BrewDetailViewModel(it, brewId) },
    ),
) {
    val brew by vm.brew.collectAsStateWithLifecycle()
    val recipe by vm.recipe.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preparo") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
                actions = {
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Excluir") }
                },
            )
        },
    ) { padding ->
        val b = brew
        if (b == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard("${b.method.label} · ${b.recipeName ?: "Preparo livre"}") {
                Text(formatDateTime(b.brewedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SummaryRow(b.doseGrams, b.waterGrams, b.totalTimeSeconds)
                if (b.rating != null) RatingBar(b.rating)
            }
            SectionCard("Detalhes") {
                Column {
                    InfoRow("Café", b.coffee.ifBlank { "—" })
                    InfoRow("Moedor", b.grinder.ifBlank { "—" })
                    InfoRow("Ajuste de moagem", b.grindSetting.ifBlank { "—" })
                    InfoRow("Temperatura da água", b.waterTempC?.let { "$it °C" })
                }
            }
            SectionCard("Despejos") { PourTable(b.pours) }
            if (b.notes.isNotBlank()) SectionCard("Notas") { Text(b.notes) }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onSaveAsRecipe(b.id) }, modifier = Modifier.weight(1f).height(52.dp)) {
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Salvar como receita")
                }
                val r = recipe
                if (r != null) {
                    Button(onClick = { onBrewAgain(r.id) }, modifier = Modifier.weight(1f).height(52.dp)) {
                        Icon(Icons.Filled.Replay, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Preparar de novo")
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir preparo?") },
            text = { Text("Ele será removido do histórico.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(onBack) }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

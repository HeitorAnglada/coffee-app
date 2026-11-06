package com.heitoranglada.coffeeapp.ui.recipes

import com.heitoranglada.coffeeapp.data.waterGrams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.heitoranglada.coffeeapp.ui.components.InfoRow
import com.heitoranglada.coffeeapp.ui.components.PourTable
import com.heitoranglada.coffeeapp.ui.components.SectionCard
import com.heitoranglada.coffeeapp.ui.components.SummaryRow
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onBrew: (Long) -> Unit,
    vm: RecipeDetailViewModel = viewModel(
        key = "recipe-$recipeId",
        factory = repoViewModelFactory { RecipeDetailViewModel(it, recipeId) },
    ),
) {
    val recipe by vm.recipe.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.name ?: "Receita") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
                actions = {
                    IconButton(onClick = { onEdit(recipeId) }) { Icon(Icons.Filled.Edit, contentDescription = "Editar") }
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Excluir") }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { onBrew(recipeId) },
                    enabled = recipe != null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Preparar esta receita", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
    ) { padding ->
        val r = recipe
        if (r == null) {
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
            SectionCard(r.method.label) {
                SummaryRow(r.doseGrams, r.waterGrams, r.targetTimeSeconds)
                Text(r.method.tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SectionCard("Detalhes") {
                Column {
                    InfoRow("Café", r.coffee.ifBlank { "—" })
                    InfoRow("Moedor", r.grinder.ifBlank { "—" })
                    InfoRow("Ajuste de moagem", r.grindSetting.ifBlank { "—" })
                    InfoRow("Temperatura da água", r.waterTempC?.let { "$it °C" })
                }
            }
            SectionCard("Despejos") { PourTable(r.pours) }
            if (r.notes.isNotBlank()) {
                SectionCard("Notas") { Text(r.notes) }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir receita?") },
            text = { Text("Os preparos já salvos no histórico continuam lá.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.delete(onBack) }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

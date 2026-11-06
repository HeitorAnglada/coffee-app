package com.heitoranglada.coffeeapp.ui.brew

import com.heitoranglada.coffeeapp.data.waterGrams
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.domain.cumulativeGramsAt
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.domain.guidedProgress
import com.heitoranglada.coffeeapp.ui.components.CoffeeFormFields
import com.heitoranglada.coffeeapp.ui.components.ErrorCard
import com.heitoranglada.coffeeapp.ui.components.FormTextField
import com.heitoranglada.coffeeapp.ui.components.MethodSelector
import com.heitoranglada.coffeeapp.ui.components.PourTable
import com.heitoranglada.coffeeapp.ui.components.SectionCard
import com.heitoranglada.coffeeapp.ui.components.SummaryRow
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory

/**
 * Tela de preparo. Com [recipeId] == 0 é o "preparo livre"; caso contrário, guia a receita.
 * @param onBack null quando a tela é uma aba (sem botão voltar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewScreen(
    recipeId: Long,
    onBack: (() -> Unit)?,
    onSaved: (Long) -> Unit,
    vm: BrewViewModel = viewModel(
        key = "brew-$recipeId",
        factory = repoViewModelFactory { BrewViewModel(it, recipeId) },
    ),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmDiscard by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedBrewId) {
        state.savedBrewId?.let {
            onSaved(it)
            vm.onSavedHandled()
        }
    }

    // Mantém a tela ligada enquanto o timer corre.
    val view = LocalView.current
    val running = state.phase == BrewPhase.RUNNING
    DisposableEffect(running) {
        view.keepScreenOn = running
        onDispose { view.keepScreenOn = false }
    }

    BackHandler(enabled = state.phase != BrewPhase.READY) { confirmDiscard = true }

    val title = when (state.phase) {
        BrewPhase.REVIEW -> "Salvar preparo"
        else -> state.recipe?.name ?: "Preparo livre"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = {
                            if (state.phase == BrewPhase.READY) onBack() else confirmDiscard = true
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.phase == BrewPhase.READY -> ReadyContent(state, vm)
                state.phase == BrewPhase.RUNNING -> RunningContent(state, vm)
                else -> ReviewContent(state, vm)
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Descartar preparo?") },
            text = { Text("O timer e os despejos marcados serão perdidos.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    vm.reset()
                    onBack?.invoke()
                }) { Text("Descartar") }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Continuar") } },
        )
    }
}

@Composable
private fun ReadyContent(state: BrewUiState, vm: BrewViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val recipe = state.recipe
        if (recipe != null) {
            SectionCard(recipe.method.label) {
                SummaryRow(recipe.doseGrams, recipe.waterGrams, recipe.targetTimeSeconds)
                val details = listOfNotNull(
                    recipe.coffee.takeIf { it.isNotBlank() },
                    listOf(recipe.grinder, recipe.grindSetting).filter { it.isNotBlank() }.joinToString(" · ").takeIf { it.isNotBlank() },
                    recipe.waterTempC?.let { "Água a $it °C" },
                )
                details.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            SectionCard("Despejos") { PourTable(recipe.pours) }
            if (recipe.notes.isNotBlank()) SectionCard("Notas") { Text(recipe.notes) }
            Text(
                "Prepare a balança e o café moído. Toque em Iniciar no momento do primeiro despejo: o app avisa quando fazer cada um.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            SectionCard("Método") { MethodSelector(state.method, vm::selectMethod) }
            SectionCard("Como funciona") {
                Text("1. Toque em Iniciar preparo junto com o primeiro despejo. O timer começa a contar.")
                Text("2. A cada novo despejo, toque em Novo despejo.")
                Text("3. No final, toque em Finalizar e informe quantos gramas teve cada despejo.")
                Text("Depois, você pode salvar o preparo como receita.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = vm::start,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(160.dp),
            shape = CircleShape,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(48.dp))
                Text("Iniciar\npreparo", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun RunningContent(state: BrewUiState, vm: BrewViewModel) {
    val elapsedMs by vm.elapsedMs.collectAsStateWithLifecycle()
    val elapsed = (elapsedMs / 1000).toInt()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            formatDuration(elapsed),
            fontSize = 88.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        val recipe = state.recipe
        if (recipe != null) {
            GuidedRunning(recipe, elapsed)
        } else {
            FreeRunning(state, vm)
        }
        Button(
            onClick = vm::finish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Finalizar", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FreeRunning(state: BrewUiState, vm: BrewViewModel) {
    val haptic = LocalHapticFeedback.current
    Text(
        "Despejos marcados: ${state.pourMarks.size}",
        style = MaterialTheme.typography.titleMedium,
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(state.pourMarks) { i, s ->
            AssistChip(onClick = {}, label = { Text("${i + 1}º · ${formatDuration(s)}") })
        }
    }
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            vm.markPour()
        },
        modifier = Modifier.fillMaxWidth().height(120.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Icon(Icons.Filled.WaterDrop, contentDescription = null, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Text("Novo despejo", style = MaterialTheme.typography.headlineSmall)
    }
    OutlinedButton(onClick = vm::undoLastPour, enabled = state.pourMarks.size > 1) {
        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Desfazer último despejo")
    }
}

@Composable
private fun GuidedRunning(recipe: Recipe, elapsed: Int) {
    val pours = remember(recipe) { recipe.pours.sortedBy { it.startSeconds } }
    val progress = guidedProgress(pours, elapsed)
    val haptic = LocalHapticFeedback.current

    // Vibra quando chega a hora de um novo despejo.
    LaunchedEffect(progress.currentIndex) {
        if (progress.currentIndex >= 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val current = progress.currentIndex
            if (current >= 0) {
                val pour = pours[current]
                Text(
                    "Despejo ${current + 1} de ${pours.size}",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    "Despeje ${formatGrams(pour.grams)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Balança até ${formatGrams(pours.cumulativeGramsAt(current))}",
                    style = MaterialTheme.typography.titleMedium,
                )
            } else {
                Text("Aguarde o primeiro despejo", style = MaterialTheme.typography.titleMedium)
            }
            val next = progress.nextIndex
            if (next != null && progress.secondsToNext != null) {
                Text(
                    "Próximo em ${formatDuration(progress.secondsToNext)} (até ${formatGrams(pours.cumulativeGramsAt(next))})",
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                val target = recipe.targetTimeSeconds
                Text(
                    if (target != null && elapsed < target) {
                        "Último despejo. Aguarde a extração até ${formatDuration(target)}."
                    } else if (target != null) {
                        "Tempo-alvo atingido! Toque em Finalizar."
                    } else {
                        "Último despejo. Toque em Finalizar quando terminar."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
    recipe.targetTimeSeconds?.takeIf { it > 0 }?.let { target ->
        Column(Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { (elapsed.toFloat() / target).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Tempo-alvo ${formatDuration(target)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
    PourTable(pours, highlightIndex = progress.currentIndex)
}

@Composable
private fun ReviewContent(state: BrewUiState, vm: BrewViewModel) {
    val coffees by vm.coffees.collectAsStateWithLifecycle()
    val grinders by vm.grinders.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Tempo total: ${formatDuration(state.totalSeconds)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (!state.isGuided) {
            Text(
                "Informe quantos gramas teve cada despejo. Os tempos marcados já estão preenchidos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CoffeeFormFields(
            form = state.form,
            onChange = vm::onFormChange,
            coffees = coffees,
            grinders = grinders,
            showName = false,
            showRating = true,
            timeLabel = "Tempo total",
            poursHint = "Início de cada despejo (m:ss) e gramas de água despejadas.",
            poursFirst = !state.isGuided,
        )
        SectionCard(if (state.isGuided) "Salvar como nova receita" else "Virar receita") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Salvar também como receita",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(checked = state.saveAsRecipe, onCheckedChange = vm::setSaveAsRecipe)
            }
            if (state.saveAsRecipe) {
                FormTextField(
                    state.form.name,
                    { vm.onFormChange(state.form.copy(name = it)) },
                    "Nome da receita",
                )
            }
        }
        ErrorCard(state.errors)
        Button(onClick = vm::save, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Salvar no histórico", style = MaterialTheme.typography.titleMedium)
        }
        TextButton(onClick = vm::reset, modifier = Modifier.fillMaxWidth()) { Text("Descartar preparo") }
    }
}

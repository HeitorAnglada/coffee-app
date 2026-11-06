package com.heitoranglada.coffeeapp.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.data.waterGrams
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.cumulativeGramsAt
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.domain.formatRatio
import com.heitoranglada.coffeeapp.ui.components.BigPillButton
import com.heitoranglada.coffeeapp.ui.components.Blob
import com.heitoranglada.coffeeapp.ui.components.BloomDialog
import com.heitoranglada.coffeeapp.ui.components.CircleIconButton
import com.heitoranglada.coffeeapp.ui.components.Eyebrow
import com.heitoranglada.coffeeapp.ui.components.MethodHero
import com.heitoranglada.coffeeapp.ui.components.Pill
import com.heitoranglada.coffeeapp.ui.components.PillFlow
import com.heitoranglada.coffeeapp.ui.components.SectionTitle
import com.heitoranglada.coffeeapp.ui.components.Sheet
import com.heitoranglada.coffeeapp.ui.components.TextAction
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import com.heitoranglada.coffeeapp.ui.theme.palette

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
    val r = recipe

    if (r == null) {
        Box(Modifier.fillMaxSize().background(Bloom.Cream), contentAlignment = Alignment.Center) {
            Blob(Bloom.Terra, Modifier.size(110.dp), periodMs = 2000, wobble = 0.12f)
        }
        return
    }
    val p = r.method.palette
    val scroll = rememberScrollState()

    Column(Modifier.fillMaxSize().background(Bloom.Cream)) {
        Column(Modifier.weight(1f).verticalScroll(scroll)) {
            MethodHero(
                r.method,
                scroll = scroll,
                topBar = {
                    CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBack, background = Bloom.Cream.copy(alpha = 0.3f), contentColor = p.fg)
                    Spacer(Modifier.weight(1f))
                    TextAction("Editar", { onEdit(r.id) }, color = p.fg)
                    CircleIconButton(Icons.Filled.Delete, "Excluir receita", { confirmDelete = true }, background = Bloom.Cream.copy(alpha = 0.3f), contentColor = p.fg)
                },
            ) {
                Eyebrow(
                    listOf(r.method.label, r.coffee).filter { it.isNotBlank() }.joinToString(" · "),
                    Modifier.enterCascade(0),
                    color = p.fg.copy(alpha = 0.75f),
                )
                Text(r.name, style = BloomType.display(46.sp, -0.035f), color = p.fg, modifier = Modifier.padding(top = 6.dp).enterCascade(1))
                PillFlow(Modifier.padding(top = 16.dp).enterCascade(2)) {
                    r.doseGrams?.let { Pill("${formatGrams(it)} de café", p.chip, contentColor = p.fg) }
                    if (r.waterGrams > 0) Pill("${formatGrams(r.waterGrams)} de água", p.chip, contentColor = p.fg)
                    formatRatio(r.doseGrams, r.waterGrams)?.let { Pill(it, p.chip, contentColor = p.fg) }
                    r.targetTimeSeconds?.let { Pill(formatDuration(it), p.chip, contentColor = p.fg) }
                }
            }
            Sheet {
                val gear = listOf(r.grinder, r.grindSetting).filter { it.isNotBlank() }.joinToString(" · ")
                if (gear.isNotBlank() || r.waterTempC != null) {
                    PillFlow(Modifier.enterCascade(2)) {
                        if (gear.isNotBlank()) Pill(gear, Bloom.Sand)
                        r.waterTempC?.let { Pill("$it °C", Bloom.Sand) }
                    }
                }
                SectionTitle("Como fazer", Modifier.enterCascade(3))
                HowToSteps(r)
                if (r.notes.isNotBlank()) {
                    SectionTitle("Notas", Modifier.enterCascade(8))
                    Text(
                        r.notes,
                        style = BloomType.body,
                        color = Bloom.Body,
                        modifier = Modifier
                            .fillMaxWidth()
                            .enterCascade(9)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Bloom.Sand)
                            .padding(16.dp),
                    )
                }
                Spacer(Modifier.size(8.dp))
            }
        }
        BigPillButton(
            "Bora passar",
            { onBrew(r.id) },
            Modifier.navigationBarsPadding().padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 16.dp),
            leading = { Icon(Icons.Filled.PlayArrow, null, tint = Bloom.Terra, modifier = Modifier.size(26.dp)) },
        )
    }

    if (confirmDelete) {
        BloomDialog(
            title = "Excluir receita?",
            text = "Os preparos já salvos no histórico continuam lá.",
            confirm = "Excluir",
            onConfirm = { confirmDelete = false; vm.delete(onBack) },
            dismiss = "Cancelar",
            onDismiss = { confirmDelete = false },
        )
    }
}

/** Texto de apoio de cada despejo (bloom, meio, último). */
fun pourHint(method: BrewMethod, pours: List<Pour>, index: Int, targetSeconds: Int?): String {
    val total = pours.cumulativeGramsAt(index)
    val isLast = index == pours.lastIndex
    return when {
        method == BrewMethod.FRENCH_PRESS && isLast ->
            "Até ${formatGrams(total)}. Espere${targetSeconds?.let { " até ${formatDuration(it)}" } ?: ""}, quebre a crosta e pressione devagar."
        index == 0 && pours.size > 1 -> "Bloom: molhe todo o pó e espere ele inchar."
        isLast -> "Até ${formatGrams(total)}. Depois é só esperar escorrer."
        else -> "Em círculos, até a balança marcar ${formatGrams(total)}."
    }
}

@Composable
private fun HowToSteps(r: Recipe) {
    val pours = r.pours.sortedBy { it.startSeconds }
    if (pours.isEmpty()) {
        Text("Esta receita ainda não tem despejos.", style = BloomType.body, color = Bloom.Body)
        return
    }
    pours.forEachIndexed { i, pour ->
        Row(Modifier.fillMaxWidth().enterCascade(4 + i), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (i == 0) r.method.palette.bg else Bloom.Ink),
                contentAlignment = Alignment.Center,
            ) {
                Text("${i + 1}", style = BloomType.display(20.sp, 0f), color = if (i == 0) r.method.palette.fg else Bloom.Cream)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Despeje ${formatGrams(pour.grams)}", style = BloomType.display(20.sp, -0.02f), color = Bloom.Ink)
                    Text("  aos ${formatDuration(pour.startSeconds)}", style = BloomType.label.copy(fontSize = 13.sp), color = Bloom.Muted, modifier = Modifier.padding(bottom = 2.dp))
                }
                Text(pourHint(r.method, pours, i, r.targetTimeSeconds), style = BloomType.bodySmall.copy(fontSize = 14.sp), color = Bloom.Body, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

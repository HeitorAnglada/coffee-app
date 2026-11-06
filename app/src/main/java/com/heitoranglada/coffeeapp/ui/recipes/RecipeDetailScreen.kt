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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.heitoranglada.coffeeapp.domain.StepKind
import com.heitoranglada.coffeeapp.ui.components.MethodSilhouette
import com.heitoranglada.coffeeapp.ui.components.heroPills
import com.heitoranglada.coffeeapp.ui.components.icon
import com.heitoranglada.coffeeapp.ui.components.sway
import com.heitoranglada.coffeeapp.ui.components.windowMax
import com.heitoranglada.coffeeapp.ui.components.windowMin
import com.heitoranglada.coffeeapp.ui.components.windowText
import com.heitoranglada.coffeeapp.ui.theme.isDark
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
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.cumulativeGramsAt
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
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
                if (r.method.isEspresso) {
                    DoseToYield(r.doseGrams, r.yieldGrams, p.fg, Modifier.padding(top = 18.dp).enterCascade(2))
                }
                PillFlow(Modifier.padding(top = 16.dp).enterCascade(2)) {
                    r.heroPills().forEach { Pill(it, p.chip, contentColor = p.fg) }
                }
            }
            Sheet {
                val gear = listOf(r.grinder, r.grindSetting).filter { it.isNotBlank() }.joinToString(" · ")
                val showTemp = r.waterTempC != null && !r.method.isEspresso
                if (gear.isNotBlank() || showTemp || r.preInfusionSeconds != null) {
                    PillFlow(Modifier.enterCascade(2)) {
                        if (gear.isNotBlank()) Pill(gear, Bloom.Sand)
                        if (showTemp) Pill("${r.waterTempC} °C", Bloom.Sand)
                        r.preInfusionSeconds?.let { Pill("pré-infusão $it s", Bloom.Sand) }
                    }
                }
                if (r.method == BrewMethod.AEROPRESS) PositionCard(r.inverted, Modifier.enterCascade(3))
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
            when (r.method) {
                BrewMethod.ESPRESSO -> "Tirar o shot"
                BrewMethod.AEROPRESS -> "Bora prensar"
                else -> "Bora passar"
            },
            { onBrew(r.id) },
            Modifier.navigationBarsPadding().padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 16.dp),
            leading = { Icon(Icons.Filled.PlayArrow, null, tint = if (p.isDark) p.accent else p.bg, modifier = Modifier.size(26.dp)) },
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

/** Texto de apoio de cada etapa (bloom, meio, último, e as etapas da AeroPress). */
fun pourHint(method: BrewMethod, pours: List<Pour>, index: Int, targetSeconds: Int?): String {
    val pour = pours[index]
    val total = pours.cumulativeGramsAt(index)
    val isLast = index == pours.lastIndex
    val firstWater = pours.indexOfFirst { it.kind == StepKind.POUR }
    val nextStart = pours.getOrNull(index + 1)?.startSeconds
    return when (pour.kind) {
        StepKind.STIR -> "Mexa com firmeza para molhar todo o pó."
        StepKind.FLIP -> "Encaixe o filtro umedecido, ponha a caneca por cima e vire."
        StepKind.PRESS -> {
            val until = nextStart ?: targetSeconds?.takeIf { it > pour.startSeconds }
            "Prense devagar${until?.let { " até ${formatDuration(it)}" } ?: ""}. Pare no primeiro chiado."
        }
        StepKind.DILUTE -> "Água quente direto na caneca, até ${formatGrams(total)}."
        StepKind.POUR -> when {
            method == BrewMethod.FRENCH_PRESS && isLast ->
                "Até ${formatGrams(total)}. Espere${targetSeconds?.let { " até ${formatDuration(it)}" } ?: ""}, quebre a crosta e pressione devagar."
            method == BrewMethod.AEROPRESS -> "Até ${formatGrams(total)}, molhando todo o pó."
            index == firstWater && pours.count { it.kind == StepKind.POUR } > 1 -> "Bloom: molhe todo o pó e espere ele inchar."
            isLast -> "Até ${formatGrams(total)}. Depois é só esperar escorrer."
            else -> "Em círculos, até a balança marcar ${formatGrams(total)}."
        }
    }
}

/** Título de uma etapa ("Despeje 100 g", "Mexa", "Dilua com 100 g"). */
fun stepTitle(pour: Pour): String = when (pour.kind) {
    StepKind.POUR -> "Despeje ${formatGrams(pour.grams)}"
    StepKind.STIR -> "Mexa"
    StepKind.FLIP -> "Vire"
    StepKind.PRESS -> "Prense"
    StepKind.DILUTE -> "Dilua com ${formatGrams(pour.grams)}"
}

/** "18 g → 36 g" grande, com legenda embaixo. */
@Composable
fun DoseToYield(dose: Double?, yield: Double?, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(dose?.let(::formatGrams) ?: "—", style = BloomType.display(44.sp, -0.03f), color = Bloom.Mustard)
            Text("de café", style = BloomType.label.copy(fontSize = 12.sp), color = color.copy(alpha = 0.7f))
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = color, modifier = Modifier.size(28.dp))
        Column {
            Text(yield?.let(::formatGrams) ?: "—", style = BloomType.display(44.sp, -0.03f), color = Bloom.Terra)
            Text("na xícara", style = BloomType.label.copy(fontSize = 12.sp), color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun PositionCard(inverted: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Bloom.Sand).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MethodSilhouette(
            BrewMethod.AEROPRESS,
            Bloom.Ink,
            Modifier.size(48.dp).graphicsLayer { rotationZ = if (inverted) 180f else 0f }.sway(4f, 3000),
        )
        Column(Modifier.weight(1f)) {
            Text(if (inverted) "Montagem invertida" else "Montagem normal", style = BloomType.display(17.sp, -0.01f), color = Bloom.Ink)
            Text(
                if (inverted) "Êmbolo no nº 4, de cabeça para baixo. Nada pinga até virar." else "Filtro embaixo desde o começo. Encaixe o êmbolo de leve para segurar o pingo.",
                style = BloomType.bodySmall,
                color = Bloom.Muted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private data class HowTo(val icon: ImageVector?, val number: String?, val title: String, val time: String?, val hint: String)

private fun howToSteps(r: Recipe): List<HowTo> {
    if (r.method.isEspresso) {
        val pre = r.preInfusionSeconds?.let { " A pré-infusão de $it s entra na conta." } ?: ""
        return listOf(
            HowTo(Icons.Filled.Grain, null, "Moa ${r.doseGrams?.let(::formatGrams) ?: "a dose"}", "antes", "Distribua e nivele no porta-filtro."),
            HowTo(Icons.Filled.Compress, null, "Compacte", "antes", "Pressão firme e reta."),
            HowTo(Icons.Filled.PowerSettingsNew, null, "Acione a bomba", "0 s", "Toque em começar junto com o botão da máquina.$pre"),
            HowTo(
                Icons.Filled.Stop, null,
                "Pare em ${r.yieldGrams?.let(::formatGrams) ?: "o dobro da dose"}",
                windowText(r.windowMin, r.windowMax),
                "Quando a balança marcar o rendimento.",
            ),
        )
    }
    val pours = r.pours.sortedBy { it.startSeconds }
    var waterIndex = 0
    return pours.mapIndexed { i, pour ->
        val number = if (pour.kind == StepKind.POUR) "${++waterIndex}" else null
        HowTo(if (pour.kind == StepKind.POUR) null else pour.kind.icon, number, stepTitle(pour), "aos ${formatDuration(pour.startSeconds)}", pourHint(r.method, pours, i, r.targetTimeSeconds))
    }
}

@Composable
private fun HowToSteps(r: Recipe) {
    val steps = howToSteps(r)
    if (steps.isEmpty()) {
        Text("Esta receita ainda não tem despejos.", style = BloomType.body, color = Bloom.Body)
        return
    }
    val p = r.method.palette
    val firstBg = if (p.isDark) p.accent else p.bg
    steps.forEachIndexed { i, step ->
        Row(Modifier.fillMaxWidth().enterCascade(4 + i), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (i == 0) firstBg else Bloom.Ink),
                contentAlignment = Alignment.Center,
            ) {
                val c = if (i == 0) Bloom.Ink else Bloom.Cream
                if (step.icon != null) {
                    Icon(step.icon, null, tint = c, modifier = Modifier.size(22.dp))
                } else {
                    Text(step.number.orEmpty(), style = BloomType.display(20.sp, 0f), color = c)
                }
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(step.title, style = BloomType.display(20.sp, -0.02f), color = Bloom.Ink)
                    if (step.time != null) {
                        Text("  ${step.time}", style = BloomType.label.copy(fontSize = 13.sp), color = Bloom.Muted, modifier = Modifier.padding(bottom = 2.dp))
                    }
                }
                Text(step.hint, style = BloomType.bodySmall.copy(fontSize = 14.sp), color = Bloom.Body, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

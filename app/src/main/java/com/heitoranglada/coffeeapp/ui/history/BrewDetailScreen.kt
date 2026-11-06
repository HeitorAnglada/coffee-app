package com.heitoranglada.coffeeapp.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.StepKind
import com.heitoranglada.coffeeapp.domain.espressoStyle
import com.heitoranglada.coffeeapp.domain.formatShotSeconds
import com.heitoranglada.coffeeapp.ui.components.icon
import com.heitoranglada.coffeeapp.ui.recipes.DoseToYield
import com.heitoranglada.coffeeapp.ui.theme.isDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.waterGrams
import com.heitoranglada.coffeeapp.domain.cumulativeGramsAt
import com.heitoranglada.coffeeapp.domain.formatDateTime
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.domain.formatRatio
import com.heitoranglada.coffeeapp.ui.components.BeanRating
import com.heitoranglada.coffeeapp.ui.components.BigPillButton
import com.heitoranglada.coffeeapp.ui.components.Blob
import com.heitoranglada.coffeeapp.ui.components.BloomDialog
import com.heitoranglada.coffeeapp.ui.components.CircleIconButton
import com.heitoranglada.coffeeapp.ui.components.Drop
import com.heitoranglada.coffeeapp.ui.components.Eyebrow
import com.heitoranglada.coffeeapp.ui.components.InfoLine
import com.heitoranglada.coffeeapp.ui.components.MethodHero
import com.heitoranglada.coffeeapp.ui.components.Pill
import com.heitoranglada.coffeeapp.ui.components.PillFlow
import com.heitoranglada.coffeeapp.ui.components.SectionTitle
import com.heitoranglada.coffeeapp.ui.components.Sheet
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.components.pourColor
import com.heitoranglada.coffeeapp.ui.appViewModelFactory
import com.heitoranglada.coffeeapp.ui.journal.BrewDiary
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import com.heitoranglada.coffeeapp.ui.theme.palette

@Composable
fun BrewDetailScreen(
    brewId: Long,
    onBack: () -> Unit,
    onSaveAsRecipe: (Long) -> Unit,
    onBrewAgain: (Long) -> Unit,
    onOpenBean: (Long) -> Unit = {},
    vm: BrewDetailViewModel = viewModel(
        key = "brew-detail-$brewId",
        factory = appViewModelFactory { BrewDetailViewModel(it.repository, brewId, it.journal) },
    ),
) {
    val brew by vm.brew.collectAsStateWithLifecycle()
    val note by vm.note.collectAsStateWithLifecycle()
    val bean by vm.bean.collectAsStateWithLifecycle()
    val recipe by vm.recipe.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val b = brew

    if (b == null) {
        Box(Modifier.fillMaxSize().background(Bloom.Cream), contentAlignment = Alignment.Center) {
            Blob(Bloom.Terra, Modifier.size(110.dp), periodMs = 2000, wobble = 0.12f)
        }
        return
    }
    val p = b.method.palette
    val scroll = rememberScrollState()

    Column(Modifier.fillMaxSize().background(Bloom.Cream).verticalScroll(scroll)) {
        MethodHero(
            b.method,
            scroll = scroll,
            topBar = {
                CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBack, background = Bloom.Cream.copy(alpha = 0.3f), contentColor = p.fg)
                Spacer(Modifier.weight(1f))
                CircleIconButton(Icons.Filled.Delete, "Excluir preparo", { confirmDelete = true }, background = Bloom.Cream.copy(alpha = 0.3f), contentColor = p.fg)
            },
        ) {
            Eyebrow("${b.method.label} · ${formatDateTime(b.brewedAt)}", Modifier.enterCascade(0), p.fg.copy(alpha = 0.75f))
            Text(b.recipeName ?: "Preparo livre", style = BloomType.display(40.sp, -0.035f), color = p.fg, modifier = Modifier.padding(top = 6.dp).enterCascade(1))
            val time = if (b.method.isEspresso) "${formatShotSeconds(b.shotSeconds ?: b.totalTimeSeconds.toDouble())} s" else formatDuration(b.totalTimeSeconds)
            Text(time, style = BloomType.display(64.sp, -0.04f), color = p.fg, modifier = Modifier.enterCascade(2))
            if (b.method.isEspresso) {
                DoseToYield(b.doseGrams, b.yieldGrams, p.fg, Modifier.padding(top = 8.dp).enterCascade(3))
                PillFlow(Modifier.padding(top = 10.dp).enterCascade(3)) {
                    formatRatio(b.doseGrams, b.yieldGrams)?.let { r ->
                        Pill(listOfNotNull(r, espressoStyle(b.doseGrams, b.yieldGrams)).joinToString(" · "), p.chip, contentColor = p.fg)
                    }
                }
            } else {
                PillFlow(Modifier.padding(top = 10.dp).enterCascade(3)) {
                    b.doseGrams?.let { Pill("${formatGrams(it)} de café", p.chip, contentColor = p.fg) }
                    if (b.waterGrams > 0) Pill("${formatGrams(b.waterGrams)} de água", p.chip, contentColor = p.fg)
                    formatRatio(b.doseGrams, b.waterGrams)?.let { Pill(it, p.chip, contentColor = p.fg) }
                }
            }
        }
        Sheet {
            if (b.rating != null) {
                SectionTitle("Como ficou", Modifier.enterCascade(3))
                BeanRating(b.rating, null, Modifier.enterCascade(4))
            }
            val pours = b.pours.sortedBy { it.startSeconds }
            if (pours.isNotEmpty()) {
                SectionTitle(if (b.method == BrewMethod.AEROPRESS) "Etapas" else "Despejos", Modifier.enterCascade(5))
            }
            pours.forEachIndexed { i, pour ->
                Row(Modifier.fillMaxWidth().enterCascade(6 + i), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (pour.kind == StepKind.POUR) {
                        val n = pours.take(i + 1).count { it.kind == StepKind.POUR }
                        Drop("$n", fill = Bloom.Ink, textColor = pourColor(i), width = 30.dp, textStyle = BloomType.display(13.sp, 0f))
                    } else {
                        Box(Modifier.size(30.dp).clip(CircleShape).background(Bloom.Ink), contentAlignment = Alignment.Center) {
                            Icon(pour.kind.icon, null, tint = pourColor(i), modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(formatDuration(pour.startSeconds), style = BloomType.chip.copy(fontSize = 14.sp), color = Bloom.Muted, modifier = Modifier.padding(end = 4.dp))
                    if (pour.kind.addsWater) {
                        Text(
                            if (pour.kind == StepKind.DILUTE) "diluiu ${formatGrams(pour.grams)}" else "+${formatGrams(pour.grams)}",
                            style = BloomType.display(18.sp, -0.01f),
                            color = Bloom.Ink,
                            modifier = Modifier.weight(1f),
                        )
                        Text("→ ${formatGrams(pours.cumulativeGramsAt(i))}", style = BloomType.chip.copy(fontSize = 14.sp), color = Bloom.Body)
                    } else {
                        Text(pour.kind.label, style = BloomType.display(18.sp, -0.01f), color = Bloom.Ink, modifier = Modifier.weight(1f))
                    }
                }
            }
            SectionTitle("Café e moagem")
            Column {
                InfoLine("Café", b.coffee.ifBlank { "—" })
                InfoLine("Moedor", b.grinder.ifBlank { "—" })
                InfoLine("Moagem", b.grindSetting.ifBlank { "—" })
                InfoLine("Temperatura", b.waterTempC?.let { "$it °C" })
                if (b.method.isEspresso) InfoLine("Na xícara", b.yieldGrams?.let(::formatGrams))
            }
            if (b.notes.isNotBlank()) {
                SectionTitle("Notas")
                Text(
                    b.notes,
                    style = BloomType.body,
                    color = Bloom.Body,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Bloom.Sand).padding(16.dp),
                )
            }
            BrewDiary(note, bean, onOpenBean)
            val r = recipe
            if (r != null) {
                BigPillButton(
                    "Passar de novo",
                    { onBrewAgain(r.id) },
                    leading = { Icon(Icons.Filled.Replay, null, tint = if (p.isDark) p.accent else p.bg, modifier = Modifier.size(24.dp)) },
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bouncyClick { onSaveAsRecipe(b.id) }
                    .clip(RoundedCornerShape(50))
                    .border(2.dp, Bloom.Ink, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (r != null) "Salvar ajustes como nova receita" else "Virar receita", style = BloomType.button.copy(fontSize = 17.sp), color = Bloom.Ink)
            }
            Spacer(Modifier.navigationBarsPadding().height(16.dp))
        }
    }

    if (confirmDelete) {
        BloomDialog(
            title = "Excluir preparo?",
            text = "Ele será removido do histórico.",
            confirm = "Excluir",
            onConfirm = { confirmDelete = false; vm.delete(onBack) },
            dismiss = "Cancelar",
            onDismiss = { confirmDelete = false },
        )
    }
}

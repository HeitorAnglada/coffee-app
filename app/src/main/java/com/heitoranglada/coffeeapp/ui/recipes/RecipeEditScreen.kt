package com.heitoranglada.coffeeapp.ui.recipes

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.domain.formatRatio
import com.heitoranglada.coffeeapp.ui.components.BigPillButton
import com.heitoranglada.coffeeapp.ui.components.Blob
import com.heitoranglada.coffeeapp.ui.components.CircleIconButton
import com.heitoranglada.coffeeapp.ui.components.CoffeeForm
import com.heitoranglada.coffeeapp.ui.components.ErrorSticker
import com.heitoranglada.coffeeapp.ui.components.Eyebrow
import com.heitoranglada.coffeeapp.ui.components.ExpandableCard
import com.heitoranglada.coffeeapp.ui.components.MethodTiles
import com.heitoranglada.coffeeapp.ui.components.PillField
import com.heitoranglada.coffeeapp.ui.components.PourEditor
import com.heitoranglada.coffeeapp.ui.components.Stepper
import com.heitoranglada.coffeeapp.ui.components.SuggestionRow
import com.heitoranglada.coffeeapp.ui.components.TextAction
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.components.wiggle
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType

@Composable
fun RecipeEditScreen(
    recipeId: Long,
    fromBrewId: Long,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    vm: RecipeEditViewModel = viewModel(
        key = "edit-$recipeId-$fromBrewId",
        factory = repoViewModelFactory { RecipeEditViewModel(it, recipeId, fromBrewId) },
    ),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val coffees by vm.coffees.collectAsStateWithLifecycle()
    val grinders by vm.grinders.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedId) { state.savedId?.let(onSaved) }

    if (state.loading) {
        Box(Modifier.fillMaxSize().background(Bloom.Cream), contentAlignment = Alignment.Center) {
            Blob(Bloom.Mustard, Modifier.size(110.dp), periodMs = 2000, wobble = 0.12f)
        }
        return
    }
    val form = state.form
    val onChange = vm::onFormChange

    Column(
        Modifier
            .fillMaxSize()
            .background(Bloom.Cream)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBack)
            Spacer(Modifier.weight(1f))
            TextAction("Cancelar", onBack)
        }
        Text(
            if (state.isNew) "Nova\nreceita" else "Editar\nreceita",
            style = BloomType.display(40.sp, -0.035f),
            color = Bloom.Ink,
            modifier = Modifier.enterCascade(0),
        )

        Section("1 · Qual método?", 1) {
            MethodTiles(form.method) { onChange(form.copy(method = it)) }
            AnimatedContent(form.method, label = "tip") { m ->
                Text(m.tip, style = BloomType.bodySmall, color = Bloom.Body)
            }
        }

        Section("2 · Sobre o café", 2) {
            PillField(form.name, { onChange(form.copy(name = it)) }, label = "Nome da receita", placeholder = "Ex.: V60 do sábado")
            PillField(form.coffee, { onChange(form.copy(coffee = it)) }, label = "Café", placeholder = "Ex.: Fazenda X, catuaí amarelo")
            SuggestionRow(form.coffee, coffees) { onChange(form.copy(coffee = it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillField(form.grinder, { onChange(form.copy(grinder = it)) }, Modifier.weight(1.4f), label = "Moedor", placeholder = "Comandante C40")
                PillField(form.grindSetting, { onChange(form.copy(grindSetting = it)) }, Modifier.weight(1f), label = "Moagem", placeholder = "24 cliques")
            }
            SuggestionRow(form.grinder, grinders) { onChange(form.copy(grinder = it)) }
        }

        Section("3 · Quanto?", 3) {
            QuantityRow(form, onChange)
        }

        Section("4 · Despejos", 4) {
            Text(
                "Quando cada despejo começa (m:ss) e quantos gramas de água ele tem. O primeiro costuma ser o bloom.",
                style = BloomType.bodySmall,
                color = Bloom.Body,
            )
            PourEditor(form.pours, { onChange(form.copy(pours = it)) }, highlightEmpty = false)
        }

        ExpandableCard("Mais opções", "temperatura, tempo-alvo e notas", Modifier.enterCascade(5)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillField(form.waterTempText, { onChange(form.copy(waterTempText = it)) }, Modifier.weight(1f), label = "Temperatura", placeholder = "93", suffix = "°C", keyboardType = KeyboardType.Number)
                PillField(form.timeText, { onChange(form.copy(timeText = it)) }, Modifier.weight(1f), label = "Tempo-alvo", placeholder = "3:00")
            }
            PillField(form.notes, { onChange(form.copy(notes = it)) }, label = "Notas", placeholder = "Sabor, acidez, doçura, o que mudar…", singleLine = false)
        }

        ErrorSticker(state.errors)
        BigPillButton("Salvar receita", vm::save)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Section(title: String, index: Int, content: @Composable () -> Unit) {
    Column(Modifier.enterCascade(index), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Eyebrow(title)
        content()
    }
}

/** Dose com − e +, água (soma dos despejos) e a proporção num adesivo. */
@Composable
fun QuantityRow(form: CoffeeForm, onChange: (CoffeeForm) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stepper(
                label = "Café",
                value = form.doseText,
                onValueChange = { onChange(form.copy(doseText = it)) },
                onMinus = { onChange(form.copy(doseText = formatNumber(((form.dose ?: 15.0) - 1).coerceAtLeast(1.0)))) },
                onPlus = { onChange(form.copy(doseText = formatNumber((form.dose ?: 14.0) + 1))) },
                unit = "g",
                modifier = Modifier.weight(1.25f),
            )
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Bloom.White)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Eyebrow("Água")
                AnimatedContent(formatNumber(form.waterGrams), label = "water") { w ->
                    Text(
                        buildAnnotatedString {
                            append(w)
                            withStyle(SpanStyle(fontSize = 14.sp, letterSpacing = 0.sp)) { append(" g") }
                        },
                        style = BloomType.display(30.sp),
                        color = Bloom.Ink,
                    )
                }
                Text("soma dos despejos", style = BloomType.bodySmall.copy(fontSize = 11.sp), color = Bloom.Muted)
            }
        }
        val ratio = formatRatio(form.dose, form.waterGrams)
        if (ratio != null) {
            val water = form.waterGrams
            val dose = form.dose ?: 1.0
            val r = water / dose
            val feel = when {
                r < 14.5 -> "mais forte"
                r <= 17.5 -> "equilibrado"
                else -> "mais suave"
            }
            Row(
                Modifier
                    .wiggle(-2f, 3000)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Bloom.Ink)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Proporção ", style = BloomType.chip.copy(fontSize = 14.sp), color = Bloom.Cream)
                AnimatedContent(ratio, label = "ratio") { Text(it, style = BloomType.display(20.sp, 0f), color = Bloom.Mustard) }
                Text(" · $feel", style = BloomType.chip.copy(fontSize = 14.sp), color = Bloom.Cream)
            }
        }
    }
}

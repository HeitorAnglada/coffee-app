package com.heitoranglada.coffeeapp.ui.calculator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.domain.CalcAnchor
import com.heitoranglada.coffeeapp.domain.CalcState
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.domain.parseDecimal
import com.heitoranglada.coffeeapp.ui.components.BigPillButton
import com.heitoranglada.coffeeapp.ui.components.CircleIconButton
import com.heitoranglada.coffeeapp.ui.components.Eyebrow
import com.heitoranglada.coffeeapp.ui.components.RollingText
import com.heitoranglada.coffeeapp.ui.components.bob
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import kotlinx.coroutines.launch

/**
 * Calculadora rápida: escolhe a proporção e ajusta o café ou a água; o outro lado se recalcula.
 * Sobe por cima da tela atual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorSheet(
    state: CalcState,
    onChange: ((CalcState) -> CalcState) -> Unit,
    onDismiss: () -> Unit,
    onBrew: () -> Unit,
) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun close(then: () -> Unit = {}) {
        scope.launch { sheet.hide() }.invokeOnCompletion {
            onDismiss()
            then()
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheet,
        containerColor = Bloom.Cream,
        contentColor = Bloom.Ink,
        scrimColor = Bloom.Ink.copy(alpha = 0.55f),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = {
            Box(Modifier.padding(top = 12.dp, bottom = 4.dp).size(44.dp, 5.dp).clip(RoundedCornerShape(50)).background(Bloom.Line))
        },
    ) {
        CalculatorContent(state, onChange, onClose = { close() }, onBrew = { close(onBrew) })
    }
}

@Composable
fun CalculatorContent(
    state: CalcState,
    onChange: ((CalcState) -> CalcState) -> Unit,
    onClose: () -> Unit,
    onBrew: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.enterCascade(0)) {
            Column(Modifier.weight(1f)) {
                Eyebrow("Calculadora", color = Bloom.Ink.copy(alpha = 0.75f))
                Text(
                    buildAnnotatedString {
                        append("Quanto de ")
                        withStyle(SpanStyle(color = Bloom.Terra)) { append("cada?") }
                    },
                    style = BloomType.display(32.sp, -0.03f),
                    color = Bloom.Ink,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            CircleIconButton(Icons.Filled.Close, "Fechar calculadora", onClose, size = 44.dp)
        }

        // Proporção
        Row(
            Modifier
                .fillMaxWidth()
                .enterCascade(1)
                .clip(RoundedCornerShape(26.dp))
                .background(Bloom.Ink)
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Eyebrow("Proporção", color = Bloom.Cream.copy(alpha = 0.7f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("1:", style = BloomType.display(34.sp, -0.03f), color = Bloom.Cream)
                    RollingText(formatNumber(state.ratio), BloomType.display(34.sp, -0.03f), Bloom.Mustard)
                }
            }
            CircleIconButton(Icons.Filled.Remove, "Proporção mais forte", { onChange { it.stepRatio(up = false) } }, background = Bloom.Cream.copy(alpha = 0.14f), contentColor = Bloom.Cream, size = 48.dp)
            Spacer(Modifier.width(6.dp))
            CircleIconButton(Icons.Filled.Add, "Proporção mais fraca", { onChange { it.stepRatio(up = true) } }, background = Bloom.Cream.copy(alpha = 0.14f), contentColor = Bloom.Cream, size = 48.dp)
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).enterCascade(2),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CalcState.presets.forEach { (label, value) ->
                val on = kotlin.math.abs(state.ratio - value) < 0.01
                val bg by animateColorAsState(if (on) Bloom.Ink else Bloom.Cream, label = "preset-bg")
                val fg by animateColorAsState(if (on) Bloom.Cream else Bloom.Ink, label = "preset-fg")
                Box(
                    Modifier
                        .height(40.dp)
                        .bouncyClick(onClickLabel = "Proporção $label") { onChange { it.withRatio(value) } }
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .border(2.dp, Bloom.Ink, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp)
                        .semantics { role = Role.RadioButton },
                    contentAlignment = Alignment.Center,
                ) { Text(label, style = BloomType.chip.copy(fontSize = 14.sp), color = fg, maxLines = 1) }
            }
        }

        Column(Modifier.enterCascade(3)) {
            AmountCard(
                label = "Café",
                value = state.coffeeGrams,
                active = state.anchor == CalcAnchor.COFFEE,
                background = Bloom.Mustard,
                note = if (state.anchor == CalcAnchor.COFFEE) "você definiu · toque no número para digitar" else if (state.isEspresso) "calculado pela bebida" else "calculado pela água",
                onMinus = { onChange { it.stepCoffee(up = false) } },
                onPlus = { onChange { it.stepCoffee(up = true) } },
                onTyped = { g -> onChange { it.withCoffee(g) } },
            )
            Box(Modifier.fillMaxWidth().height(0.dp).zIndex(1f), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .offset(y = 5.dp)
                        .requiredSize(44.dp)
                        .bob(4.dp, 2400)
                        .clip(CircleShape)
                        .background(Bloom.Cream)
                        .border(3.dp, Bloom.Ink, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.SwapVert, null, tint = Bloom.Ink, modifier = Modifier.size(22.dp)) }
            }
            Spacer(Modifier.height(10.dp))
            AmountCard(
                label = if (state.isEspresso) "Na xícara" else "Água",
                value = state.waterGrams,
                active = state.anchor == CalcAnchor.WATER,
                background = Bloom.Terra,
                note = if (state.anchor == CalcAnchor.WATER) "você definiu · toque no número para digitar" else "calculado pelo café",
                onMinus = { onChange { it.stepWater(up = false) } },
                onPlus = { onChange { it.stepWater(up = true) } },
                onTyped = { g -> onChange { it.withWater(g) } },
            )
        }

        Text(
            buildAnnotatedString {
                if (state.isEspresso) {
                    append("Shot de ")
                    withStyle(SpanStyle(color = Bloom.Ink, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)) {
                        append("${formatNumber(state.waterGrams)} g")
                    }
                    append(" de bebida para ${formatNumber(state.coffeeGrams)} g de café.")
                } else {
                    append("Rende cerca de ")
                    withStyle(SpanStyle(color = Bloom.Ink, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)) {
                        append("${formatNumber(kotlin.math.round(state.cupGrams))} ml")
                    }
                    append(" na xícara. O pó segura uns 2 g de água por grama.")
                }
            },
            style = BloomType.body.copy(fontSize = 14.sp),
            color = Bloom.Body,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().enterCascade(4),
        )
        BigPillButton(
            "Preparar livre com isso",
            onBrew,
            modifier = Modifier.enterCascade(5),
            leading = { Icon(Icons.Filled.PlayArrow, null, tint = Bloom.Mustard, modifier = Modifier.size(24.dp)) },
        )
        Spacer(Modifier.height(12.dp))
    }
}

/** Cartão grande com o número (digitável) e os botões − e +. */
@Composable
private fun AmountCard(
    label: String,
    value: Double,
    active: Boolean,
    background: Color,
    note: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onTyped: (Double) -> Unit,
) {
    val border by animateDpAsState(if (active) 3.dp else 0.dp, spring(dampingRatio = 0.6f), label = "anchor")
    var editing by remember { mutableStateOf(false) }
    var hadFocus by remember { mutableStateOf(false) }
    val requester = remember { FocusRequester() }
    var text by remember { mutableStateOf(formatNumber(value)) }
    LaunchedEffect(value, editing) { if (!editing) text = formatNumber(value) }
    val focus = LocalFocusManager.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .border(border.coerceAtLeast(0.dp), Bloom.Ink, RoundedCornerShape(28.dp))
            .padding(start = 20.dp, end = 14.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Eyebrow(label, color = Bloom.Ink.copy(alpha = 0.8f))
            Row(verticalAlignment = Alignment.Bottom) {
                if (editing) {
                    BasicTextField(
                        value = text,
                        onValueChange = { t ->
                            text = t
                            parseDecimal(t)?.let(onTyped)
                        },
                        singleLine = true,
                        textStyle = BloomType.display(54.sp, -0.04f).copy(color = Bloom.Ink),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                        cursorBrush = SolidColor(Bloom.Ink),
                        modifier = Modifier
                            .widthIn(min = 60.dp, max = 170.dp)
                            .focusRequester(requester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    hadFocus = true
                                } else if (hadFocus) {
                                    hadFocus = false
                                    editing = false
                                }
                            },
                    )
                    LaunchedEffect(Unit) { requester.requestFocus() }
                } else {
                    Box(
                        Modifier.bouncyClick(onClickLabel = "Digitar $label") {
                            text = ""
                            editing = true
                        },
                    ) { RollingText(formatNumber(value), BloomType.display(54.sp, -0.04f), Bloom.Ink) }
                }
                Text(" g", style = BloomType.display(24.sp, 0f), color = Bloom.Ink, modifier = Modifier.padding(bottom = 6.dp))
            }
            Text(note, style = BloomType.bodySmall.copy(fontSize = 12.sp), color = Bloom.Ink.copy(alpha = 0.8f), modifier = Modifier.padding(top = 2.dp))
        }
        CircleIconButton(Icons.Filled.Remove, "Menos ${label.lowercase()}", onMinus, background = Bloom.Cream.copy(alpha = 0.4f), size = 48.dp)
        Spacer(Modifier.width(8.dp))
        CircleIconButton(Icons.Filled.Add, "Mais ${label.lowercase()}", onPlus, background = Bloom.Cream.copy(alpha = 0.4f), size = 48.dp)
    }
}

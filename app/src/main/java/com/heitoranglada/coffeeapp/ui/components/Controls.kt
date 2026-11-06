package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColor
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import com.heitoranglada.coffeeapp.ui.theme.palette

/** "bloombrew." com o ponto terracota. */
@Composable
fun Wordmark(modifier: Modifier = Modifier, color: Color = Bloom.Ink) {
    Text(
        buildAnnotatedString {
            append("bloombrew")
            withStyle(SpanStyle(color = Bloom.Terra)) { append(".") }
        },
        style = BloomType.display(16.sp, 0f),
        color = color,
        modifier = modifier,
    )
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Bloom.Muted) {
    Text(text.uppercase(), style = BloomType.eyebrow, color = color, modifier = modifier)
}

/** Título de seção em Bricolage. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = BloomType.display(24.sp, -0.02f), color = Bloom.Ink, modifier = modifier)
}

@Composable
fun Pill(
    text: String,
    background: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Bloom.Ink,
    dot: Color? = null,
    height: Dp = 32.dp,
) {
    Row(
        modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dot != null) Box(Modifier.size(10.dp).clip(CircleShape).background(dot))
        Text(text, style = BloomType.chip, color = contentColor, maxLines = 1)
    }
}

/** Botão principal: pílula grande e cheia. */
@Composable
fun BigPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Bloom.Ink,
    contentColor: Color = Bloom.Cream,
    height: Dp = 64.dp,
    enabled: Boolean = true,
    leading: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .bouncyClick(enabled = enabled, pressedScale = 0.96f, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) background else background.copy(alpha = 0.4f))
            .semantics { role = Role.Button },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = BloomType.button, color = contentColor)
    }
}

/** Botão redondo com ícone (voltar, desfazer, apagar). */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Bloom.Sand,
    contentColor: Color = Bloom.Ink,
    size: Dp = 42.dp,
    border: Color? = null,
) {
    Box(
        modifier
            .size(size)
            .bouncyClick(onClickLabel = contentDescription, onClick = onClick)
            .clip(CircleShape)
            .background(background)
            .then(if (border != null) Modifier.border(2.dp, border, CircleShape) else Modifier)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(size * 0.5f))
    }
}

/** Texto clicável simples (ex.: "Cancelar", "Editar"). */
@Composable
fun TextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Bloom.Ink) {
    Text(
        text,
        style = BloomType.body.copy(fontSize = 15.sp),
        color = color,
        modifier = modifier
            .bouncyClick(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
    )
}

/**
 * Campo em pílula. Com [highlight], a borda "respira" em terracota para mostrar o que falta preencher.
 */
@Composable
fun PillField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    highlight: Boolean = false,
    singleLine: Boolean = true,
    textAlign: TextAlign = TextAlign.Start,
) {
    val glow = rememberInfiniteTransition(label = "glow")
    val glowColor by glow.animateColor(
        initialValue = Bloom.Terra,
        targetValue = Bloom.Peach,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "glow-color",
    )
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        when {
            focused -> Bloom.Ink
            highlight -> glowColor
            else -> Bloom.Line
        },
        label = "border",
    )
    val borderWidth by animateDpAsState(if (focused || highlight) 2.5.dp else 2.dp, label = "border-w")
    val shape = if (singleLine) RoundedCornerShape(26.dp) else RoundedCornerShape(22.dp)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label != null) Text(label, style = BloomType.label, color = Bloom.Muted)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            textStyle = BloomType.body.copy(fontSize = 16.sp, color = Bloom.Ink, textAlign = textAlign),
            cursorBrush = SolidColor(Bloom.Terra),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .clip(shape)
                        .background(Bloom.White)
                        .border(borderWidth, borderColor, shape)
                        .padding(horizontal = 18.dp, vertical = if (singleLine) 0.dp else 14.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                style = BloomType.body.copy(fontSize = 16.sp, textAlign = textAlign),
                                color = Bloom.Bean,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        inner()
                    }
                    if (suffix != null) Text(suffix, style = BloomType.label, color = Bloom.Muted)
                }
            },
        )
    }
}

/** Sugestões de valores já usados (cafés, moedores). */
@Composable
fun SuggestionRow(value: String, suggestions: List<String>, onPick: (String) -> Unit) {
    val filtered = suggestions.filter { it != value && it.contains(value.trim(), ignoreCase = true) }.take(8)
    AnimatedVisibility(filtered.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filtered) { s ->
                Pill(s, Bloom.Sand, Modifier.bouncyClick { onPick(s) })
            }
        }
    }
}

/** Filtro/seleção de método em pílulas com bolinha colorida. */
@Composable
fun MethodChips(
    selected: BrewMethod?,
    onSelect: (BrewMethod?) -> Unit,
    modifier: Modifier = Modifier,
    showAll: Boolean = false,
) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showAll) ChoiceChip("Todas", null, selected == null) { onSelect(null) }
        BrewMethod.entries.forEach { m ->
            ChoiceChip(if (m == BrewMethod.FRENCH_PRESS) "Prensa" else m.label, m.palette.bg, selected == m) { onSelect(m) }
        }
    }
}

@Composable
private fun ChoiceChip(text: String, dot: Color?, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Bloom.Ink else Bloom.Cream, label = "chip-bg")
    val fg by animateColorAsState(if (selected) Bloom.Cream else Bloom.Ink, label = "chip-fg")
    Row(
        Modifier
            .height(40.dp)
            .bouncyClick(onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(2.dp, Bloom.Ink, RoundedCornerShape(50))
            .padding(horizontal = 12.dp)
            .semantics { role = Role.RadioButton },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dot != null) Box(Modifier.size(10.dp).clip(CircleShape).background(dot))
        Text(text, style = BloomType.chip.copy(fontSize = 14.sp), color = fg, maxLines = 1, softWrap = false)
    }
}

/** Escolha do método em blocos grandes com a silhueta. */
@Composable
fun MethodTiles(selected: BrewMethod, onSelect: (BrewMethod) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BrewMethod.entries.forEach { m ->
            val p = m.palette
            val on = m == selected
            val ring by animateDpAsState(if (on) 3.dp else 0.dp, spring(dampingRatio = 0.5f), label = "ring")
            val lift by animateFloatAsState(if (on) 1f else 0.94f, spring(dampingRatio = 0.5f), label = "lift")
            Box(
                Modifier
                    .weight(1f)
                    .height(120.dp)
                    .graphicsLayer {
                        scaleX = lift
                        scaleY = lift
                    }
                    .bouncyClick { onSelect(m) }
                    .border(ring, Bloom.Ink, RoundedCornerShape(26.dp))
                    .padding(ring + (if (on) 3.dp else 0.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(p.bg)
                    .semantics { role = Role.RadioButton },
            ) {
                MethodSilhouette(
                    m, p.fg.copy(alpha = 0.3f),
                    Modifier.size(80.dp).align(Alignment.TopEnd).offset(x = 14.dp, y = (-6).dp).sway(5f, 4000 + m.ordinal * 700),
                )
                androidx.compose.animation.AnimatedVisibility(
                    on,
                    enter = scaleIn(spring(dampingRatio = 0.4f)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                ) {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(Bloom.Ink), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Check, null, tint = Bloom.Cream, modifier = Modifier.size(15.dp))
                    }
                }
                Text(
                    if (m == BrewMethod.FRENCH_PRESS) "Prensa" else m.label,
                    style = BloomType.display(17.sp, -0.01f),
                    color = p.fg,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                )
            }
        }
    }
}

/** Número grande com botões de − e +. O número também pode ser digitado. */
@Composable
fun Stepper(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Bloom.White)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Eyebrow(label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(Icons.Filled.Remove, "Menos", onMinus, size = 36.dp)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = BloomType.display(30.sp).copy(color = Bloom.Ink, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(Bloom.Terra),
                    modifier = Modifier.width(64.dp),
                )
                Text(" $unit", style = BloomType.label.copy(fontSize = 14.sp), color = Bloom.Ink, modifier = Modifier.padding(bottom = 5.dp))
            }
            CircleIconButton(Icons.Filled.Add, "Mais", onPlus, background = Bloom.Ink, contentColor = Bloom.Cream, size = 36.dp)
        }
    }
}

/** Cartão que abre e fecha com animação (opções avançadas). */
@Composable
fun ExpandableCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    background: Color = Bloom.Sand,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val arrow by animateFloatAsState(if (expanded) 180f else 0f, spring(dampingRatio = 0.6f), label = "arrow")
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .bouncyClick(pressedScale = 0.98f) { expanded = !expanded }
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = BloomType.display(18.sp, -0.01f), color = Bloom.Ink)
                Text(subtitle, style = BloomType.bodySmall, color = Bloom.Muted)
            }
            Icon(Icons.Filled.KeyboardArrowDown, null, tint = Bloom.Ink, modifier = Modifier.rotate(arrow))
        }
        if (expanded) {
            Column(
                Modifier.padding(start = 14.dp, end = 14.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) { content() }
        }
    }
}

/** Interruptor em pílula com bolinha que desliza com mola. */
@Composable
fun BloomSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, track: Color = Bloom.Ink, thumb: Color = Bloom.Mustard) {
    val x by animateDpAsState(if (checked) 26.dp else 4.dp, spring(dampingRatio = 0.55f, stiffness = 600f), label = "thumb")
    val trackColor by animateColorAsState(if (checked) track else track.copy(alpha = 0.25f), label = "track")
    Box(
        Modifier
            .size(width = 56.dp, height = 34.dp)
            .bouncyClick { onCheckedChange(!checked) }
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .semantics { role = Role.Switch },
    ) {
        Box(
            Modifier
                .offset(x = x, y = 4.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(thumb),
        )
    }
}

/** Nota de 1 a 5 em grãos de café. Cada grão escolhido "pula". */
@Composable
fun BeanRating(rating: Int?, onChange: ((Int?) -> Unit)?, modifier: Modifier = Modifier, size: Dp = 52.dp) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        (1..5).forEach { n ->
            val on = rating != null && n <= rating
            val bg by animateColorAsState(if (on) Bloom.Terra else Bloom.Sand, tween(220, delayMillis = n * 40), label = "bean-bg")
            val fill by animateColorAsState(if (on) Bloom.Ink else Bloom.Bean, tween(220, delayMillis = n * 40), label = "bean")
            val bump = remember { androidx.compose.animation.core.Animatable(1f) }
            LaunchedEffect(on) {
                if (on) {
                    bump.snapTo(0.7f)
                    bump.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 500f))
                }
            }
            Box(
                Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = bump.value
                        scaleY = bump.value
                    }
                    .then(if (onChange != null) Modifier.bouncyClick { onChange(if (rating == n) null else n) } else Modifier)
                    .clip(CircleShape)
                    .background(bg)
                    .semantics { contentDescription = "Nota $n de 5" },
                contentAlignment = Alignment.Center,
            ) {
                BeanIcon(fill, bg, Modifier.size(size * 0.5f))
            }
        }
    }
}

/** Versão pequena da nota (lista do histórico). */
@Composable
fun MiniBeans(rating: Int?, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        (1..5).forEach { n ->
            BeanIcon(if (rating != null && n <= rating) Bloom.Ink else Bloom.Line, Bloom.White, Modifier.size(14.dp))
        }
    }
}

/** Lista de problemas que impedem salvar, num adesivo terracota. */
@Composable
fun ErrorSticker(errors: List<String>) {
    AnimatedVisibility(
        errors.isNotEmpty(),
        enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { rotationZ = -1.5f }
                .clip(RoundedCornerShape(22.dp))
                .background(Bloom.Terra)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Falta pouco:", style = BloomType.display(17.sp, 0f), color = Bloom.Ink)
            errors.forEach { Text("• $it", style = BloomType.bodySmall, color = Bloom.Ink) }
        }
    }
}

/** Cores das gotas de cada despejo. */
fun pourColor(index: Int): Color = listOf(Bloom.Terra, Bloom.Mustard, Bloom.Peach, Bloom.Green)[index % 4]

/**
 * Editor dos despejos: gota numerada + início + gramas. Linhas entram e saem com animação.
 * Com [highlightEmpty], os campos de gramas vazios brilham (preparo livre).
 */
@Composable
fun PourEditor(
    pours: List<PourInput>,
    onChange: (List<PourInput>) -> Unit,
    highlightEmpty: Boolean,
    allowAdd: Boolean = true,
) {
    Column(Modifier.animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f)), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        pours.forEachIndexed { index, pour ->
            androidx.compose.runtime.key(index) {
                Row(Modifier.popIn(index * 50L), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Drop(
                        "${index + 1}",
                        fill = if (highlightEmpty && pour.gramsText.isBlank()) Bloom.Terra else Bloom.Ink,
                        textColor = if (highlightEmpty && pour.gramsText.isBlank()) Bloom.Ink else pourColor(index),
                        width = 30.dp,
                        textStyle = BloomType.display(13.sp, 0f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    PillField(
                        pour.startText,
                        { v -> onChange(pours.toMutableList().also { it[index] = pour.copy(startText = v) }) },
                        Modifier.weight(0.8f),
                        placeholder = "0:45",
                    )
                    PillField(
                        pour.gramsText,
                        { v -> onChange(pours.toMutableList().also { it[index] = pour.copy(gramsText = v) }) },
                        Modifier.weight(1f),
                        placeholder = "quantos g?",
                        suffix = "g",
                        keyboardType = KeyboardType.Decimal,
                        highlight = highlightEmpty && pour.gramsText.isBlank(),
                    )
                    if (allowAdd) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .bouncyClick(enabled = pours.size > 1, onClickLabel = "Remover despejo") {
                                    onChange(pours.toMutableList().also { it.removeAt(index) })
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Close, "Remover despejo", tint = if (pours.size > 1) Bloom.Muted else Bloom.Line, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
        if (allowAdd) {
            Row(
                Modifier
                    .height(44.dp)
                    .bouncyClick {
                        val last = pours.lastOrNull()?.toPour()
                        val nextStart = (last?.startSeconds ?: -30) + 30
                        onChange(pours + PourInput(com.heitoranglada.coffeeapp.domain.formatDuration(nextStart), ""))
                    }
                    .border(2.dp, Bloom.Ink, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Add, null, tint = Bloom.Ink, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("despejo", style = BloomType.chip.copy(fontSize = 14.sp), color = Bloom.Ink)
            }
        }
    }
}

/** Troca de texto com deslize lateral (instruções que mudam durante o preparo). */
@Composable
fun <T> SlideSwap(target: T, modifier: Modifier = Modifier, content: @Composable (T) -> Unit) {
    AnimatedContent(
        targetState = target,
        transitionSpec = {
            (slideInHorizontally(spring(dampingRatio = 0.75f, stiffness = 300f)) { it } + fadeIn())
                .togetherWith(slideOutHorizontally(tween(220)) { -it / 2 } + fadeOut(tween(160)))
        },
        modifier = modifier,
        label = "swap",
    ) { content(it) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PillFlow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
}

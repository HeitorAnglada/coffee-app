package com.heitoranglada.coffeeapp.ui.brew

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.data.waterGrams
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.cumulativeGramsAt
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.domain.formatRatio
import com.heitoranglada.coffeeapp.domain.guidedProgress
import com.heitoranglada.coffeeapp.ui.components.BeanRating
import com.heitoranglada.coffeeapp.ui.components.BigPillButton
import com.heitoranglada.coffeeapp.ui.components.Blob
import com.heitoranglada.coffeeapp.ui.components.BloomDialog
import com.heitoranglada.coffeeapp.ui.components.BloomSwitch
import com.heitoranglada.coffeeapp.ui.components.CircleIconButton
import com.heitoranglada.coffeeapp.ui.components.Drop
import com.heitoranglada.coffeeapp.ui.components.ErrorSticker
import com.heitoranglada.coffeeapp.ui.components.ExpandableCard
import com.heitoranglada.coffeeapp.ui.components.Eyebrow
import com.heitoranglada.coffeeapp.ui.components.MethodChips
import com.heitoranglada.coffeeapp.ui.components.MethodHero
import com.heitoranglada.coffeeapp.ui.components.Pill
import com.heitoranglada.coffeeapp.ui.components.PillField
import com.heitoranglada.coffeeapp.ui.components.PillFlow
import com.heitoranglada.coffeeapp.ui.components.PourEditor
import com.heitoranglada.coffeeapp.ui.components.RollingText
import com.heitoranglada.coffeeapp.ui.components.SectionTitle
import com.heitoranglada.coffeeapp.ui.components.Sheet
import com.heitoranglada.coffeeapp.ui.components.SlideSwap
import com.heitoranglada.coffeeapp.ui.components.SuggestionRow
import com.heitoranglada.coffeeapp.ui.components.TextAction
import com.heitoranglada.coffeeapp.ui.components.Wordmark
import com.heitoranglada.coffeeapp.ui.components.blink
import com.heitoranglada.coffeeapp.ui.components.bob
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.dashedRing
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.components.popIn
import com.heitoranglada.coffeeapp.ui.components.rememberLoop
import com.heitoranglada.coffeeapp.ui.components.wiggle
import com.heitoranglada.coffeeapp.ui.recipes.FloatingNavSpace
import com.heitoranglada.coffeeapp.ui.recipes.pourHint
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import com.heitoranglada.coffeeapp.ui.theme.palette
import kotlinx.coroutines.launch

/**
 * Tela de preparo. Com [recipeId] == 0 é o "preparo livre"; caso contrário, guia a receita.
 * @param onBack null quando a tela é uma aba (sem botão voltar).
 * @param onImmersive avisa quando o preparo está em andamento (a barra de navegação some).
 */
@Composable
fun BrewScreen(
    recipeId: Long,
    onBack: (() -> Unit)?,
    onSaved: (Long) -> Unit,
    onImmersive: (Boolean) -> Unit = {},
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

    LaunchedEffect(state.phase) { onImmersive(state.phase != BrewPhase.READY) }
    DisposableEffect(Unit) { onDispose { onImmersive(false) } }

    // Mantém a tela ligada enquanto o timer corre.
    val view = LocalView.current
    val running = state.phase == BrewPhase.RUNNING
    DisposableEffect(running) {
        view.keepScreenOn = running
        onDispose { view.keepScreenOn = false }
    }

    BackHandler(enabled = state.phase != BrewPhase.READY) { confirmDiscard = true }

    val method = when (state.phase) {
        BrewPhase.REVIEW -> state.form.method
        else -> state.recipe?.method ?: state.method
    }
    val freeReady = !state.isGuided && state.phase == BrewPhase.READY
    val background by animateColorAsState(
        if (freeReady || state.phase == BrewPhase.REVIEW || state.loading) Bloom.Cream else method.palette.bg,
        tween(500),
        label = "brew-bg",
    )

    Box(Modifier.fillMaxSize().background(background)) {
        if (state.loading) {
            Blob(Bloom.Terra, Modifier.size(110.dp).align(Alignment.Center), periodMs = 2000, wobble = 0.12f)
        } else {
            AnimatedContent(
                targetState = state.phase,
                transitionSpec = {
                    (fadeIn(tween(350, delayMillis = 120)) + scaleIn(tween(450, delayMillis = 120), initialScale = 0.92f))
                        .togetherWith(fadeOut(tween(200)) + scaleOut(tween(250), targetScale = 1.06f))
                },
                label = "brew-phase",
            ) { phase ->
                when (phase) {
                    BrewPhase.READY -> {
                        val recipe = state.recipe
                        if (recipe != null) GuidedReady(recipe, onBack, vm::start) else FreeReady(state.method, vm::selectMethod, vm::start)
                    }
                    BrewPhase.RUNNING -> {
                        val recipe = state.recipe
                        if (recipe != null) GuidedRunning(recipe, vm) { confirmDiscard = true } else FreeRunning(state, vm)
                    }
                    BrewPhase.REVIEW -> ReviewContent(state, vm) { confirmDiscard = true }
                }
            }
        }
    }

    if (confirmDiscard) {
        BloomDialog(
            title = "Descartar preparo?",
            text = "O timer e os despejos marcados serão perdidos.",
            confirm = "Descartar",
            onConfirm = {
                confirmDiscard = false
                vm.reset()
                onBack?.invoke()
            },
            dismiss = "Continuar",
            onDismiss = { confirmDiscard = false },
        )
    }
}

/** O botão de começar: um bloom que respira, com um halo atrás. */
@Composable
private fun StartBloom(color: Color, contentColor: Color, halo: Color, onClick: () -> Unit, label: String = "Começar") {
    Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
        Blob(halo, Modifier.size(272.dp), periodMs = 7000, wobble = 0.08f, seed = 3)
        Blob(
            color,
            Modifier
                .size(228.dp)
                .bouncyClick(pressedScale = 0.88f, onClickLabel = label, onClick = onClick),
            periodMs = 6000,
            wobble = 0.08f,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.PlayArrow, null, tint = contentColor, modifier = Modifier.size(52.dp))
                Text(label, style = BloomType.display(30.sp, -0.02f), color = contentColor)
            }
        }
    }
}

@Composable
private fun GuidedReady(recipe: Recipe, onBack: (() -> Unit)?, onStart: () -> Unit) {
    val p = recipe.method.palette
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBack, background = Bloom.Cream.copy(alpha = 0.3f), contentColor = p.fg)
            }
        }
        Eyebrow(listOf(recipe.method.label, recipe.coffee).filter { it.isNotBlank() }.joinToString(" · "), Modifier.enterCascade(0), p.fg.copy(alpha = 0.75f))
        Text(recipe.name, style = BloomType.display(40.sp, -0.035f), color = p.fg, modifier = Modifier.padding(top = 4.dp).enterCascade(1))
        PillFlow(Modifier.padding(top = 14.dp).enterCascade(2)) {
            recipe.doseGrams?.let { Pill("${formatGrams(it)} de café", p.chip, contentColor = p.fg) }
            if (recipe.waterGrams > 0) Pill("${formatGrams(recipe.waterGrams)} de água", p.chip, contentColor = p.fg)
            recipe.targetTimeSeconds?.let { Pill(formatDuration(it), p.chip, contentColor = p.fg) }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            StartBloom(Bloom.Cream, Bloom.Ink, Bloom.Cream.copy(alpha = 0.25f), onStart)
        }
        Text(
            "Balança zerada e café no coador? Toque em Começar junto com o primeiro despejo: eu aviso a hora de cada um.",
            style = BloomType.body,
            color = p.fg,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 24.dp).enterCascade(3),
        )
    }
}

@Composable
private fun FreeReady(method: BrewMethod, onSelect: (BrewMethod) -> Unit, onStart: () -> Unit) {
    val p = method.palette
    val color by animateColorAsState(p.bg, tween(450), label = "start-color")
    val content by animateColorAsState(p.fg, tween(450), label = "start-fg")
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = FloatingNavSpace + navBottom),
    ) {
        Wordmark(Modifier.enterCascade(0))
        Text(
            buildAnnotatedString {
                append("Sem receita,\n")
                withStyle(SpanStyle(color = Bloom.Terra)) { append("no improviso.") }
            },
            style = BloomType.display(44.sp, -0.035f),
            color = Bloom.Ink,
            modifier = Modifier.padding(top = 12.dp).enterCascade(1),
        )
        MethodChips(method, { it?.let(onSelect) }, Modifier.padding(top = 18.dp).enterCascade(2))
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            StartBloom(color, content, color.copy(alpha = 0.25f), onStart)
        }
        Text(
            buildAnnotatedString {
                append("Toque junto com o ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Bloom.Ink)) { append("primeiro despejo") }
                append(". A cada novo despejo, toque de novo. No fim, você conta quantos gramas foram.")
            },
            style = BloomType.body,
            color = Bloom.Body,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).enterCascade(3),
        )
    }
}

/** O timer no centro de um bloom creme, com anéis tracejados girando em volta. */
@Composable
private fun TimerBloom(
    elapsedText: String,
    subtitle: String?,
    fill: Float,
    ringColor: Color,
    outerLabel: String?,
    innerLabel: String?,
) {
    val rotation by rememberLoop(40_000)
    val scale by animateFloatAsState(0.8f + 0.2f * fill.coerceIn(0f, 1f), spring(dampingRatio = 0.5f, stiffness = 120f), label = "bloom-size")
    val density = LocalDensity.current
    Box(Modifier.size(340.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(340.dp)) {
            val w = with(density) { 1.5.dp.toPx() }
            dashedRing(size.minDimension / 2f - w, ringColor.copy(alpha = 0.5f), w, rotation * 360f)
            dashedRing(size.minDimension * 0.376f, ringColor.copy(alpha = 0.5f), w, -rotation * 360f)
        }
        Blob(Bloom.Cream, Modifier.size(232.dp * scale), periodMs = 6000, wobble = 0.08f) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RollingText(elapsedText, BloomType.display(88.sp, -0.04f), Bloom.Ink)
                if (subtitle != null) Text(subtitle, style = BloomType.chip.copy(fontSize = 14.sp), color = Bloom.Ink)
            }
        }
        if (outerLabel != null) {
            Text(
                outerLabel,
                style = BloomType.chip,
                color = ringColor,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-6).dp, y = 10.dp),
            )
        }
        if (innerLabel != null) {
            SlideSwap(innerLabel, Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 44.dp)) {
                Pill(it, Bloom.Ink, contentColor = Bloom.Cream, height = 26.dp)
            }
        }
    }
}

private data class Sticker(val eyebrow: String, val main: String, val sub: String?)

@Composable
private fun GuidedRunning(recipe: Recipe, vm: BrewViewModel, onExit: () -> Unit) {
    val elapsedMs by vm.elapsedMs.collectAsStateWithLifecycle()
    val elapsed = (elapsedMs / 1000).toInt()
    val p = recipe.method.palette
    val pours = remember(recipe) { recipe.pours.sortedBy { it.startSeconds } }
    val progress = guidedProgress(pours, elapsed)
    val haptic = LocalHapticFeedback.current
    val target = recipe.targetTimeSeconds
    val targetReached = target != null && elapsed >= target
    val totalWater = recipe.waterGrams

    // Vibra quando chega a hora de um novo despejo.
    LaunchedEffect(progress.currentIndex) {
        if (progress.currentIndex >= 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val current = progress.currentIndex
    val lastStart = pours.lastOrNull()?.startSeconds ?: 0
    val sticker = when {
        current < 0 -> Sticker("PREPARE-SE", "Já vai começar", null)
        recipe.method == BrewMethod.FRENCH_PRESS && targetReached ->
            Sticker("HORA DE PRESSIONAR", "Pressione devagar", "Quebre a crosta antes e desça o êmbolo sem pressa.")
        current == pours.lastIndex && elapsed > lastStart + 25 ->
            Sticker("AGORA É ESPERAR", "Deixe escorrer", if (target != null && !targetReached) "O alvo é ${formatDuration(target)}. Finalize quando terminar." else "Finalize quando terminar de escorrer.")
        else -> Sticker(
            if (current == 0 && pours.size > 1) "AGORA · BLOOM" else "AGORA · ${current + 1}º DESPEJO",
            "Despeje ${formatGrams(pours[current].grams)}",
            "até a balança marcar ${formatGrams(pours.cumulativeGramsAt(current))}",
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, bottom = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Sair do preparo", onExit, background = Bloom.Cream.copy(alpha = 0.3f), contentColor = p.fg)
            Text(recipe.name, style = BloomType.display(20.sp, -0.01f), color = p.fg, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            StepDashes(pours.size, current, p.fg)
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val poured = if (current >= 0 && totalWater > 0) pours.cumulativeGramsAt(current) / totalWater else 0.0
            TimerBloom(
                elapsedText = formatDuration(elapsed),
                subtitle = target?.let { "de ${formatDuration(it)}" },
                fill = poured.toFloat(),
                ringColor = p.fg,
                outerLabel = if (totalWater > 0) formatGrams(totalWater) else null,
                innerLabel = if (current >= 0) formatGrams(pours.cumulativeGramsAt(current)) else null,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .wiggle()
                .clip(RoundedCornerShape(26.dp))
                .background(Bloom.Ink)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            SlideSwap(sticker) { s ->
                Column(Modifier.fillMaxWidth()) {
                    Text(s.eyebrow, style = BloomType.eyebrow, color = Bloom.Peach)
                    Text(s.main, style = BloomType.display(38.sp, -0.025f), color = Bloom.Cream, modifier = Modifier.padding(top = 4.dp))
                    if (s.sub != null) Text(s.sub, style = BloomType.body, color = Bloom.Cream.copy(alpha = 0.85f), modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        val next = progress.nextIndex
        val secondsToNext = progress.secondsToNext
        val nextText = when {
            next != null && secondsToNext != null ->
                "Próximo em ${formatDuration(secondsToNext)} · mais ${formatGrams(pours[next].grams)} até ${formatGrams(pours.cumulativeGramsAt(next))}"
            target != null && !targetReached -> "Último despejo. Alvo: ${formatDuration(target)}"
            target != null -> "Tempo-alvo atingido!"
            else -> "Último despejo. Finalize quando escorrer."
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val soon = secondsToNext != null && secondsToNext <= 5
            Box(Modifier.size(8.dp).then(if (soon) Modifier.blink(400) else Modifier).clip(CircleShape).background(p.fg))
            Spacer(Modifier.width(8.dp))
            Text(nextText, style = BloomType.chip.copy(fontSize = 14.sp), color = p.fg)
        }
        BigPillButton("Finalizar preparo", vm::finish, background = Bloom.Cream, contentColor = Bloom.Ink, height = 62.dp)
    }
}

/** Tracinhos de progresso dos despejos: feitos, atual (maior) e próximos. */
@Composable
private fun StepDashes(count: Int, current: Int, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(count.coerceAtMost(6)) { i ->
            val c by animateColorAsState(
                when {
                    i < current -> color
                    i == current -> Bloom.Cream
                    else -> color.copy(alpha = 0.25f)
                },
                label = "dash",
            )
            val w by animateDpAsState(if (i == current) 30.dp else 18.dp, spring(dampingRatio = 0.6f), label = "dash-w")
            Box(Modifier.size(width = w, height = 8.dp).clip(RoundedCornerShape(50)).background(c))
        }
    }
}

@Composable
private fun FreeRunning(state: BrewUiState, vm: BrewViewModel) {
    val elapsedMs by vm.elapsedMs.collectAsStateWithLifecycle()
    val elapsed = (elapsedMs / 1000).toInt()
    val p = state.method.palette
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val splash = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.pourMarks.size) {
        if (state.pourMarks.isNotEmpty()) listState.animateScrollToItem(state.pourMarks.size)
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Preparo livre · ${if (state.method == BrewMethod.FRENCH_PRESS) "Prensa" else state.method.label}",
                style = BloomType.display(20.sp, -0.01f),
                color = p.fg,
                modifier = Modifier.weight(1f),
            )
            Row(
                Modifier.height(30.dp).clip(RoundedCornerShape(50)).background(Bloom.Ink).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).blink().clip(CircleShape).background(Bloom.Mustard))
                Spacer(Modifier.width(6.dp))
                Text("gravando", style = BloomType.chip.copy(fontSize = 12.sp), color = Bloom.Cream)
            }
        }
        RollingText(formatDuration(elapsed), BloomType.display(120.sp, -0.05f), p.fg, Modifier.padding(top = 4.dp))
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
        ) {
            itemsIndexed(state.pourMarks, key = { i, _ -> i }) { i, s ->
                Column(Modifier.popIn().animateItem(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Drop("${i + 1}", fill = p.fg, textColor = p.bg, textStyle = BloomType.display(16.sp, 0f))
                    Text(formatDuration(s), style = BloomType.chip, color = p.fg, modifier = Modifier.padding(top = 4.dp))
                }
            }
            item(key = "next") {
                Column(Modifier.animateItem(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Drop(null, fill = p.fg.copy(alpha = 0.45f), textColor = p.bg, dashed = true)
                    Text("próximo", style = BloomType.chip, color = p.fg.copy(alpha = 0.45f), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Blob(Bloom.Cream.copy(alpha = 0.25f), Modifier.size(280.dp, 258.dp), periodMs = 5000, wobble = 0.09f, seed = 5)
            // Onda que se espalha a cada toque, como a água caindo no café.
            Canvas(Modifier.size(300.dp)) {
                val s = splash.value
                if (s < 1f) {
                    drawCircle(
                        color = p.fg.copy(alpha = (1f - s) * 0.8f),
                        radius = size.minDimension / 2f * (0.6f + 0.4f * s),
                        center = Offset(size.width / 2f, size.height / 2f),
                        style = Stroke(width = 6.dp.toPx() * (1f - s) + 1f),
                    )
                }
            }
            Blob(
                Bloom.Ink,
                Modifier
                    .size(244.dp, 224.dp)
                    .bouncyClick(pressedScale = 0.86f, onClickLabel = "Novo despejo") {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.markPour()
                        scope.launch {
                            splash.snapTo(0f)
                            splash.animateTo(1f, tween(650))
                        }
                    },
                periodMs = 6000,
                wobble = 0.08f,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.bob(), contentAlignment = Alignment.Center) {
                        Drop(null, fill = p.bg.takeIf { state.method != BrewMethod.FRENCH_PRESS } ?: Bloom.Mustard, textColor = Bloom.Ink, width = 40.dp)
                        Icon(Icons.Filled.Add, null, tint = Bloom.Ink, modifier = Modifier.size(22.dp).offset(y = 6.dp))
                    }
                    Text("Novo despejo", style = BloomType.display(28.sp, -0.02f), color = Bloom.Cream, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(
                Icons.AutoMirrored.Filled.Undo,
                "Desfazer último despejo",
                vm::undoLastPour,
                background = Color.Transparent,
                contentColor = p.fg,
                size = 62.dp,
                border = p.fg,
            )
            BigPillButton("Finalizar", vm::finish, Modifier.weight(1f), background = Bloom.Cream, contentColor = Bloom.Ink, height = 62.dp)
        }
    }
}

@Composable
private fun ReviewContent(state: BrewUiState, vm: BrewViewModel, onDiscard: () -> Unit) {
    val coffees by vm.coffees.collectAsStateWithLifecycle()
    val grinders by vm.grinders.collectAsStateWithLifecycle()
    val form = state.form
    val onChange = vm::onFormChange
    val p = form.method.palette
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scroll),
    ) {
        MethodHero(
            form.method,
            scroll = scroll,
            bottomPadding = 48.dp,
            topBar = {
                Eyebrow("${form.method.label} · ${form.pours.size} despejo${if (form.pours.size == 1) "" else "s"}", color = p.fg.copy(alpha = 0.75f))
            },
        ) {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.enterCascade(0)) {
                Text(formatDuration(state.totalSeconds), style = BloomType.display(76.sp, -0.04f), color = p.fg)
                Text("  Boa!", style = BloomType.display(26.sp, -0.02f), color = p.fg, modifier = Modifier.padding(bottom = 10.dp).popIn(300))
            }
        }
        Sheet {
            SectionTitle("Quanto foi cada despejo?", Modifier.enterCascade(1))
            if (!state.isGuided) {
                Text("Os tempos já estão marcados. Falta só dizer os gramas.", style = BloomType.bodySmall, color = Bloom.Body)
            }
            PourEditor(form.pours, { onChange(form.copy(pours = it)) }, highlightEmpty = true)
            val water = form.waterGrams
            Text(
                listOfNotNull(
                    "${formatGrams(water)} no total",
                    formatRatio(form.dose, water)?.let { "proporção $it" },
                ).joinToString(" · "),
                style = BloomType.chip.copy(fontSize = 14.sp),
                color = Bloom.Ink,
                modifier = Modifier.align(Alignment.End),
            )

            SectionTitle("Café e moagem")
            if (!state.isGuided) Text("Já preenchi com o último preparo.", style = BloomType.bodySmall, color = Bloom.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillField(form.coffee, { onChange(form.copy(coffee = it)) }, Modifier.weight(1.5f), label = "Café", placeholder = "Qual café?")
                PillField(form.doseText, { onChange(form.copy(doseText = it)) }, Modifier.weight(1f), label = "Dose", placeholder = "15", suffix = "g", keyboardType = KeyboardType.Decimal)
            }
            SuggestionRow(form.coffee, coffees) { onChange(form.copy(coffee = it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillField(form.grinder, { onChange(form.copy(grinder = it)) }, Modifier.weight(1.4f), label = "Moedor", placeholder = "Moedor")
                PillField(form.grindSetting, { onChange(form.copy(grindSetting = it)) }, Modifier.weight(1f), label = "Moagem", placeholder = "24 cliques")
            }
            SuggestionRow(form.grinder, grinders) { onChange(form.copy(grinder = it)) }

            SectionTitle("Como ficou?")
            BeanRating(form.rating, { onChange(form.copy(rating = it)) })

            ExpandableCard("Mais opções", "temperatura, tempo total e notas") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillField(form.waterTempText, { onChange(form.copy(waterTempText = it)) }, Modifier.weight(1f), label = "Temperatura", placeholder = "93", suffix = "°C", keyboardType = KeyboardType.Number)
                    PillField(form.timeText, { onChange(form.copy(timeText = it)) }, Modifier.weight(1f), label = "Tempo total", placeholder = "3:00")
                }
                PillField(form.notes, { onChange(form.copy(notes = it)) }, label = "Notas", placeholder = "Sabor, acidez, doçura, o que mudar…", singleLine = false)
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Bloom.Mustard)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (state.isGuided) "Salvar como nova receita" else "Virar receita também",
                        style = BloomType.display(18.sp, -0.01f),
                        color = Bloom.Ink,
                        modifier = Modifier.weight(1f),
                    )
                    BloomSwitch(state.saveAsRecipe, vm::setSaveAsRecipe)
                }
                AnimatedVisibility(state.saveAsRecipe, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    PillField(form.name, { onChange(form.copy(name = it)) }, placeholder = "Nome da receita")
                }
            }

            ErrorSticker(state.errors)
            BigPillButton("Salvar no histórico", vm::save)
            TextAction("Descartar", onDiscard, Modifier.align(Alignment.CenterHorizontally), color = Bloom.Muted)
            Spacer(Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

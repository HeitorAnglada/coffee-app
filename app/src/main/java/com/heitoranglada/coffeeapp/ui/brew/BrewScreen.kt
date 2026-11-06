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
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.heitoranglada.coffeeapp.domain.EspressoWindows
import com.heitoranglada.coffeeapp.domain.ShotSpeed
import com.heitoranglada.coffeeapp.domain.StepKind
import com.heitoranglada.coffeeapp.domain.espressoTip
import com.heitoranglada.coffeeapp.domain.formatShotSeconds
import com.heitoranglada.coffeeapp.domain.shotSpeed
import com.heitoranglada.coffeeapp.ui.components.doneLabel
import com.heitoranglada.coffeeapp.ui.components.icon
import com.heitoranglada.coffeeapp.ui.components.shortPills
import com.heitoranglada.coffeeapp.ui.components.windowMax
import com.heitoranglada.coffeeapp.ui.components.windowMin
import com.heitoranglada.coffeeapp.ui.recipes.EspressoQuantity
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
import com.heitoranglada.coffeeapp.ui.appViewModelFactory
import com.heitoranglada.coffeeapp.data.journal.BeanReminder
import com.heitoranglada.coffeeapp.ui.journal.BeanPills
import com.heitoranglada.coffeeapp.ui.journal.BeanTiles
import com.heitoranglada.coffeeapp.ui.journal.DiaryFields
import com.heitoranglada.coffeeapp.ui.journal.ReminderCard
import com.heitoranglada.coffeeapp.ui.journal.ReminderStrip
import com.heitoranglada.coffeeapp.ui.calculator.CalculatorPill
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
    onOpenCalculator: () -> Unit = {},
    onNewBean: () -> Unit = {},
    vm: BrewViewModel = viewModel(
        key = "brew-$recipeId",
        factory = appViewModelFactory { BrewViewModel(it.repository, recipeId, it.journal, it.calculator) },
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
                        val reminder = state.reminder.takeUnless { state.reminderDismissed }
                        if (recipe != null) {
                            GuidedReady(recipe, onBack, vm::start, reminder, vm::dismissReminder)
                        } else {
                            FreeReady(state, vm::selectMethod, vm::start, vm::selectBean, onOpenCalculator)
                        }
                    }
                    BrewPhase.RUNNING -> {
                        val recipe = state.recipe
                        when {
                            state.currentMethod.isEspresso -> EspressoRunning(state, vm) { confirmDiscard = true }
                            recipe != null -> GuidedRunning(recipe, vm) { confirmDiscard = true }
                            else -> FreeRunning(state, vm)
                        }
                    }
                    BrewPhase.REVIEW ->
                        if (state.form.method.isEspresso) EspressoReview(state, vm) { confirmDiscard = true } else ReviewContent(state, vm, onNewBean) { confirmDiscard = true }
                }
            }
        }
    }

    if (confirmDiscard) {
        BloomDialog(
            title = "Descartar preparo?",
            text = if (method.isEspresso) "O tempo deste shot será perdido." else "O timer e as etapas marcadas serão perdidos.",
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
private fun GuidedReady(
    recipe: Recipe,
    onBack: (() -> Unit)?,
    onStart: () -> Unit,
    reminder: BeanReminder? = null,
    onDismissReminder: () -> Unit = {},
) {
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
            recipe.shortPills().forEach { Pill(it, p.chip, contentColor = p.fg) }
        }
        // Lembrete do diário: o que mudar desta vez com este café.
        AnimatedVisibility(
            reminder != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically() + scaleOut(targetScale = 0.9f),
        ) {
            reminder?.let { ReminderCard(it, onDismissReminder, Modifier.padding(top = 18.dp)) }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            StartBloom(Bloom.Cream, Bloom.Ink, Bloom.Cream.copy(alpha = 0.25f), onStart)
        }
        Text(
            when (recipe.method) {
                BrewMethod.ESPRESSO -> "Porta-filtro encaixado e xícara na balança? Toque em Começar junto com o botão da máquina."
                BrewMethod.AEROPRESS -> "Café na câmara e balança zerada? Toque em Começar junto com o primeiro despejo: eu aviso cada etapa."
                else -> "Balança zerada e café no coador? Toque em Começar junto com o primeiro despejo: eu aviso a hora de cada um."
            },
            style = BloomType.body,
            color = p.fg,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 24.dp).enterCascade(3),
        )
    }
}

@Composable
private fun FreeReady(
    state: BrewUiState,
    onSelect: (BrewMethod) -> Unit,
    onStart: () -> Unit,
    onSelectBean: (Long?) -> Unit = {},
    onOpenCalculator: () -> Unit = {},
) {
    val method = state.method
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
        Row(Modifier.fillMaxWidth().enterCascade(0), verticalAlignment = Alignment.CenterVertically) {
            Wordmark(Modifier.weight(1f))
            CalculatorPill(state.plan, onOpenCalculator)
        }
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
        if (state.beans.isNotEmpty()) {
            Text("Com qual café?", style = BloomType.label, color = Bloom.Muted, modifier = Modifier.padding(top = 16.dp).enterCascade(3))
            BeanPills(state.beans, state.selectedBeanId, onSelectBean, Modifier.padding(top = 6.dp).enterCascade(3))
        }
        val reminder = state.reminder.takeIf { state.selectedBeanId != null }
        AnimatedVisibility(
            reminder != null,
            enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + shrinkVertically(),
        ) {
            reminder?.let { ReminderStrip(it, Modifier.padding(top = 14.dp)) }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            StartBloom(color, content, color.copy(alpha = 0.25f), onStart)
        }
        Text(
            buildAnnotatedString {
                append("Toque junto com o ")
                when (method) {
                    BrewMethod.ESPRESSO -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Bloom.Ink)) { append("botão da máquina") }
                        append(". Pare quando a balança marcar o que você quer na xícara.")
                    }
                    BrewMethod.AEROPRESS -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Bloom.Ink)) { append("primeiro despejo") }
                        append(". Depois marque cada etapa: mexer, virar, prensar. No fim, você conta os gramas.")
                    }
                    else -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Bloom.Ink)) { append("primeiro despejo") }
                        append(". A cada novo despejo, toque de novo. No fim, você conta quantos gramas foram.")
                    }
                }
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
    val cur = pours.getOrNull(current)
    val isAero = recipe.method == BrewMethod.AEROPRESS
    val waterOrdinal = if (cur != null) pours.take(current + 1).count { it.kind == StepKind.POUR } else 0
    val sticker = when {
        cur == null -> Sticker("PREPARE-SE", "Já vai começar", null)
        recipe.method == BrewMethod.FRENCH_PRESS && targetReached ->
            Sticker("HORA DE PRESSIONAR", "Pressione devagar", "Quebre a crosta antes e desça o êmbolo sem pressa.")
        current == pours.lastIndex && cur.kind != StepKind.POUR && cur.kind != StepKind.PRESS && elapsed > lastStart + 20 ->
            Sticker("PRONTO", "Pode servir", "Finalize o preparo e conte como ficou.")
        cur.kind == StepKind.STIR -> Sticker("AGORA · ${current + 1}ª ETAPA", "Mexa", "Com firmeza, molhando todo o pó.")
        cur.kind == StepKind.FLIP -> Sticker("AGORA · ${current + 1}ª ETAPA", "Vire", "Filtro umedecido, caneca por cima, e vire de uma vez.")
        cur.kind == StepKind.PRESS -> Sticker("AGORA · ${current + 1}ª ETAPA", "Prense devagar", "Pare no primeiro chiado.")
        cur.kind == StepKind.DILUTE -> Sticker("AGORA · ${current + 1}ª ETAPA", "Dilua com ${formatGrams(cur.grams)}", "Água quente direto na caneca, até ${formatGrams(pours.cumulativeGramsAt(current))}.")
        current == pours.lastIndex && elapsed > lastStart + 25 ->
            Sticker("AGORA É ESPERAR", "Deixe escorrer", if (target != null && !targetReached) "O alvo é ${formatDuration(target)}. Finalize quando terminar." else "Finalize quando terminar de escorrer.")
        else -> Sticker(
            when {
                isAero -> "AGORA · ${current + 1}ª ETAPA"
                waterOrdinal == 1 && pours.count { it.kind == StepKind.POUR } > 1 -> "AGORA · BLOOM"
                else -> "AGORA · ${waterOrdinal}º DESPEJO"
            },
            "Despeje ${formatGrams(cur.grams)}",
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
            cur?.kind == StepKind.PRESS && secondsToNext != null && pours[next!!].kind == StepKind.DILUTE ->
                "Prensando · faltam ${secondsToNext} s · depois dilua"
            cur?.kind == StepKind.PRESS && (secondsToNext ?: target?.minus(elapsed)?.takeIf { it > 0 }) != null ->
                "Prensando · faltam ${secondsToNext ?: (target!! - elapsed)} s"
            next != null && secondsToNext != null && pours[next].kind == StepKind.POUR ->
                "Próximo em ${formatDuration(secondsToNext)} · mais ${formatGrams(pours[next].grams)} até ${formatGrams(pours.cumulativeGramsAt(next))}"
            next != null && secondsToNext != null && pours[next].kind == StepKind.DILUTE ->
                "Próximo em ${formatDuration(secondsToNext)} · diluir com ${formatGrams(pours[next].grams)}"
            next != null && secondsToNext != null ->
                "Próximo em ${formatDuration(secondsToNext)} · ${pours[next].kind.label.lowercase()}"
            isAero && target != null && targetReached -> "Tempo-alvo atingido!"
            isAero -> "Última etapa. Finalize quando terminar."
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

/** Botões do preparo livre da AeroPress: prensar em destaque e as outras etapas em pílulas. */
@Composable
private fun AeroActions(color: Color, splash: Float, onMark: (StepKind) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("O QUE VOCÊ FEZ AGORA?", style = BloomType.eyebrow, color = color)
        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                if (splash < 1f) {
                    drawCircle(
                        color = color.copy(alpha = (1f - splash) * 0.8f),
                        radius = size.height / 2f * (0.7f + 0.6f * splash),
                        center = Offset(size.width / 2f, size.height / 2f),
                        style = Stroke(width = 6.dp.toPx() * (1f - splash) + 1f),
                    )
                }
            }
            Blob(
                Bloom.Ink,
                Modifier.fillMaxSize().bouncyClick(pressedScale = 0.9f, onClickLabel = "Prensar") { onMark(StepKind.PRESS) },
                periodMs = 6000,
                wobble = 0.07f,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(StepKind.PRESS.icon, null, tint = Bloom.Peach, modifier = Modifier.size(36.dp).bob())
                    Text("Prensar", style = BloomType.display(26.sp, -0.02f), color = Bloom.Cream, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        listOf(listOf(StepKind.POUR to "Despejei", StepKind.STIR to "Mexi"), listOf(StepKind.FLIP to "Virei", StepKind.DILUTE to "Diluí")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (kind, text) ->
                    Row(
                        Modifier
                            .weight(1f)
                            .height(58.dp)
                            .bouncyClick(pressedScale = 0.9f, onClickLabel = text) { onMark(kind) }
                            .clip(RoundedCornerShape(50))
                            .background(Bloom.Cream.copy(alpha = 0.35f))
                            .border(2.5.dp, color, RoundedCornerShape(50)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(kind.icon, null, tint = color, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text, style = BloomType.display(17.sp, -0.01f), color = color)
                    }
                }
            }
        }
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

    LaunchedEffect(state.marks.size) {
        if (state.marks.isNotEmpty()) listState.animateScrollToItem(state.marks.size)
    }
    val isAero = state.method == BrewMethod.AEROPRESS
    val onMark: (StepKind) -> Unit = { kind ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        vm.mark(kind)
        scope.launch {
            splash.snapTo(0f)
            splash.animateTo(1f, tween(650))
        }
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
                "Preparo livre · ${state.method.shortLabel}",
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
        RollingText(formatDuration(elapsed), BloomType.display(if (isAero) 104.sp else 120.sp, -0.05f), p.fg, Modifier.padding(top = 4.dp))
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
        ) {
            itemsIndexed(state.marks, key = { i, _ -> i }) { i, mark ->
                Column(Modifier.popIn().animateItem(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isAero) {
                        Box(Modifier.size(50.dp).clip(CircleShape).background(p.fg), contentAlignment = Alignment.Center) {
                            Icon(mark.kind.icon, mark.kind.label, tint = p.bg, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        val n = state.marks.take(i + 1).count { it.kind == StepKind.POUR }
                        Drop("$n", fill = p.fg, textColor = p.bg, textStyle = BloomType.display(16.sp, 0f))
                    }
                    Text(formatDuration(mark.startSeconds), style = BloomType.chip, color = p.fg, modifier = Modifier.padding(top = 4.dp))
                    if (isAero) Text(mark.kind.doneLabel, style = BloomType.chip.copy(fontSize = 11.sp), color = p.fg.copy(alpha = 0.7f))
                }
            }
            item(key = "next") {
                Column(Modifier.animateItem(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isAero) {
                        Canvas(Modifier.size(50.dp)) { dashedRing(size.minDimension / 2f - 2.dp.toPx(), p.fg.copy(alpha = 0.45f), 2.dp.toPx(), 0f) }
                    } else {
                        Drop(null, fill = p.fg.copy(alpha = 0.45f), textColor = p.bg, dashed = true)
                    }
                    Text(if (isAero) "próxima" else "próximo", style = BloomType.chip, color = p.fg.copy(alpha = 0.45f), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        if (isAero) {
            Spacer(Modifier.weight(1f))
            AeroActions(p.fg, splash.value, onMark)
            Spacer(Modifier.height(14.dp))
        } else {
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
                        .bouncyClick(pressedScale = 0.86f, onClickLabel = "Novo despejo") { onMark(StepKind.POUR) },
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
private fun ReviewContent(state: BrewUiState, vm: BrewViewModel, onNewBean: () -> Unit, onDiscard: () -> Unit) {
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
                val n = if (form.method == BrewMethod.AEROPRESS) form.pours.size else form.pours.count { it.kind.addsWater }
                val what = if (form.method == BrewMethod.AEROPRESS) "etapa" else "despejo"
                Eyebrow("${form.method.label} · $n $what${if (n == 1) "" else "s"}", color = p.fg.copy(alpha = 0.75f))
            },
        ) {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.enterCascade(0)) {
                Text(formatDuration(state.totalSeconds), style = BloomType.display(76.sp, -0.04f), color = p.fg)
                Text("  Boa!", style = BloomType.display(26.sp, -0.02f), color = p.fg, modifier = Modifier.padding(bottom = 10.dp).popIn(300))
            }
        }
        Sheet {
            SectionTitle(if (form.method == BrewMethod.AEROPRESS) "Como foram as etapas?" else "Quanto foi cada despejo?", Modifier.enterCascade(1))
            if (!state.isGuided) {
                Text("Os tempos já estão marcados. Falta só dizer os gramas.", style = BloomType.bodySmall, color = Bloom.Body)
            }
            PourEditor(
                form.pours,
                { onChange(form.copy(pours = it)) },
                highlightEmpty = true,
                stepKinds = if (form.method == BrewMethod.AEROPRESS) StepKind.entries else listOf(StepKind.POUR),
            )
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
            if (state.beans.isNotEmpty()) {
                Text("Qual café?", style = BloomType.label, color = Bloom.Muted)
                BeanTiles(state.beans, state.selectedBeanId, vm::selectReviewBean, onNewBean)
            }
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

            DiaryFields(state.liked, state.change, vm::setLiked, vm::setChange, state.selectedBean?.name, form.coffee)

            ExpandableCard("Mais opções", "temperatura, tempo total e outras notas") {
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

/** Mostrador do shot: a janela-alvo acesa no anel e o ponteiro do tempo correndo por cima. */
@Composable
private fun ShotDial(seconds: Double, windowMin: Int, windowMax: Int, modifier: Modifier = Modifier) {
    val full = maxOf(40, windowMax + 10).toFloat()
    val measurer = rememberTextMeasurer()
    val speed = shotSpeed(seconds, windowMin, windowMax)
    val inWindow = speed == ShotSpeed.IN_WINDOW
    val blobColor by animateColorAsState(
        when (speed) {
            ShotSpeed.IN_WINDOW -> Bloom.Mustard
            ShotSpeed.SLOW, ShotSpeed.TOO_SLOW -> Bloom.Peach
            else -> Bloom.Terra
        },
        tween(400),
        label = "shot-blob",
    )
    val pulseLoop by rememberLoop(1200, reverse = true)
    val pulse = if (inWindow) pulseLoop else 0f
    Box(modifier.size(330.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 18.dp.toPx()
            val r = size.minDimension / 2f - 34.dp.toPx()
            val topLeft = Offset(center.x - r, center.y - r)
            val arcSize = androidx.compose.ui.geometry.Size(r * 2, r * 2)
            drawCircle(Bloom.Cream.copy(alpha = 0.12f), radius = r, style = Stroke(stroke))
            drawArc(
                Bloom.Mustard,
                startAngle = -90f + 360f * windowMin / full,
                sweepAngle = 360f * (windowMax - windowMin) / full,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke),
            )
            drawArc(
                Bloom.Cream,
                startAngle = -90f,
                sweepAngle = 360f * (seconds.toFloat().coerceAtMost(full) / full),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round),
            )
            listOf(0, 10, 20, 30).filter { it < full }.forEach { t ->
                val a = Math.toRadians((360.0 * t / full) - 90.0)
                val lr = r + 24.dp.toPx()
                val layout = measurer.measure("$t", BloomType.chip.copy(color = Bloom.Cream.copy(alpha = 0.6f)))
                drawText(
                    layout,
                    topLeft = Offset(
                        center.x + (lr * kotlin.math.cos(a)).toFloat() - layout.size.width / 2f,
                        center.y + (lr * kotlin.math.sin(a)).toFloat() - layout.size.height / 2f,
                    ),
                )
            }
        }
        Blob(blobColor, Modifier.size(196.dp), periodMs = 3000, wobble = 0.09f) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatShotSeconds(seconds), style = BloomType.display(76.sp, -0.04f), color = Bloom.Ink)
                Text("segundos", style = BloomType.chip.copy(fontSize = 14.sp), color = Bloom.Ink)
            }
        }
        if (seconds >= windowMin - 5) {
            Pill(
                speed.label,
                if (inWindow) Bloom.Mustard else Bloom.Cream,
                Modifier.align(Alignment.BottomCenter).graphicsLayer {
                    val sc = 1f + 0.06f * pulse
                    scaleX = sc
                    scaleY = sc
                },
                height = 28.dp,
            )
        }
    }
}

@Composable
private fun EspressoRunning(state: BrewUiState, vm: BrewViewModel, onExit: () -> Unit) {
    val elapsedMs by vm.elapsedMs.collectAsStateWithLifecycle()
    val seconds = elapsedMs / 1000.0
    val recipe = state.recipe
    val wMin = recipe?.windowMin ?: EspressoWindows.DEFAULT_MIN
    val wMax = recipe?.windowMax ?: EspressoWindows.DEFAULT_MAX
    val haptic = LocalHapticFeedback.current
    val inWindow = seconds >= wMin && seconds <= wMax
    LaunchedEffect(inWindow) { if (inWindow) haptic.performHapticFeedback(HapticFeedbackType.LongPress) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Sair do shot", onExit, background = Bloom.Cream.copy(alpha = 0.14f), contentColor = Bloom.Cream)
            Text(recipe?.name ?: "Shot livre", style = BloomType.display(20.sp, -0.01f), color = Bloom.Cream, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Row(
                Modifier.height(30.dp).clip(RoundedCornerShape(50)).background(Bloom.Terra).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).blink().clip(CircleShape).background(Bloom.Ink))
                Spacer(Modifier.width(6.dp))
                Text("extraindo", style = BloomType.chip.copy(fontSize = 12.sp), color = Bloom.Ink)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            ShotDial(seconds, wMin, wMax)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .wiggle()
                .clip(RoundedCornerShape(26.dp))
                .background(Bloom.Cream)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text("FICA DE OLHO NA BALANÇA", style = BloomType.eyebrow, color = Bloom.Terra)
            Text(
                recipe?.yieldGrams?.let { "Pare em ${formatGrams(it)}" } ?: "Pare no ponto",
                style = BloomType.display(38.sp, -0.025f),
                color = Bloom.Ink,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text("janela de $wMin a $wMax s", style = BloomType.body, color = Bloom.Body, modifier = Modifier.padding(top = 6.dp))
        }
        Spacer(Modifier.height(18.dp))
        BigPillButton(
            "Parar",
            vm::finish,
            background = Bloom.Terra,
            contentColor = Bloom.Ink,
            height = 72.dp,
            leading = { Icon(Icons.Filled.Stop, null, tint = Bloom.Ink, modifier = Modifier.size(26.dp)) },
        )
    }
}

@Composable
private fun EspressoReview(state: BrewUiState, vm: BrewViewModel, onDiscard: () -> Unit) {
    val coffees by vm.coffees.collectAsStateWithLifecycle()
    val grinders by vm.grinders.collectAsStateWithLifecycle()
    val form = state.form
    val onChange = vm::onFormChange
    val scroll = rememberScrollState()
    val seconds = state.shotSeconds
    val speed = shotSpeed(seconds, form.windowMin, form.windowMax)
    val tip = espressoTip(seconds, form.windowMin, form.windowMax)
    val shotNumber = state.todayShots.size + 1

    Column(Modifier.fillMaxSize().imePadding().verticalScroll(scroll)) {
        MethodHero(
            BrewMethod.ESPRESSO,
            scroll = scroll,
            bottomPadding = 48.dp,
            topBar = {
                Eyebrow("${(state.recipe?.name ?: "Espresso").uppercase()} · ${shotNumber}º SHOT DE HOJE", color = Bloom.Cream.copy(alpha = 0.7f))
            },
        ) {
            Text("${formatShotSeconds(seconds)} s", style = BloomType.display(76.sp, -0.04f), color = Bloom.Cream, modifier = Modifier.enterCascade(0))
            Pill(
                if (speed == ShotSpeed.IN_WINDOW) "dentro da janela" else speed.label,
                if (speed == ShotSpeed.IN_WINDOW) Bloom.Mustard else Bloom.Peach,
                Modifier.padding(top = 6.dp).popIn(300),
                height = 30.dp,
            )
        }
        Sheet {
            SectionTitle("Quanto saiu na xícara?", Modifier.enterCascade(1))
            EspressoQuantity(form, onChange)

            Row(
                Modifier
                    .fillMaxWidth()
                    .enterCascade(2)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Bloom.Mustard)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Bloom.Ink), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Tune, null, tint = Bloom.Mustard, modifier = Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(tip.title, style = BloomType.display(18.sp, -0.01f), color = Bloom.Ink)
                    Text(tip.body, style = BloomType.bodySmall.copy(fontSize = 14.sp), color = Bloom.Ink.copy(alpha = 0.85f), modifier = Modifier.padding(top = 3.dp))
                }
            }

            if (state.todayShots.isNotEmpty()) {
                SectionTitle("Calibragem de hoje")
                val shots = (state.todayShots.map { (it.shotSeconds ?: it.totalTimeSeconds.toDouble()) to it.yieldGrams } + (seconds to form.yieldGrams)).takeLast(3)
                val firstNumber = shotNumber - shots.size + 1
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    shots.forEachIndexed { i, (t, y) ->
                        ShotCard(firstNumber + i, t, y, shotSpeed(t, form.windowMin, form.windowMax), current = i == shots.lastIndex, modifier = Modifier.weight(1f).enterCascade(3 + i))
                    }
                }
            }

            SectionTitle("Café e moagem")
            PillField(form.coffee, { onChange(form.copy(coffee = it)) }, label = "Café", placeholder = "Qual café?")
            SuggestionRow(form.coffee, coffees) { onChange(form.copy(coffee = it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillField(form.grinder, { onChange(form.copy(grinder = it)) }, Modifier.weight(1.4f), label = "Moedor", placeholder = "Moedor")
                PillField(form.grindSetting, { onChange(form.copy(grindSetting = it)) }, Modifier.weight(1f), label = "Moagem", placeholder = "2,4")
            }
            SuggestionRow(form.grinder, grinders) { onChange(form.copy(grinder = it)) }

            SectionTitle("Como ficou?")
            BeanRating(form.rating, { onChange(form.copy(rating = it)) })

            ExpandableCard("Mais opções", "temperatura e notas") {
                PillField(form.waterTempText, { onChange(form.copy(waterTempText = it)) }, label = "Temperatura", placeholder = "93", suffix = "°C", keyboardType = KeyboardType.Number)
                PillField(form.notes, { onChange(form.copy(notes = it)) }, label = "Notas", placeholder = "Corpo, crema, doçura, o que mudar…", singleLine = false)
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Bloom.Sand)
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
            BigPillButton("Salvar shot", vm::save)
            TextAction("Descartar", onDiscard, Modifier.align(Alignment.CenterHorizontally), color = Bloom.Muted)
            Spacer(Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

@Composable
private fun ShotCard(number: Int, seconds: Double, yield: Double?, speed: ShotSpeed, current: Boolean, modifier: Modifier = Modifier) {
    val bg = if (current) Bloom.Ink else Bloom.White
    val fg = if (current) Bloom.Cream else Bloom.Ink
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(bg).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text("${number}º SHOT", style = BloomType.eyebrow.copy(fontSize = 10.sp), color = fg.copy(alpha = 0.7f))
        Text("${formatShotSeconds(seconds).substringBefore(',')} s", style = BloomType.display(22.sp, -0.02f), color = fg, modifier = Modifier.padding(top = 2.dp))
        Text(yield?.let(::formatGrams) ?: "—", style = BloomType.chip.copy(fontSize = 12.sp), color = fg)
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(8.dp).clip(CircleShape).background(
                    when (speed) {
                        ShotSpeed.IN_WINDOW -> Bloom.Mustard
                        ShotSpeed.FAST, ShotSpeed.SLOW -> Bloom.Peach
                        else -> Bloom.Terra
                    },
                ),
            )
            Spacer(Modifier.width(4.dp))
            Text(speed.label, style = BloomType.chip.copy(fontSize = 11.sp), color = fg, maxLines = 1)
        }
    }
}

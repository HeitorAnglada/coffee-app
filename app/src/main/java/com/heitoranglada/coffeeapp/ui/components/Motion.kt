package com.heitoranglada.coffeeapp.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Movimento do Bloombrew: tudo entra, responde ao toque e respira.
 * Quando o Android está com as animações desligadas, as animações contínuas ficam paradas
 * e as entradas acontecem sem movimento.
 */

/** true quando o usuário desligou as animações do sistema. */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/** Fase de 0 a 1 que se repete para sempre (ou fica parada em [still] com animações desligadas). */
@Composable
fun rememberLoop(durationMs: Int, reverse: Boolean = false, still: Float = 0f): State<Float> {
    val reduced = rememberReducedMotion()
    if (reduced) return remember { mutableFloatStateOf(still) }
    val transition = rememberInfiniteTransition(label = "loop")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = if (reverse) RepeatMode.Reverse else RepeatMode.Restart,
        ),
        label = "loop-phase",
    )
}

/** Toque "de borracha": encolhe ao apertar e volta com mola. Sem o ripple padrão do Material. */
fun Modifier.bouncyClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.93f,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 700f),
        label = "press",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            onClick = onClick,
        )
}

/** Entrada em cascata: sobe e aparece, um item depois do outro. */
fun Modifier.enterCascade(index: Int, distance: Dp = 30.dp): Modifier = composed {
    val reduced = rememberReducedMotion()
    val progress = remember { Animatable(if (reduced) 1f else 0f) }
    val distancePx = with(LocalDensity.current) { distance.toPx() }
    LaunchedEffect(Unit) {
        if (!reduced) {
            delay(index.coerceAtMost(8) * 70L)
            progress.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow))
        }
    }
    graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * distancePx
    }
}

/** "Pop": nasce pequeno e cresce com mola (gotas, carimbos, confirmações). */
fun Modifier.popIn(delayMs: Long = 0): Modifier = composed {
    val reduced = rememberReducedMotion()
    val scale = remember { Animatable(if (reduced) 1f else 0.3f) }
    LaunchedEffect(Unit) {
        if (!reduced) {
            delay(delayMs)
            launch { scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f)) }
        }
    }
    graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        alpha = ((scale.value - 0.3f) / 0.7f).coerceIn(0f, 1f)
    }
}

/** Balanço suave (formas dos métodos ao fundo dos cartões). */
fun Modifier.sway(degrees: Float = 4f, durationMs: Int = 5000, pivotY: Float = 0f): Modifier = composed {
    val phase by rememberLoop(durationMs, reverse = true, still = 0.5f)
    graphicsLayer {
        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, pivotY)
        rotationZ = -degrees + 2 * degrees * easeInOut(phase)
    }
}

/** Sobe e desce devagar. */
fun Modifier.bob(distance: Dp = 6.dp, durationMs: Int = 1600): Modifier = composed {
    val phase by rememberLoop(durationMs, reverse = true, still = 0f)
    val px = with(LocalDensity.current) { distance.toPx() }
    graphicsLayer { translationY = -px * easeInOut(phase) }
}

/** O cartão-adesivo do preparo: inclinado e "vivo". */
fun Modifier.wiggle(baseDegrees: Float = -3f, durationMs: Int = 2400): Modifier = composed {
    val phase by rememberLoop(durationMs, reverse = true, still = 0f)
    val px = with(LocalDensity.current) { 3.dp.toPx() }
    graphicsLayer {
        val e = easeInOut(phase)
        rotationZ = baseDegrees + 2f * e
        translationY = -px * e
    }
}

/** Pisca (indicador "gravando"). */
fun Modifier.blink(durationMs: Int = 1100): Modifier = composed {
    val phase by rememberLoop(durationMs, reverse = true, still = 0f)
    graphicsLayer { alpha = 1f - 0.75f * phase }
}

/** Faz o próximo item da lista se sobrepor a este (pilha de cartões). */
fun Modifier.overlapNext(overlap: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val height = (placeable.height - overlap.roundToPx()).coerceAtLeast(0)
    layout(placeable.width, height) { placeable.place(0, 0) }
}

fun easeInOut(t: Float): Float = if (t < 0.5f) 2 * t * t else 1 - (-2 * t + 2).let { it * it } / 2

/** Texto em que cada caractere que muda "rola" para cima, como um placar (timer, gramas). */
@Composable
fun RollingText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    val reduced = rememberReducedMotion()
    Row(modifier) {
        text.forEachIndexed { index, char ->
            key(index) {
                if (reduced) {
                    Text(char.toString(), style = style, color = color)
                } else {
                    AnimatedContent(
                        targetState = char,
                        transitionSpec = {
                            (slideInVertically(spring(dampingRatio = 0.75f, stiffness = 500f)) { it / 2 } + fadeIn(tween(160)))
                                .togetherWith(slideOutVertically(tween(180)) { -it / 2 } + fadeOut(tween(120)))
                                .using(SizeTransform(clip = false))
                        },
                        label = "digit",
                    ) { c -> Text(c.toString(), style = style, color = color) }
                }
            }
        }
    }
}

package com.heitoranglada.coffeeapp.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import kotlinx.coroutines.launch

/*
 * Animações do Bloombrew, desenhadas em Canvas no mesmo traço dos ícones.
 * Todas ficam paradas quando o sistema pede para reduzir/desligar animações.
 */

/** true quando o usuário desligou as animações do sistema (acessibilidade / opções do desenvolvedor). */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

private fun svgPath(d: String): Path = PathParser().parsePathString(d).toPath()

/** Desenha num "viewport" de 100x100 centralizado, como os SVGs do design. */
private inline fun DrawScope.viewport100(block: DrawScope.() -> Unit) {
    val s = size.minDimension / 100f
    translate((size.width - 100f * s) / 2f, (size.height - 100f * s) / 2f) {
        scale(s, s, pivot = Offset.Zero) { block() }
    }
}

private fun line(width: Float) = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)

/** Fase 0..1 que se repete em loop (ou 0 fixo se animações estiverem desligadas). */
@Composable
private fun loopPhase(durationMillis: Int, reduced: Boolean, label: String): State<Float> {
    val transition = rememberInfiniteTransition(label = label)
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing)),
        label = label,
    )
    return if (reduced) remember { androidx.compose.runtime.mutableFloatStateOf(0.5f) } else phase
}

/** Interpola v em [a, b] para 0..1 (com suavização). */
private fun ramp(v: Float, a: Float, b: Float): Float {
    val t = ((v - a) / (b - a)).coerceIn(0f, 1f)
    return t * t * (3 - 2 * t)
}

// ---------------------------------------------------------------------------------------------
// Timer

/**
 * Anel do timer: enche conforme [progress] (0..1) e mostra pontos nos despejos ([markers], 0..1).
 * O conteúdo (número do timer) fica no centro.
 */
@Composable
fun TimerRing(
    progress: Float,
    modifier: Modifier = Modifier,
    markers: List<Float> = emptyList(),
    trackColor: Color = Bloom.GreenSoft,
    progressColor: Color = Bloom.Paper,
    markerColor: Color = Bloom.TerracottaLight,
    strokeWidth: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(300, easing = LinearEasing), label = "ring")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val sw = strokeWidth.toPx()
            val radius = (size.minDimension - sw) / 2f
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(sw))
            if (animated > 0f) {
                drawArc(progressColor, -90f, 360f * animated, false, topLeft, arcSize, style = Stroke(sw, cap = StrokeCap.Round))
            }
            markers.forEach { m ->
                val angle = Math.toRadians((-90.0 + 360.0 * m.coerceIn(0f, 1f)))
                val p = Offset(center.x + radius * kotlin.math.cos(angle).toFloat(), center.y + radius * kotlin.math.sin(angle).toFloat())
                drawCircle(markerColor, radius = sw * 0.55f, center = p)
            }
        }
        content()
    }
}

// ---------------------------------------------------------------------------------------------
// Bloom

private val conePath by lazy { svgPath("M20 46 H80 L58 80 H42 Z") }
private val coneRibs by lazy { svgPath("M44 54 L48 72 M56 54 L52 72") }
private val coneBase by lazy { svgPath("M33 87 H67") }
private val domePath by lazy { svgPath("M28 46 Q50 18 72 46 Z") }

/** O café "florescendo" no coador: a cúpula incha e solta bolhas de CO₂. */
@Composable
fun BloomAnimation(modifier: Modifier = Modifier, lineColor: Color = Bloom.Green) {
    val reduced = rememberReducedMotion()
    val transition = rememberInfiniteTransition(label = "bloom")
    val domeAnim by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4000
                0.15f at 0
                1.05f at 1400
                1f at 2000
                1f at 3400
                0.15f at 4000
            },
        ),
        label = "dome",
    )
    val bubbles by loopPhase(2000, reduced, "bubbles")
    val dome = if (reduced) 1f else domeAnim

    Canvas(modifier) {
        viewport100 {
            withTransform({ scale(1f, dome, pivot = Offset(50f, 46f)) }) {
                drawPath(domePath, Bloom.Terracotta)
            }
            if (!reduced) {
                val spots = listOf(Triple(42f, 40f, 1.8f), Triple(53f, 36f, 1.4f), Triple(60f, 41f, 1.1f), Triple(47f, 34f, 1.2f))
                spots.forEachIndexed { i, (x, y, r) ->
                    val t = (bubbles + i * 0.27f) % 1f
                    val alpha = when {
                        t < 0.2f -> t / 0.2f
                        t > 0.8f -> (1f - t) / 0.2f
                        else -> 1f
                    } * dome.coerceIn(0f, 1f)
                    drawCircle(if (i == 3) Bloom.Terracotta else Bloom.Paper, r, Offset(x, y - 26f * t), alpha = alpha)
                }
            }
            drawPath(conePath, lineColor, style = line(3.2f))
            drawPath(coneRibs, lineColor, style = line(1.6f))
            drawPath(coneBase, lineColor, style = line(3.2f))
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Moedor

private val grinderBody by lazy { svgPath("M42 34 h16 a4 4 0 0 1 4 4 v42 a4 4 0 0 1 -4 4 h-16 a4 4 0 0 1 -4 -4 v-42 a4 4 0 0 1 4 -4 z") }
private val grinderSeam by lazy { svgPath("M38 62 h24") }
private val grinderKnurl by lazy { svgPath("M44 40 v16 M50 40 v16 M56 40 v16") }
private val grinderTop by lazy { svgPath("M35 34 h30") }
private val grinderAxle by lazy { svgPath("M50 34 V24") }

/**
 * Moedor manual visto de lado: a manivela dá a volta (no plano horizontal, por isso o braço
 * encurta e a manopla passa por trás) e o pó vai enchendo o copo de baixo.
 */
@Composable
fun GrinderAnimation(modifier: Modifier = Modifier, lineColor: Color = Bloom.Green) {
    val reduced = rememberReducedMotion()
    val spin by loopPhase(1600, reduced, "crank")
    val fall by loopPhase(900, reduced, "grounds")
    val fill by loopPhase(6000, reduced, "fill")
    Canvas(modifier) {
        viewport100 {
            val theta = spin * 2f * Math.PI.toFloat()
            val armX = 50f + 18f * kotlin.math.cos(theta)
            val knobInFront = kotlin.math.sin(theta) >= 0f
            val knobSize = androidx.compose.ui.geometry.Size(5f, 11f)
            val knobRadius = androidx.compose.ui.geometry.CornerRadius(2.5f)

            // Pó acumulado e caindo dentro do copo inferior.
            val level = 2f + 15f * fill
            drawRect(Bloom.Grounds, topLeft = Offset(41f, 82f - level), size = androidx.compose.ui.geometry.Size(18f, level))
            if (!reduced) {
                listOf(46f, 50f, 54f).forEachIndexed { i, x ->
                    val t = (fall + i / 3f) % 1f
                    val alpha = if (t < 0.2f) t / 0.2f else 1f - t
                    drawCircle(Bloom.Grounds, if (i == 1) 1f else 1.2f, Offset(x, 64f + 14f * t), alpha = alpha)
                }
            }
            drawPath(grinderBody, lineColor, style = line(3f))
            drawPath(grinderSeam, lineColor, style = line(2.4f))
            drawPath(grinderKnurl, lineColor, style = line(1.4f))
            drawPath(grinderTop, lineColor, style = line(3.4f))
            if (!knobInFront) {
                drawRoundRect(Bloom.Terracotta, Offset(armX - 2.5f, 13f), knobSize, knobRadius, alpha = 0.5f)
            }
            drawPath(grinderAxle, lineColor, style = line(3f))
            drawLine(lineColor, Offset(50f, 24f), Offset(armX, 24f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawCircle(lineColor, 2.4f, Offset(50f, 24f))
            if (knobInFront) {
                drawRoundRect(Bloom.Terracotta, Offset(armX - 2.5f, 13f), knobSize, knobRadius)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Despejo

private val kettleBody by lazy { svgPath("M14 22 h20 v14 a4 4 0 0 1 -4 4 h-12 a4 4 0 0 1 -4 -4 z") }
private val kettleSpout by lazy { svgPath("M34 32 C42 32 44 20 52 16") }
private val kettleHandle by lazy { svgPath("M14 26 h-3 a3 3 0 0 0 0 6 h3") }
private val dripper by lazy { svgPath("M40 52 H72 L62 68 H50 Z") }
private val jar by lazy { svgPath("M45 72 v18 a2 2 0 0 0 2 2 h18 a2 2 0 0 0 2 -2 v-18") }

/** Chaleira inclinando e despejando no coador enquanto a jarra enche. */
@Composable
fun PourAnimation(modifier: Modifier = Modifier, lineColor: Color = Bloom.Green) {
    val reduced = rememberReducedMotion()
    val phase by loopPhase(5000, reduced, "pour")
    val tilt = if (reduced) 1f else ramp(phase, 0.08f, 0.22f) * (1f - ramp(phase, 0.72f, 0.86f))
    val stream = if (reduced) 1f else ramp(phase, 0.2f, 0.28f) * (1f - ramp(phase, 0.7f, 0.78f))
    val fill = if (reduced) 0.6f else 0.05f + 0.95f * ramp(phase, 0.26f, 0.74f)
    Canvas(modifier) {
        viewport100 {
            rotate(24f * tilt, pivot = Offset(30f, 30f)) {
                drawPath(kettleBody, lineColor, style = line(2.6f))
                drawPath(kettleSpout, lineColor, style = line(2.6f))
                drawPath(kettleHandle, lineColor, style = line(2.6f))
            }
            if (stream > 0f) {
                drawRoundRect(
                    Bloom.Water,
                    topLeft = Offset(55f, 26f),
                    size = androidx.compose.ui.geometry.Size(2.4f, 26f * stream),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.2f),
                )
            }
            drawPath(dripper, lineColor, style = line(2.6f))
            val h = 16f * fill
            drawRect(Bloom.Terracotta, topLeft = Offset(47f, 90f - h), size = androidx.compose.ui.geometry.Size(18f, h))
            drawPath(jar, lineColor, style = line(2.6f))
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Prensa francesa

private val pressBody by lazy { svgPath("M32 30 h34 v58 h-34 z") }
private val pressHandle by lazy { svgPath("M66 42 h5 a3 3 0 0 1 3 3 v20 a3 3 0 0 1 -3 3 h-5") }
private val plunger by lazy { svgPath("M49 10 V40 M43 10 h12 M34 40 h30") }

/** Êmbolo da prensa descendo devagar: hora de pressionar. */
@Composable
fun FrenchPressAnimation(modifier: Modifier = Modifier, lineColor: Color = Bloom.Green) {
    val reduced = rememberReducedMotion()
    val phase by loopPhase(4000, reduced, "press")
    val down = if (reduced) 0.5f else ramp(phase, 0.15f, 0.65f) * (1f - ramp(phase, 0.85f, 1f))
    Canvas(modifier) {
        viewport100 {
            drawRect(Bloom.Terracotta.copy(alpha = 0.25f), topLeft = Offset(34f, 40f), size = androidx.compose.ui.geometry.Size(30f, 46f))
            // O filtro desce e para em cima do pó, que é compactado (nunca o atravessa).
            val groundsH = 20f * (1f - 0.5f * down)
            drawRect(Bloom.Grounds, topLeft = Offset(34f, 86f - groundsH), size = androidx.compose.ui.geometry.Size(30f, groundsH))
            translate(0f, 34f * down) { drawPath(plunger, lineColor, style = line(3.2f)) }
            drawPath(pressBody, lineColor, style = line(3f))
            drawPath(pressHandle, lineColor, style = line(3f))
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Botão "Novo despejo"

/**
 * Botão que afunda ao tocar e solta uma onda, para confirmar o toque sem olhar a tela.
 */
@Composable
fun PulseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Bloom.Terracotta,
    contentColor: Color = Bloom.Night,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    content: @Composable RowScope.() -> Unit,
) {
    val reduced = rememberReducedMotion()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed && !reduced) 0.94f else 1f, label = "press")
    val ripple = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(modifier, contentAlignment = Alignment.Center) {
        // Onda que se expande atrás do botão.
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    val t = ripple.value
                    scaleX = 1f + 0.25f * t
                    scaleY = 1f + 0.5f * t
                    alpha = 0.5f * (1f - t)
                }
                .clip(shape)
                .drawBehind { drawRect(containerColor) },
        )
        Button(
            onClick = {
                if (!reduced) scope.launch {
                    ripple.snapTo(0f)
                    ripple.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
                }
                onClick()
            },
            interactionSource = interaction,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = pressScale; scaleY = pressScale },
            content = content,
        )
    }
}

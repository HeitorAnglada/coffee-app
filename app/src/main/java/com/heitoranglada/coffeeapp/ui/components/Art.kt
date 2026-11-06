package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heitoranglada.coffeeapp.domain.BrewMethod
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * Formas próprias do Bloombrew: o "bloom" orgânico, as silhuetas dos métodos,
 * as gotas dos despejos e os grãos da nota.
 */

private fun svgPath(d: String): Path = PathParser().parsePathString(d).toPath()

private val v60Path = svgPath("M8 22 H92 L60 78 H40 Z M30 90 H70 V96 H30 Z")
private val melittaPath = svgPath("M12 20 H88 L72 80 H28 Z M24 90 H76 V96 H24 Z")
private val pressPath = svgPath(
    "M24 30 H76 V96 H24 Z M46 4 H54 V30 H46 Z M36 2 H64 V9 H36 Z " +
        "M78 44 Q94 44 94 60 Q94 76 78 76 V68 Q86 68 86 60 Q86 52 78 52 Z",
)
private val aeropressPath = svgPath("M24 4 H76 V11 H24 Z M35 11 H65 V34 H35 Z M30 30 H70 V86 H30 Z M24 86 H76 V93 H24 Z")
private val espressoPath = svgPath(
    "M22 14 H66 V28 H22 Z M66 18 H96 V24 H66 Z M40 28 H48 V38 H40 Z M24 52 H64 V72 Q64 88 44 88 Q24 88 24 72 Z " +
        "M64 58 Q78 58 78 66 Q78 74 64 74 V69 Q72 69 72 66 Q72 63 64 63 Z M12 92 H76 V97 H12 Z",
)
private val dropPath = svgPath("M12 2s9 10 9 17a9 9 0 0 1-18 0c0-7 9-17 9-17z")

fun BrewMethod.silhouette(): Path = when (this) {
    BrewMethod.V60 -> v60Path
    BrewMethod.MELITTA -> melittaPath
    BrewMethod.FRENCH_PRESS -> pressPath
    BrewMethod.AEROPRESS -> aeropressPath
    BrewMethod.ESPRESSO -> espressoPath
}

/** Desenha num espaço de [w]x[h] unidades, centralizado e mantendo a proporção. */
private inline fun DrawScope.viewport(w: Float, h: Float, block: DrawScope.() -> Unit) {
    val s = minOf(size.width / w, size.height / h)
    translate((size.width - w * s) / 2f, (size.height - h * s) / 2f) {
        scale(s, s, pivot = Offset.Zero) { block() }
    }
}

/** Silhueta do método (coador V60, Melitta ou prensa). */
@Composable
fun MethodSilhouette(method: BrewMethod, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { viewport(100f, 100f) { drawPath(method.silhouette(), color) } }
}

/**
 * Contorno do "bloom": um círculo que respira e se deforma devagar, como o café inchando
 * no primeiro despejo. [phase] vai de 0 a 1 e o desenho fecha o ciclo sem saltos.
 */
fun blobPath(size: Size, phase: Float, wobble: Float = 0.07f, seed: Int = 0): Path {
    val n = 8
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rx = size.width / 2f
    val ry = size.height / 2f
    val t = phase * 2f * PI.toFloat()
    val pts = List(n) { i ->
        val a = i * 2f * PI.toFloat() / n
        val k = 1f - wobble + wobble * (
            0.55f * sin(t + i * 1.7f + seed * 0.9f) +
                0.45f * sin(2f * t + i * 2.3f + seed * 1.3f)
            )
        Offset(cx + cos(a) * rx * k, cy + sin(a) * ry * k)
    }
    // Catmull-Rom fechado convertido em Béziers: curva lisa passando por todos os pontos.
    val path = Path()
    path.moveTo(pts[0].x, pts[0].y)
    for (i in 0 until n) {
        val p0 = pts[(i - 1 + n) % n]
        val p1 = pts[i]
        val p2 = pts[(i + 1) % n]
        val p3 = pts[(i + 2) % n]
        val c1 = p1 + (p2 - p0) / 6f
        val c2 = p2 - (p3 - p1) / 6f
        path.cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
    }
    path.close()
    return path
}

/** O bloom desenhado atrás do conteúdo, respirando sozinho. */
fun Modifier.blobBackground(color: () -> Color, phase: () -> Float, wobble: Float = 0.07f, seed: Int = 0): Modifier =
    drawBehind { drawPath(blobPath(size, phase(), wobble, seed), color()) }

/** Bloom com conteúdo centralizado. */
@Composable
fun Blob(
    color: Color,
    modifier: Modifier = Modifier,
    periodMs: Int = 6000,
    wobble: Float = 0.07f,
    seed: Int = 0,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val phase by rememberLoop(periodMs, still = 0.15f)
    Box(
        modifier.blobBackground({ color }, { phase }, wobble, seed),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** Gota de um despejo, com o número dentro. [dashed] desenha só o contorno (o "próximo"). */
@Composable
fun Drop(
    number: String?,
    fill: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    width: Dp = 46.dp,
    dashed: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
) {
    Box(modifier.size(width, width * 1.22f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(width, width * 1.22f)) {
            viewport(24f, 30f) {
                if (dashed) {
                    drawPath(
                        dropPath,
                        fill,
                        style = Stroke(width = 1.6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))),
                    )
                } else {
                    drawPath(dropPath, fill)
                }
            }
        }
        if (number != null) {
            Text(
                number,
                color = textColor,
                style = textStyle,
                modifier = Modifier.padding(top = width * 0.28f),
            )
        }
    }
}

/** Grão de café (usado na nota e como enfeite). */
@Composable
fun BeanIcon(fill: Color, crease: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.minDimension
        rotate(30f) {
            drawOval(
                color = fill,
                topLeft = Offset(center.x - w * 0.27f, center.y - w * 0.375f),
                size = Size(w * 0.54f, w * 0.75f),
            )
            val crack = Path().apply {
                moveTo(center.x - w * 0.06f, center.y - w * 0.3f)
                cubicTo(
                    center.x + w * 0.14f, center.y - w * 0.12f,
                    center.x - w * 0.14f, center.y + w * 0.12f,
                    center.x + w * 0.06f, center.y + w * 0.3f,
                )
            }
            drawPath(crack, crease, style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))
        }
    }
}

/** Anéis tracejados que giram devagar ao redor do timer. */
fun DrawScope.dashedRing(radius: Float, color: Color, width: Float, rotation: Float) {
    rotate(rotation) {
        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = width, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(width * 2f, width * 4.5f))),
        )
    }
}

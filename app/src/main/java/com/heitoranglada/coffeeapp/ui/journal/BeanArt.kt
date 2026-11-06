package com.heitoranglada.coffeeapp.ui.journal

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.CoffeeApplication
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType

/** Cores do "pacote" desenhado de cada grão (fundo e etiqueta), escolhidas pelo nome. */
private val bagBackgrounds = listOf(Color(0xFFE9C9A0), Color(0xFFB9CFC2), Color(0xFFF2B9A0), Color(0xFFE6D5A8), Color(0xFFCFC0D8))
private val bagLabels = listOf(Bloom.Mustard, Bloom.Peach, Bloom.Cream, Bloom.Terra, Bloom.Sand)
private val Kraft = Color(0xFFC69A6B)
private val KraftDark = Color(0xFFB2865A)

private fun bagIndex(name: String): Int = (name.lowercase().hashCode() and 0x7fffffff) % bagBackgrounds.size

fun beanTint(name: String): Color = bagBackgrounds[bagIndex(name)]

/**
 * Foto do grão: a primeira foto do pacote ou, sem foto, um pacote desenhado com o nome.
 * [photo] escolhe outra foto da lista.
 */
@Composable
fun BeanPicture(
    bean: Bean,
    modifier: Modifier = Modifier,
    corner: Dp = 20.dp,
    photo: String? = bean.photos.firstOrNull(),
    showLabel: Boolean = true,
) {
    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(corner))
            .semantics { contentDescription = "Foto de ${bean.name}" },
    ) {
        val px = with(LocalDensity.current) { maxOf(maxWidth, maxHeight).toPx() }.toInt().coerceIn(64, 1600)
        val bitmap = rememberPhoto(photo, px)
        Crossfade(bitmap, label = "bean-photo") { img ->
            if (img != null) {
                Image(img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                CoffeeBag(bean.name, if (showLabel) bean.variety.ifBlank { bean.process } else "", showLabel, Modifier.fillMaxSize())
            }
        }
    }
}

/** Carrega a foto em segundo plano (null enquanto carrega ou se não houver). */
@Composable
fun rememberPhoto(name: String?, targetPx: Int): ImageBitmap? {
    val app = LocalContext.current.applicationContext as? CoffeeApplication
    val bitmap by produceState<ImageBitmap?>(null, name, targetPx) {
        value = if (name == null || app == null) null else app.journal.photos.load(name, targetPx)?.asImageBitmap()
    }
    return bitmap
}

/** Pacote de café desenhado, no lugar da foto. */
@Composable
fun CoffeeBag(name: String, subtitle: String, showLabel: Boolean, modifier: Modifier = Modifier) {
    val i = bagIndex(name)
    val bg = bagBackgrounds[i]
    val label = bagLabels[i]
    BoxWithConstraints(modifier.background(bg)) {
        val h = maxHeight
        Canvas(Modifier.fillMaxSize()) {
            // Listras diagonais suaves, como papel.
            val stripe = Bloom.Ink.copy(alpha = 0.04f)
            val step = 14.dp.toPx()
            var x = -size.height
            while (x < size.width) {
                drawLine(stripe, Offset(x, size.height), Offset(x + size.height * 0.47f, 0f), strokeWidth = 6.dp.toPx())
                x += step
            }
            val bw = size.width * 0.58f
            val bh = size.height * 0.86f
            val left = (size.width - bw) / 2f
            val top = size.height - bh + size.height * 0.08f
            clipRect {
                drawRoundRect(Kraft, Offset(left, top), Size(bw, bh), CornerRadius(10.dp.toPx()))
                drawRoundRect(KraftDark, Offset(left, top), Size(bw, bh * 0.12f), CornerRadius(10.dp.toPx()))
                drawRect(KraftDark, Offset(left, top + bh * 0.06f), Size(bw, bh * 0.06f))
                drawRoundRect(
                    label,
                    Offset(left + bw * 0.12f, top + bh * 0.28f),
                    Size(bw * 0.76f, bh * 0.42f),
                    CornerRadius(6.dp.toPx()),
                )
            }
        }
        if (showLabel && h > 60.dp) {
            // A etiqueta ocupa de 46% a 82% da altura e 44% da largura, no centro.
            androidx.compose.foundation.layout.Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = h * 0.47f)
                    .size(width = maxWidth * 0.42f, height = h * 0.34f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                Text(
                    name,
                    style = BloomType.display((h.value / 13f).coerceIn(9f, 28f).sp, -0.01f),
                    color = Bloom.Ink,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = BloomType.label.copy(fontSize = (h.value / 26f).coerceIn(7f, 15f).sp),
                        color = Bloom.Ink.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

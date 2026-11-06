package com.heitoranglada.coffeeapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.R
import com.heitoranglada.coffeeapp.domain.BrewMethod

/**
 * Identidade "Cores do método": cada método tem a sua cor
 * (V60 terracota, Melitta mostarda, prensa verde) sobre um fundo creme e tinta café.
 */
object Bloom {
    val Cream = Color(0xFFFFF6EA)
    val Ink = Color(0xFF2A1408)
    val Terra = Color(0xFFE2653A)
    val Mustard = Color(0xFFF0B73F)
    val Green = Color(0xFF2F4A40)
    val Sand = Color(0xFFF3E4CF)
    val Line = Color(0xFFEADBC6)
    val Muted = Color(0xFF7A5A45)
    val Body = Color(0xFF5C4535)
    val Peach = Color(0xFFF7A27B)
    val Bean = Color(0xFFD9C3A6)
    val White = Color(0xFFFFFFFF)
}

/** Cores de um método: fundo, conteúdo, fundo das pílulas e destaque. */
data class MethodPalette(val bg: Color, val fg: Color, val chip: Color, val accent: Color)

/** Fundo escuro (prensa, espresso): o que costuma ser tinta vira creme. */
val MethodPalette.isDark: Boolean get() = fg == Bloom.Cream

val BrewMethod.palette: MethodPalette
    get() = when (this) {
        BrewMethod.V60 -> MethodPalette(Bloom.Terra, Bloom.Ink, Bloom.Cream.copy(alpha = 0.35f), Bloom.Mustard)
        BrewMethod.MELITTA -> MethodPalette(Bloom.Mustard, Bloom.Ink, Bloom.Cream.copy(alpha = 0.45f), Bloom.Terra)
        BrewMethod.FRENCH_PRESS -> MethodPalette(Bloom.Green, Bloom.Cream, Bloom.Cream.copy(alpha = 0.16f), Bloom.Mustard)
        BrewMethod.AEROPRESS -> MethodPalette(Bloom.Peach, Bloom.Ink, Bloom.Cream.copy(alpha = 0.35f), Bloom.Terra)
        BrewMethod.ESPRESSO -> MethodPalette(Bloom.Ink, Bloom.Cream, Bloom.Cream.copy(alpha = 0.14f), Bloom.Mustard)
    }

val Bricolage = FontFamily(
    Font(R.font.bricolage_bold, FontWeight.Bold),
    Font(R.font.bricolage_extrabold, FontWeight.ExtraBold),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

/** Estilos de texto do Bloombrew. */
object BloomType {
    /** Títulos grandes em Bricolage, com espaçamento negativo (como no design). */
    fun display(size: TextUnit, tracking: Float = -0.03f) = TextStyle(
        fontFamily = Bricolage,
        fontWeight = FontWeight.ExtraBold,
        fontSize = size,
        letterSpacing = tracking.em,
        lineHeight = size * 0.98f,
        fontFeatureSettings = "tnum",
    )

    val eyebrow = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.2.sp)
    val label = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    val body = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp)
    val bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp)
    val chip = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    val button = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
}

private val Colors = lightColorScheme(
    primary = Bloom.Ink,
    onPrimary = Bloom.Cream,
    primaryContainer = Bloom.Sand,
    onPrimaryContainer = Bloom.Ink,
    secondary = Bloom.Terra,
    onSecondary = Bloom.Ink,
    tertiary = Bloom.Mustard,
    onTertiary = Bloom.Ink,
    background = Bloom.Cream,
    onBackground = Bloom.Ink,
    surface = Bloom.Cream,
    onSurface = Bloom.Ink,
    onSurfaceVariant = Bloom.Muted,
    surfaceContainer = Bloom.White,
    surfaceContainerHigh = Bloom.Cream,
    surfaceContainerHighest = Bloom.Sand,
    outline = Bloom.Muted,
    outlineVariant = Bloom.Line,
    error = Color(0xFFB3261E),
)

private val base = Typography()

private val Type = Typography(
    headlineSmall = base.headlineSmall.copy(fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold),
    titleLarge = base.titleLarge.copy(fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold),
    titleMedium = base.titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Inter),
    bodyMedium = base.bodyMedium.copy(fontFamily = Inter),
    bodySmall = base.bodySmall.copy(fontFamily = Inter),
    labelLarge = base.labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
)

/** O app usa sempre o tema claro: as cores de cada método são a identidade. */
@Composable
fun CoffeeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        typography = Type,
        shapes = Shapes(extraLarge = RoundedCornerShape(28.dp)),
        content = content,
    )
}

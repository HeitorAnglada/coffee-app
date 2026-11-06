package com.heitoranglada.coffeeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.R

/** Paleta Bloombrew (derivada da logo: coador verde + bloom terracota). */
object Bloom {
    val Green = Color(0xFF2F4A40)      // verde coador: primária
    val Night = Color(0xFF1E3029)      // verde noite: textos
    val Terracotta = Color(0xFFC8763E) // destaque, bloom
    val TerracottaLight = Color(0xFFE08A4F)
    val Paper = Color(0xFFF4F1EA)      // fundo
    val Sand = Color(0xFFE6DFD2)       // cartões, linhas
    val Moss = Color(0xFF4A5A53)       // texto secundário
    val GreenSoft = Color(0xFF48655A)
    val Water = Color(0xFF7FB0C9)
    val Grounds = Color(0xFF8A5A3B)
}

val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

private val LightColors = lightColorScheme(
    primary = Bloom.Green,
    onPrimary = Bloom.Paper,
    primaryContainer = Color(0xFFD7E3DC),
    onPrimaryContainer = Bloom.Night,
    secondary = Bloom.Terracotta,
    onSecondary = Bloom.Night,
    secondaryContainer = Color(0xFFF6DCC8),
    onSecondaryContainer = Bloom.Night,
    tertiary = Bloom.Terracotta,
    onTertiary = Bloom.Night,
    background = Bloom.Paper,
    onBackground = Bloom.Night,
    surface = Bloom.Paper,
    onSurface = Bloom.Night,
    onSurfaceVariant = Bloom.Moss,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAF8F3),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Bloom.Sand,
    surfaceContainerHighest = Color(0xFFDDD4C5),
    outline = Color(0xFF8D9993),
    outlineVariant = Color(0xFFB9C3BD),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC2B0),
    onPrimary = Bloom.Night,
    primaryContainer = Bloom.GreenSoft,
    onPrimaryContainer = Bloom.Paper,
    secondary = Bloom.TerracottaLight,
    onSecondary = Bloom.Night,
    tertiary = Bloom.TerracottaLight,
    onTertiary = Bloom.Night,
    background = Color(0xFF141F1B),
    surface = Color(0xFF141F1B),
    surfaceContainer = Color(0xFF1E2B26),
    surfaceContainerHigh = Color(0xFF283731),
)

private val base = Typography()

private val BloomTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium),
    titleSmall = base.titleSmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Inter),
    bodyMedium = base.bodyMedium.copy(fontFamily = Inter),
    bodySmall = base.bodySmall.copy(fontFamily = Inter),
    labelLarge = base.labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
)

/** Estilo dos números grandes do timer. */
val TimerTextStyle = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 64.sp, letterSpacing = (-2).sp)

private val BloomShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun CoffeeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = BloomTypography,
        shapes = BloomShapes,
        content = content,
    )
}

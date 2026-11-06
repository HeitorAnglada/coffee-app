package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import com.heitoranglada.coffeeapp.ui.theme.palette

/**
 * Cabeçalho na cor do método, com a silhueta balançando ao fundo.
 * Com [scroll], a silhueta anda mais devagar que a página (paralaxe).
 */
@Composable
fun MethodHero(
    method: BrewMethod,
    modifier: Modifier = Modifier,
    scroll: ScrollState? = null,
    background: Color = method.palette.bg,
    bottomPadding: Dp = 56.dp,
    topBar: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val p = method.palette
    Box(
        modifier
            .fillMaxWidth()
            .background(background)
            .clipToBounds(),
    ) {
        MethodSilhouette(
            method,
            p.fg.copy(alpha = 0.16f),
            Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = 40.dp)
                .graphicsLayer { translationY = (scroll?.value ?: 0) * 0.45f }
                .sway(5f, 5200),
        )
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = bottomPadding),
        ) {
            Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically, content = topBar)
            content()
        }
    }
}

/** Folha creme que sobe por cima do cabeçalho colorido. */
@Composable
fun Sheet(modifier: Modifier = Modifier, overlap: Dp = 32.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .layout { measurable, constraints ->
                val px = overlap.roundToPx()
                val placeable = measurable.measure(constraints)
                layout(placeable.width, (placeable.height - px).coerceAtLeast(0)) { placeable.place(0, -px) }
            }
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Bloom.Cream)
            .padding(start = 20.dp, end = 20.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content,
    )
}

@Composable
fun BloomDialog(
    title: String,
    text: String,
    confirm: String,
    onConfirm: () -> Unit,
    dismiss: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Bloom.Cream,
        shape = RoundedCornerShape(30.dp),
        title = { Text(title, style = BloomType.display(24.sp, -0.02f), color = Bloom.Ink) },
        text = { Text(text, style = BloomType.body, color = Bloom.Body) },
        confirmButton = {
            Box(
                Modifier
                    .bouncyClick(onClick = onConfirm)
                    .clip(RoundedCornerShape(50))
                    .background(Bloom.Terra)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) { Text(confirm, style = BloomType.chip.copy(fontSize = 15.sp), color = Bloom.Ink) }
        },
        dismissButton = {
            Box(
                Modifier
                    .bouncyClick(onClick = onDismiss)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) { Text(dismiss, style = BloomType.chip.copy(fontSize = 15.sp), color = Bloom.Ink) }
        },
    )
}

/** Linha "rótulo · valor" das telas de detalhe. */
@Composable
fun InfoLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = BloomType.body, color = Bloom.Muted, modifier = Modifier.weight(1f))
        Text(value, style = BloomType.body.copy(fontSize = 15.sp), color = Bloom.Ink)
    }
}

@Composable
fun BottomSpace() {
    Spacer(Modifier.height(24.dp))
}

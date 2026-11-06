package com.heitoranglada.coffeeapp.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.data.journal.JournalRepository
import com.heitoranglada.coffeeapp.ui.components.MiniBeans
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType

/** Origem e processo numa linha ("Sul de Minas · natural"). */
fun Bean.originLine(): String =
    listOf(origin, process.lowercase()).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { roaster }

/** Grade de grãos em duas colunas, dentro de uma LazyColumn. O último quadro cria um grão novo. */
fun LazyListScope.beanGrid(cards: List<BeanCard>, onOpen: (Long) -> Unit, onNew: () -> Unit) {
    val tiles: List<BeanCard?> = cards + listOf(null)
    val rows = tiles.chunked(2)
    itemsIndexed(rows, key = { _, row -> row.joinToString { it?.bean?.id?.toString() ?: "novo" } }) { index, row ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .enterCascade(index + 3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { card ->
                if (card != null) {
                    BeanTile(card, Modifier.weight(1f)) { onOpen(card.bean.id) }
                } else {
                    NewBeanTile(Modifier.weight(1f).height(if (cards.isEmpty()) 200.dp else 250.dp), onNew)
                }
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun BeanTile(card: BeanCard, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bean = card.bean
    Column(
        modifier
            .bouncyClick(pressedScale = 0.96f, onClickLabel = "Abrir ${bean.name}", onClick = onClick)
            .clip(RoundedCornerShape(26.dp))
            .background(Bloom.White)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
    ) {
        BeanPicture(bean, Modifier.fillMaxWidth().height(132.dp))
        Column(Modifier.padding(start = 6.dp, end = 6.dp, top = 10.dp)) {
            Text(bean.name, style = BloomType.display(18.sp, -0.02f), color = Bloom.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val origin = bean.originLine()
            if (origin.isNotBlank()) {
                Text(origin, style = BloomType.bodySmall.copy(fontSize = 12.sp), color = Bloom.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (card.averageRating != null) {
                    MiniBeans(card.averageRating)
                } else {
                    Text(
                        if (card.brewCount == 0) "ainda não preparado" else "${card.brewCount} preparo${if (card.brewCount == 1) "" else "s"}",
                        style = BloomType.label.copy(fontSize = 11.sp),
                        color = Bloom.Muted,
                    )
                }
                Spacer(Modifier.weight(1f))
                JournalRepository.roastAgeLabel(bean.roastDate)?.let {
                    Text(it, style = BloomType.label.copy(fontSize = 11.sp), color = Bloom.Muted)
                }
            }
            if (card.nextTime != null) {
                Row(
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Bloom.Mustard)
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.StickyNote2, null, tint = Bloom.Ink, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                    Text(card.nextTime, style = BloomType.label.copy(fontSize = 11.sp, lineHeight = 14.sp), color = Bloom.Ink, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun NewBeanTile(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .bouncyClick(pressedScale = 0.95f, onClickLabel = "Cadastrar grão", onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    Bloom.Muted,
                    cornerRadius = CornerRadius(26.dp.toPx()),
                    style = Stroke(width = 2.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                )
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(Bloom.Terra), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.PhotoCamera, null, tint = Bloom.Ink, modifier = Modifier.size(26.dp))
        }
        Text("Novo grão", style = BloomType.display(18.sp, -0.01f), color = Bloom.Ink, modifier = Modifier.padding(top = 10.dp))
        Text("foto do pacote, lote…", style = BloomType.bodySmall.copy(fontSize = 12.sp), color = Bloom.Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
    }
}

/** Sem nenhum grão ainda: convite para cadastrar o primeiro. */
@Composable
fun EmptyBeans(onNew: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Fotografe o pacote, anote lote e torra. Depois de cada preparo, conte o que gostou e o que mudaria: eu te lembro na próxima xícara desse café.",
            style = BloomType.body,
            color = Bloom.Body,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        NewBeanTile(Modifier.fillMaxWidth().height(200.dp), onNew)
    }
}

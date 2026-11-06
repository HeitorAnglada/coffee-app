package com.heitoranglada.coffeeapp.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.data.journal.BeanReminder
import com.heitoranglada.coffeeapp.ui.components.CircleIconButton
import com.heitoranglada.coffeeapp.ui.components.Eyebrow
import com.heitoranglada.coffeeapp.ui.components.MiniBeans
import com.heitoranglada.coffeeapp.ui.components.PillField
import com.heitoranglada.coffeeapp.ui.components.SectionTitle
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.popIn
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val shortDate = SimpleDateFormat("d MMM", Locale.forLanguageTag("pt-BR"))

/** "3 nov." -> "3 nov". */
fun shortDay(millis: Long): String = shortDate.format(Date(millis)).trimEnd('.')

/** O texto principal do lembrete: o que mudar; se não houver, o que deu certo. */
val BeanReminder.headline: String get() = note.change.ifBlank { note.liked }

/**
 * Lembrete grande, que aparece antes de começar uma receita com aquele café
 * (cartão creme com a foto do grão, sobre a cor do método).
 */
@Composable
fun ReminderCard(reminder: BeanReminder, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val n = reminder.note
    Box(
        modifier
            .fillMaxWidth()
            .popIn(350)
            .clip(RoundedCornerShape(26.dp))
            .background(Bloom.Cream)
            .padding(16.dp)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val bean = reminder.bean
            if (bean != null) {
                BeanPicture(bean, Modifier.size(width = 58.dp, height = 72.dp), corner = 16.dp, showLabel = false)
            } else {
                Box(Modifier.size(46.dp).clip(CircleShape).background(Bloom.Mustard), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Outlined.StickyNote2, null, tint = Bloom.Ink, modifier = Modifier.size(22.dp))
                }
            }
            Column(Modifier.weight(1f).padding(end = 30.dp)) {
                Eyebrow("Da última vez", color = Bloom.Muted)
                Text(reminder.headline, style = BloomType.display(19.sp, -0.01f).copy(lineHeight = 22.sp), color = Bloom.Ink, modifier = Modifier.padding(top = 4.dp))
                if (n.change.isNotBlank() && n.liked.isNotBlank()) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Bloom.Ink)) { append("Gostou: ") }
                            append(n.liked)
                        },
                        style = BloomType.bodySmall,
                        color = Bloom.Body,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (n.rating != null) MiniBeans(n.rating)
                    Text(
                        listOf(n.recipeName ?: n.method.label, shortDay(n.createdAt), n.grindSetting).filter { it.isNotBlank() }.joinToString(" · "),
                        style = BloomType.label,
                        color = Bloom.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        CircleIconButton(Icons.Filled.Close, "Dispensar lembrete", onDismiss, Modifier.align(Alignment.TopEnd), size = 32.dp)
    }
}

/** Lembrete compacto (preparo livre, sobre o creme). */
@Composable
fun ReminderStrip(reminder: BeanReminder, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Bloom.Mustard)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(Bloom.Cream), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Outlined.StickyNote2, null, tint = Bloom.Ink, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Eyebrow("Da última vez · ${shortDay(reminder.note.createdAt)}", color = Bloom.Ink.copy(alpha = 0.8f))
            Text(reminder.headline, style = BloomType.display(17.sp, -0.01f).copy(lineHeight = 20.sp), color = Bloom.Ink, modifier = Modifier.padding(top = 2.dp), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Escolha do café em pílulas com a foto (preparo livre). Tocar de novo no escolhido tira a escolha. */
@Composable
fun BeanPills(beans: List<Bean>, selectedId: Long?, onSelect: (Long?) -> Unit, modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues()) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = contentPadding) {
        items(beans, key = { it.id }) { bean ->
            val on = bean.id == selectedId
            val bg by animateColorAsState(if (on) Bloom.Ink else Bloom.Cream, label = "bean-pill-bg")
            val fg by animateColorAsState(if (on) Bloom.Cream else Bloom.Ink, label = "bean-pill-fg")
            Row(
                Modifier
                    .height(52.dp)
                    .bouncyClick(onClickLabel = bean.name) { onSelect(if (on) null else bean.id) }
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .border(2.dp, Bloom.Ink, RoundedCornerShape(50))
                    .padding(start = 5.dp, end = 14.dp)
                    .semantics {
                        role = Role.RadioButton
                        selected = on
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BeanPicture(bean, Modifier.size(40.dp), corner = 20.dp, showLabel = false)
                Text(bean.name, style = BloomType.chip.copy(fontSize = 14.sp), color = fg, maxLines = 1)
            }
        }
    }
}

/** Escolha do café em quadros com a foto (ao salvar o preparo). O último cadastra um grão novo. */
@Composable
fun BeanTiles(beans: List<Bean>, selectedId: Long?, onSelect: (Bean?) -> Unit, onNew: () -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(beans, key = { it.id }) { bean ->
            val on = bean.id == selectedId
            val ring by animateDpAsState(if (on) 3.dp else 0.dp, spring(dampingRatio = 0.5f), label = "bean-ring")
            Column(
                Modifier
                    .width(78.dp)
                    .bouncyClick(onClickLabel = bean.name) { onSelect(if (on) null else bean) }
                    .semantics {
                        role = Role.RadioButton
                        selected = on
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 78.dp, height = 90.dp)
                        .border(ring.coerceAtLeast(0.dp), Bloom.Ink, RoundedCornerShape(22.dp))
                        .padding(5.dp),
                ) {
                    BeanPicture(bean, Modifier.size(width = 68.dp, height = 80.dp), corner = 18.dp, showLabel = false)
                }
                Text(
                    bean.name,
                    style = BloomType.chip.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold),
                    color = Bloom.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        item(key = "novo") {
            Column(
                Modifier.width(78.dp).bouncyClick(onClickLabel = "Cadastrar grão", onClick = onNew),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 78.dp, height = 90.dp)
                        .drawBehind {
                            drawRoundRect(
                                Bloom.Muted,
                                cornerRadius = CornerRadius(22.dp.toPx()),
                                style = Stroke(width = 2.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f))),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Add, null, tint = Bloom.Ink, modifier = Modifier.size(26.dp)) }
                Text("Novo grão", style = BloomType.chip, color = Bloom.Ink, maxLines = 1)
            }
        }
    }
}

/** "Diário da xícara": o que gostou e o que mudaria, com o aviso do lembrete. */
@Composable
fun DiaryFields(
    liked: String,
    change: String,
    onLiked: (String) -> Unit,
    onChange: (String) -> Unit,
    beanName: String?,
    coffeeName: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Diário da xícara")
        val target = beanName ?: coffeeName.trim().takeIf { it.isNotEmpty() }
        if (beanName != null) {
            Text("Vai para o diário do $beanName.", style = BloomType.bodySmall, color = Bloom.Muted)
        }
        PillField(liked, onLiked, label = "O que eu gostei", placeholder = "Doçura, corpo, acidez…", singleLine = false)
        PillField(change, onChange, label = "O que eu mudaria na próxima", placeholder = "Moer mais fino, mais tempo de bloom…", singleLine = false)
        AnimatedVisibility(
            change.isNotBlank() || liked.isNotBlank(),
            enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Bloom.Mustard)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.AutoMirrored.Outlined.StickyNote2, null, tint = Bloom.Ink, modifier = Modifier.size(18.dp))
                Text(
                    if (target != null) "Eu te lembro disso no próximo preparo com o $target." else "Diga qual café usou para eu te lembrar disso na próxima xícara.",
                    style = BloomType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Bloom.Ink,
                )
            }
        }
    }
}

/** Fundo dos botões sobre a foto do grão, no topo do detalhe. */
val PhotoButton: Color = Bloom.Cream.copy(alpha = 0.85f)

/** Diário no detalhe do preparo: o grão usado e o que a pessoa anotou. */
@Composable
fun BrewDiary(note: com.heitoranglada.coffeeapp.data.journal.BrewNote?, bean: Bean?, onOpenBean: (Long) -> Unit) {
    if (note?.hasText != true && bean == null) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Diário da xícara")
        if (bean != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .bouncyClick(pressedScale = 0.97f, onClickLabel = "Abrir ${bean.name}") { onOpenBean(bean.id) }
                    .clip(RoundedCornerShape(22.dp))
                    .background(Bloom.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BeanPicture(bean, Modifier.size(width = 52.dp, height = 60.dp), corner = 16.dp, showLabel = false)
                Column(Modifier.weight(1f)) {
                    Text(bean.name, style = BloomType.display(17.sp, -0.01f), color = Bloom.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val line = bean.originLine()
                    if (line.isNotBlank()) Text(line, style = BloomType.bodySmall.copy(fontSize = 12.sp), color = Bloom.Muted, maxLines = 1)
                }
                Text("ver grão", style = BloomType.chip, color = Bloom.Terra, modifier = Modifier.padding(end = 8.dp))
            }
        }
        if (note != null && note.liked.isNotBlank()) DiaryQuote("O que eu gostei", note.liked, Bloom.Sand)
        if (note != null && note.change.isNotBlank()) DiaryQuote("O que eu mudaria", note.change, Bloom.Mustard)
    }
}

@Composable
private fun DiaryQuote(label: String, text: String, background: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Eyebrow(label, color = Bloom.Ink.copy(alpha = 0.75f))
        Text(text, style = BloomType.body, color = Bloom.Ink)
    }
}

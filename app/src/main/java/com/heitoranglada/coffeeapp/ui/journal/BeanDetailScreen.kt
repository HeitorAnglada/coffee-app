package com.heitoranglada.coffeeapp.ui.journal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.ui.appViewModelFactory
import com.heitoranglada.coffeeapp.ui.components.BigPillButton
import com.heitoranglada.coffeeapp.ui.components.Blob
import com.heitoranglada.coffeeapp.ui.components.BloomDialog
import com.heitoranglada.coffeeapp.ui.components.CircleIconButton
import com.heitoranglada.coffeeapp.ui.components.Eyebrow
import com.heitoranglada.coffeeapp.ui.components.MiniBeans
import com.heitoranglada.coffeeapp.ui.components.Pill
import com.heitoranglada.coffeeapp.ui.components.PillFlow
import com.heitoranglada.coffeeapp.ui.components.SectionTitle
import com.heitoranglada.coffeeapp.ui.components.Sheet
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.components.popIn
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import com.heitoranglada.coffeeapp.ui.theme.palette

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BeanDetailScreen(
    beanId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenBrew: (Long) -> Unit,
    onBrew: () -> Unit,
    vm: BeanDetailViewModel = viewModel(
        key = "bean-$beanId",
        factory = appViewModelFactory { BeanDetailViewModel(it, beanId) },
    ),
) {
    val bean by vm.bean.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val b = bean

    if (b == null) {
        Box(Modifier.fillMaxSize().background(Bloom.Cream), contentAlignment = Alignment.Center) {
            Blob(Bloom.Terra, Modifier.size(110.dp), periodMs = 2000, wobble = 0.12f)
        }
        return
    }
    val scroll = rememberScrollState()

    Column(Modifier.fillMaxSize().background(Bloom.Cream).verticalScroll(scroll)) {
        Box(Modifier.fillMaxWidth().height(380.dp)) {
            val photos = b.photos
            if (photos.size > 1) {
                val pager = rememberPagerState { photos.size }
                HorizontalPager(pager, Modifier.fillMaxSize().graphicsLayer { translationY = scroll.value * 0.4f }) { page ->
                    BeanPicture(b, Modifier.fillMaxSize(), corner = 0.dp, photo = photos[page])
                }
                Row(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    photos.indices.forEach { i ->
                        val on = i == pager.currentPage
                        Box(
                            Modifier
                                .size(width = if (on) 22.dp else 8.dp, height = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (on) Bloom.Ink else Bloom.Ink.copy(alpha = 0.35f)),
                        )
                    }
                }
            } else {
                BeanPicture(b, Modifier.fillMaxSize().graphicsLayer { translationY = scroll.value * 0.4f }, corner = 0.dp)
            }
            Row(
                Modifier.statusBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBack, background = PhotoButton, size = 46.dp)
                Spacer(Modifier.weight(1f))
                CircleIconButton(Icons.Filled.Edit, "Editar grão", { onEdit(b.id) }, background = PhotoButton, size = 46.dp)
                Spacer(Modifier.width(8.dp))
                CircleIconButton(Icons.Filled.Delete, "Excluir grão", { confirmDelete = true }, background = PhotoButton, size = 46.dp)
            }
        }
        Sheet(overlap = 28.dp) {
            Column {
                val place = listOf(b.origin, b.roaster).filter { it.isNotBlank() }.joinToString(" · ")
                if (place.isNotBlank()) Eyebrow(place, Modifier.enterCascade(0), Bloom.Muted)
                Text(b.name, style = BloomType.display(40.sp, -0.035f), color = Bloom.Ink, modifier = Modifier.padding(top = 4.dp).enterCascade(1))
            }
            val chips = listOfNotNull(
                b.variety.takeIf { it.isNotBlank() },
                b.process.takeIf { it.isNotBlank() },
                b.roast.takeIf { it.isNotBlank() }?.let { "Torra ${it.lowercase()}" },
                b.lot.takeIf { it.isNotBlank() }?.let { "Lote $it" },
                b.roastDate.takeIf { it.isNotBlank() }?.let { "Torrado em $it" },
            )
            if (chips.isNotEmpty()) {
                PillFlow(Modifier.enterCascade(2)) { chips.forEach { Pill(it, Bloom.Sand) } }
            }
            if (b.flavorNotes.isNotBlank()) {
                Text(
                    buildAnnotatedString {
                        append("Notas do pacote: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Bloom.Ink)) { append(b.flavorNotes) }
                    },
                    style = BloomType.body,
                    color = Bloom.Body,
                    modifier = Modifier.enterCascade(3),
                )
            }

            val reminder = entries.firstOrNull { it.note?.change?.isNotBlank() == true }
            if (reminder != null) {
                val n = reminder.note!!
                Box(
                    Modifier
                        .fillMaxWidth()
                        .popIn(250)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Bloom.Mustard),
                ) {
                    Blob(Bloom.Cream.copy(alpha = 0.3f), Modifier.size(130.dp).align(Alignment.TopEnd).padding(start = 30.dp, bottom = 30.dp), seed = 2)
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.AutoMirrored.Outlined.StickyNote2, null, tint = Bloom.Ink, modifier = Modifier.size(20.dp))
                            Eyebrow("Para a próxima xícara", color = Bloom.Ink.copy(alpha = 0.85f))
                        }
                        Text("“${n.change}”", style = BloomType.display(22.sp, -0.02f).copy(lineHeight = 25.sp), color = Bloom.Ink)
                        Text(
                            "Do seu ${n.recipeName ?: n.method.label} de ${shortDay(n.createdAt)}. Aparece quando você for preparar este café.",
                            style = BloomType.bodySmall,
                            color = Bloom.Ink,
                        )
                    }
                }
            }

            SectionTitle(if (entries.isEmpty()) "Nenhum preparo ainda" else "Preparos com ele")
            if (entries.isEmpty()) {
                Text(
                    "Quando salvar um preparo com este café, ele aparece aqui com o que você gostou e o que mudaria.",
                    style = BloomType.body,
                    color = Bloom.Body,
                )
            }
            entries.forEachIndexed { i, entry -> EntryCard(entry, Modifier.enterCascade(4 + i)) { onOpenBrew(entry.brew.id) } }

            BigPillButton(
                "Preparar este café",
                {
                    vm.brewWithThis()
                    onBrew()
                },
                leading = { Icon(Icons.Filled.PlayArrow, null, tint = Bloom.Mustard, modifier = Modifier.size(24.dp)) },
            )
            Spacer(Modifier.navigationBarsPadding().height(16.dp))
        }
    }

    if (confirmDelete) {
        BloomDialog(
            title = "Excluir ${b.name}?",
            text = "O grão e as fotos saem do diário. Os preparos continuam no histórico.",
            confirm = "Excluir",
            onConfirm = {
                confirmDelete = false
                vm.delete(onBack)
            },
            dismiss = "Cancelar",
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun EntryCard(entry: BeanBrewEntry, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val brew = entry.brew
    val note = entry.note
    Column(
        modifier
            .fillMaxWidth()
            .bouncyClick(pressedScale = 0.97f, onClickLabel = "Abrir preparo", onClick = onClick)
            .clip(RoundedCornerShape(24.dp))
            .background(Bloom.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(brew.method.palette.bg))
            Text(brew.recipeName ?: brew.method.label, style = BloomType.display(17.sp, -0.01f), color = Bloom.Ink, modifier = Modifier.weight(1f), maxLines = 1)
            Text(shortDay(brew.brewedAt), style = BloomType.label, color = Bloom.Muted)
        }
        if (brew.rating != null) MiniBeans(brew.rating)
        if (note?.liked?.isNotBlank() == true) LabeledLine("Gostei:", note.liked)
        if (note?.change?.isNotBlank() == true) LabeledLine("Mudaria:", note.change)
        if (note?.hasText != true && brew.notes.isNotBlank()) LabeledLine("Notas:", brew.notes)
    }
}

@Composable
private fun LabeledLine(label: String, text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Bloom.Ink)) { append("$label ") }
            append(text)
        },
        style = BloomType.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
        color = Bloom.Body,
    )
}
